package ru.nlp_project.story_line2.server_storm.spouts;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.NamesUtil;
import ru.nlp_project.story_line2.server_storm.dagger.ApplicationBuilder;

public class CrawlerNewsArticleReaderSpout implements IRichSpout {
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;

	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		ApplicationBuilder.inject(this);
	}

	@Override
	public void close() {
		shutdownMongoDBClient();
	}

	private void shutdownMongoDBClient() {
		mongoDBClient.shutdown();
	}

	@Override
	public void activate() {}

	@Override
	public void deactivate() {}

	@Override
	public void nextTuple() {
		collector.emit(Arrays.asList("domain" + System.currentTimeMillis(),
				"id" + System.currentTimeMillis()), System.currentTimeMillis());
	}

	@Override
	public void ack(Object msgId) {
		System.out.println(msgId);

	}

	@Override
	public void fail(Object msgId) {
		System.out.println(msgId);

	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(NamesUtil.DOMAIN_FIELD_NAME, NamesUtil.ID_FIELD_NAME));

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
