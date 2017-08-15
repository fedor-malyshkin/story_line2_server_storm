package ru.nlp_project.story_line2.server_storm.bolt;

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
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ContentExtractingBolt implements IRichBolt {
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IGroovyInterpreter groovyInterpreter;
	@Inject
	public IConfigurationManager configurationManager;
	private Logger log;

	@Override
	public void cleanup() {}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(
				new Fields(NamesUtil.TUPLE_FIELD_NAME_SOURCE, NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public void execute(Tuple input) {
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		String source = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_SOURCE);
		try {
			// получить текущую новостную статью
			Map<String, Object> newsArticle = mongoDBClient.getNewsArticle(objectId);
			// из не запись краулера
			Map<String, Object> ce =
					mongoDBClient.getCrawlerEntry(NewsArticle.crawlerIdString(newsArticle));
			// если в краулере поле сырого контента пустое (наверное взято из feed) -- выйти
			if (CrawlerEntry.rawContent(ce) == null || CrawlerEntry.rawContent(ce).isEmpty()) {
				// emit new tuple
				collector.emit(input, Arrays.asList(source, objectId));
				// ack prev tuples
				collector.ack(input);
				return;
			}

			// ... иначе извлечь данные и перенести атрибуты в статью
			Map<String, Object> data = groovyInterpreter.extractData(source, CrawlerEntry.url(ce),
					CrawlerEntry.rawContent(ce));
			if (null == data || data.isEmpty()) {
				log.debug("No content {}:{} ({})", source, CrawlerEntry.path(ce),
						CrawlerEntry.url(ce));
				return;
			}

			Date publicationDate = getDateSafe(data, IGroovyInterpreter.EXTR_KEY_PUB_DATE);
			String title = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_TITLE);
			String imageUrl = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_IMAGE_URL);
			String content = getTextSafe(data, IGroovyInterpreter.EXTR_KEY_CONTENT);

			NewsArticle.publicationDate(newsArticle, publicationDate);
			NewsArticle.title(newsArticle, title);
			NewsArticle.imageUrl(newsArticle, imageUrl);
			NewsArticle.content(newsArticle, content);
			// update crawler entry "publicationn_date"
			if (publicationDate != null) {
				CrawlerEntry.publicationDate(ce, publicationDate);
				mongoDBClient.updateCrawlerEntry(ce);
			}

			mongoDBClient.updateNewsArticle(newsArticle);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			collector.fail(input);
		}
		// emit new tuple
		collector.emit(input, Arrays.asList(source, objectId));
		// ack prev tuples
		collector.ack(input);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	private Date getDateSafe(Map<String, Object> data, String key) {
		Object object = data.get(key);
		if (object != null)
			return (Date) object;
		else
			return null;
	}


	private String getTextSafe(Map<String, Object> data, String key) {
		Object object = data.get(key);
		if (object != null)
			return object.toString();
		else
			return null;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(stormConf).inject(this);
	}
}
