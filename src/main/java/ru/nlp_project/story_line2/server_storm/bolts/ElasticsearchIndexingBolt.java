package ru.nlp_project.story_line2.server_storm.bolts;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import ru.nlp_project.story_line2.server_storm.util.NamesUtil;

public class ElasticsearchIndexingBolt implements IRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7621987543099710796L;
	private OutputCollector collector;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		System.out.println(input.toString());
		collector.ack(input);
		
	}

	@Override
	public void cleanup() {
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
