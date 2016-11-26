package ru.nlp_project.story_line2.server_storm.bolts;

import java.util.Arrays;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import ru.nlp_project.story_line2.server_storm.NamesUtil;

public class NounExtractorBolt implements IRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8970948884690790271L;
	private OutputCollector collector;

	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		System.out.println(input.toString());
		collector.emit(input, Arrays.asList("domain" + System.currentTimeMillis(),
				"id" + System.currentTimeMillis()));
		collector.ack(input);
	}

	@Override
	public void cleanup() {}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(NamesUtil.DOMAIN_FIELD_NAME, NamesUtil.ID_FIELD_NAME));

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
