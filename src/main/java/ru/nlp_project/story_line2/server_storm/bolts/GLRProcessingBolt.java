package ru.nlp_project.story_line2.server_storm.bolts;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGLRParser;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ApplicationBuilder;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;
import ru.nlp_project.story_line2.server_storm.util.NamesUtil;

public class GLRProcessingBolt implements IRichBolt {

	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public IGLRParser glrParser;

	private static final long serialVersionUID = 8970948884690790271L;
	private OutputCollector collector;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		ApplicationBuilder.inject(this);
	}

	@Override
	public void execute(Tuple input) {
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		String domain = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_DOMAIN);

		NewsArticle newsArticle = mongoDBClient.getNewsArticle(objectId);
		glrParser.parseText(newsArticle.content);
		// emit new tuple
		collector.emit(input, Arrays.asList(domain, objectId));
		// ack prev tuples
		collector.ack(input);
	}

	@Override
	public void cleanup() {
		glrParser.shutdown();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(NamesUtil.TUPLE_FIELD_NAME_DOMAIN, NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
