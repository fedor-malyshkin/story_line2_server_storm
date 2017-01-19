package ru.nlp_project.story_line2.server_storm.bolts;

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

import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ApplicationBuilder;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;
import ru.nlp_project.story_line2.server_storm.util.NamesUtil;

public class ElasticsearchIndexingBolt implements IRichBolt {

	private static final long serialVersionUID = -7621987543099710796L;
	private OutputCollector collector;
	private Logger logger;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public ISearchManager searchManager;


	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		logger = LoggerFactory.getLogger(this.getClass());
		ApplicationBuilder.inject(this);
	}

	@Override
	public void execute(Tuple input) {
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		try {
			NewsArticle newsArticle = mongoDBClient.getNewsArticle(objectId);
			searchManager.index(newsArticle);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			collector.fail(input);
		}
		// ack prev tuples
		collector.ack(input);
	}

	@Override
	public void cleanup() {
		searchManager.shutdown();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
