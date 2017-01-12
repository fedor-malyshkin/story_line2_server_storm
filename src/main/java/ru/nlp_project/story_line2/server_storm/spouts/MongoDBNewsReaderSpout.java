package ru.nlp_project.story_line2.server_storm.spouts;

import java.util.Arrays;
import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;

import ru.nlp_project.story_line2.server_storm.NamesUtil;

public class MongoDBNewsReaderSpout implements IRichSpout {
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		initilizeMongoDBClient();
	}

	private void initilizeMongoDBClient() {

	}

	@Override
	public void close() {
		shutdownMongoDBClient();

	}

	private void shutdownMongoDBClient() {

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
