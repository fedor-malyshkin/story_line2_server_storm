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
import ru.nlp_project.story_line2.server_storm.IConfigurationManager.MetricsConfiguration;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MetricsEventEmitterSpout implements IRichSpout {

	private static final long serialVersionUID = 1L;
	@Inject
	public IConfigurationManager configurationManager;
	private SpoutOutputCollector collector;
	private Logger log;
	private MetricsConfiguration metricsConfiguration;
	/**
	 * Last request for next tuple.
	 */
	private long lastRequest;
	/**
	 * request period in seconds.
	 */
	private int requestPeriod;
	private boolean enabled;

	@Override
	public void ack(Object msgId) {
		// do nothing
	}

	@Override
	public void activate() {
		initialize();
	}


	@Override
	public void close() {
		// do nothing
	}

	@Override
	public void deactivate() {
		// do nothing
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE));
	}

	@Override
	public void fail(Object msgId) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	@Override
	public void nextTuple() {
		if (!enabled) {
			return;
		}
		long now = System.currentTimeMillis();
		if ((now - lastRequest) < requestPeriod) {
			return;
		}
// else
		collector.emit(Arrays.asList(NamesUtil.TUPLE_TYPE_METRICS_COMMAND));
		lastRequest = now;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(conf).inject(this);
		initialize();
	}

	private void initialize() {
		lastRequest = 0;
		metricsConfiguration = configurationManager.getMetricsConfiguration();
		enabled = metricsConfiguration.enabled;
		requestPeriod = metricsConfiguration.collectingPeriod * 1_000;
	}

}
