package ru.nlp_project.story_line2.server_storm.bolt;

import java.util.List;
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
import ru.nlp_project.story_line2.server_storm.IMetricsManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MetricsCollectorBolt implements IRichBolt {

	private static final long serialVersionUID = 1L;
	@Inject
	public IMetricsManager metricsManager;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public ISearchManager searchManager;

	private Logger log;

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

	@Override
	public void execute(Tuple input) {
		String tupleType = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE);

		switch (tupleType) {
			case NamesUtil.TUPLE_TYPE_METRICS_COMMAND: {
				processMetricsCommand();
				break;
			}
			default: {
				log.error("Unknown tuple type: " + tupleType);
				throw new IllegalStateException("Unknown tuple type: " + tupleType);

			}
		}
	}

	private void processMetricsCommand() {

		try {
			List<String> crawlerEntrySources = mongoDBClient.getCrawlerEntrySources();
			for (String source : crawlerEntrySources) {
				metricsManager.crawlerEntriesCountDB(source, mongoDBClient.getCrawlerEntriesCount(source));
				metricsManager
						.processedCrawlerEntriesCountDB(source,
								mongoDBClient.getProcessedCrawlerEntriesCount(source));
				metricsManager
						.unprocessedCrawlerEntriesCountDB(source,
								mongoDBClient.getUnprocessedCrawlerEntriesCount(source, true));
				metricsManager
						.archivedCrawlerEntriesCountDB(source,
								mongoDBClient.getArchivedCrawlerEntriesCount(source));
			}

			List<String> newsArticleSources = mongoDBClient.getNewsArticleSources();
			for (String source : newsArticleSources) {
				metricsManager.newsArticlesCountDB(source, mongoDBClient.getNewsArticlesCount(source));
				metricsManager
						.processedNewsArticlesCountDB(source,
								mongoDBClient.getProcessedNewsArticlesCount(source));
				metricsManager
						.unprocessedNewsArticlesCountDB(source,
								mongoDBClient.getUnprocessedNewsArticlesCount(source));
				// elastic
				metricsManager.newsArticlesCountSearch(source, searchManager.getNewsArticlesCount(source));

			}
		} catch (Exception e) {
			log.error("Exception while collecting metrics: {}", e.getMessage(), e);
		}
	}


	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}


	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(stormConf).inject(this);
	}

}
