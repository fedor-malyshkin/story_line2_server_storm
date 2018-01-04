package ru.nlp_project.story_line2.server_storm.spout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MaintenanceEntryReaderSpout implements IRichSpout {

	private static final long serialVersionUID = 1L;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;

	private SpoutOutputCollector collector;
	private Logger log;

	@Override
	public void ack(Object msgId) {
	}

	@Override
	public void activate() {
	}


	@Override
	public void close() {
		mongoDBClient.shutdown();
	}

	@Override
	public void deactivate() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(
				new Fields(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE, NamesUtil.TUPLE_FIELD_NAME_MAINTENANCE_COMMAND));
	}

	@Override
	public void fail(Object msgId) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<>();
		// 1 day
		conf.put(Config.TOPOLOGY_SPOUT_WAIT_STRATEGY, "org.apache.storm.spout.SleepSpoutWaitStrategy");
		conf.put(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS, 1_000 * 60 * 60 * 24);
		return conf;
	}

	@Override
	public void nextTuple() {
		try {
			Map<String, Object> maintenanceEntry = mongoDBClient.getNextMaintenanceCommandEntry();
			// there is no data
			if (null == maintenanceEntry) {
				return;
			}
			collector.emit(Arrays.asList(NamesUtil.TUPLE_TYPE_MAINTENANCE_COMMAND, maintenanceEntry));
		} catch (Exception e) {
			log.error("nextTuple.log" + e.getMessage(), e);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(conf).inject(this);
	}

}