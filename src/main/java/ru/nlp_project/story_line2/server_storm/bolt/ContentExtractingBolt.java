package ru.nlp_project.story_line2.server_storm.bolt;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_MAINTENANCE_COMMAND;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IImageDownloader;
import ru.nlp_project.story_line2.server_storm.IMetricsManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ContentExtractingBolt implements IRichBolt {

	private static final long serialVersionUID = 1L;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IGroovyInterpreter groovyInterpreter;
	@Inject
	public IImageDownloader imageDownloader;
	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public IMetricsManager metricsManager;
	private OutputCollector collector;
	private Logger log;

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(
				new Fields(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE, NamesUtil.TUPLE_FIELD_NAME_SOURCE,
						NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public void execute(Tuple input) {
		String tupleType = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE);

		switch (tupleType) {
			case NamesUtil.TUPLE_TYPE_NEWS_ENTRY: {
				processNewsEntry(input);
				break;
			}
			case NamesUtil.TUPLE_TYPE_MAINTENANCE_COMMAND: {
				processMaintenanceCommand(input);
				break;
			}
			default: {
				log.error("Unknown tuple type: " + tupleType);
				throw new IllegalStateException("Unknown tuple type: " + tupleType);

			}
		}
	}


	private void processMaintenanceCommand(Tuple input) {
		Map<String, Object> maintenanceEntry = (Map<String, Object>) input
				.getValueByField(TUPLE_FIELD_NAME_MAINTENANCE_COMMAND);

	}

	private void processNewsEntry(Tuple input) {
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		String source = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_SOURCE);
		try {
			// получить текущую новостную статью
			long start = System.currentTimeMillis();
			Map<String, Object> newsArticle = mongoDBClient.getNewsArticle(objectId);
			metricsManager
					.callDuration(ContentExtractingBolt.class, IMongoDBClient.class, "getNewsArticle",
							System.currentTimeMillis() - start);

			// из не запись краулера
			start = System.currentTimeMillis();
			Map<String, Object> ce =
					mongoDBClient.getCrawlerEntry(NewsArticle.crawlerIdString(newsArticle));
			metricsManager
					.callDuration(ContentExtractingBolt.class, IMongoDBClient.class, "getCrawlerEntry",
							System.currentTimeMillis() - start);
			// если в краулере поле сырого контента пустое (наверное взято из feed) -- выйти
			if (CrawlerEntry.rawContent(ce) == null || CrawlerEntry.rawContent(ce).isEmpty()) {
				// emit new tuple
				collector.emit(input, Arrays.asList(NamesUtil.TUPLE_TYPE_NEWS_ENTRY, source, objectId));
				// ack prev tuples
				collector.ack(input);
				return;
			}

			// ... иначе извлечь данные и перенести атрибуты в статью
			start = System.currentTimeMillis();
			Map<String, Object> data = groovyInterpreter.extractData(source, CrawlerEntry.url(ce),
					CrawlerEntry.rawContent(ce));
			metricsManager
					.callDuration(ContentExtractingBolt.class, IGroovyInterpreter.class, "extractData",
							System.currentTimeMillis() - start);
			if (null == data || data.isEmpty()) {
				log.debug("No content {}:{} ({})", source, CrawlerEntry.path(ce),
						CrawlerEntry.url(ce));
				return;
			}

			Date publicationDate = getDateSafe(data, IGroovyInterpreter.EXTR_KEY_PUB_DATE);
			String title = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_TITLE);
			String imageUrl = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_IMAGE_URL);
			String content = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_CONTENT);

			if (publicationDate != null) {
				NewsArticle.publicationDate(newsArticle, publicationDate);
				// update crawler entry "publication_date"
				CrawlerEntry.publicationDate(ce, publicationDate);
				start = System.currentTimeMillis();
				mongoDBClient.updateCrawlerEntry(ce);
				metricsManager
						.callDuration(ContentExtractingBolt.class, IMongoDBClient.class, "updateCrawlerEntry",
								System.currentTimeMillis() - start);
			}
			//  everething is clear
			NewsArticle.processingDate(newsArticle, new Date());

			if (title != null) {
				NewsArticle.title(newsArticle, title);
			}
			if (imageUrl != null) {
				NewsArticle.imageUrl(newsArticle, imageUrl);
			}
			if (content != null) {
				NewsArticle.content(newsArticle, content);
			}

			// получаем повторно, т.к. ссылка могла быть получена при парсинге (и установлена), а могла и быть ранее (и сохранилась)
			imageUrl = NewsArticle.imageUrl(newsArticle);
			// download image
			if (imageUrl != null && !imageUrl.isEmpty()) {
				start = System.currentTimeMillis();
				byte[] imageData = imageDownloader.downloadImage(imageUrl);
				metricsManager
						.callDuration(ContentExtractingBolt.class, IImageDownloader.class, "downloadImage",
								System.currentTimeMillis() - start);
				NewsArticle.imageData(newsArticle, imageData);
			}

			start = System.currentTimeMillis();
			mongoDBClient.updateNewsArticle(newsArticle);
			metricsManager
					.callDuration(ContentExtractingBolt.class, IMongoDBClient.class, "updateNewsArticle",
							System.currentTimeMillis() - start);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			collector.fail(input);
		}
		// emit new tuple
		collector.emit(input, Arrays.asList(NamesUtil.TUPLE_TYPE_NEWS_ENTRY, source, objectId));
		// ack prev tuples
		collector.ack(input);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	private Date getDateSafe(Map<String, Object> data, String key) {
		Object object = data.get(key);
		if (object != null) {
			return (Date) object;
		} else {
			return null;
		}
	}


	private String getTextSafe(Map<String, Object> data, String key) {
		Object object = data.get(key);
		if (object != null) {
			return object.toString();
		} else {
			return null;
		}
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(stormConf).inject(this);
	}
}
