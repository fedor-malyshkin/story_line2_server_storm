package ru.nlp_project.story_line2.server_storm.topologies;

import java.io.IOException;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.bolt.ContentExtractingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.MaintenanceBolt;
import ru.nlp_project.story_line2.server_storm.bolt.MetricsCollectorBolt;
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.MaintenanceEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.MetricsEventEmitterSpout;

/**
 * <p/>
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * <p/>
 * Deploy: ./storm jar server_storm-0.1-SNAPSHOT-all.jar  ru.nlp_project.story_line2.server_storm.topologies.CrawlerDataProcessingTopology http://datahouse01.nlp-project.ru:9000/server_storm.yaml
 * <p/>
 *
 * @author fedor
 */
public class CrawlerDataProcessingTopology {

	public static final String TOPOLOGY_NAME = "crawler_data_processing_topology";
	private static final String BOLT_ELASTICSEARCH_INDEXER = "elasticsearch_indexer";
	private static final String BOLT_CONTENT_EXTRACTOR = "content_extractor";
	private static final String BOLT_TEXT_PROCESSOR = "text_processor";
	private static final String SPOUT_CRAWLER_ENTRY_READER = "crawler_entry_reader";
	private static final String SPOUT_MAINTENANCE_ENTRY_READER = "maintenance_reader";
	private static final String BOLT_MAINTENANCE_PROCESSOR = "maintenance_processor";
	private static final String SPOUT_METRICS_EVENT_EMITTER = "metrics_event_emitter";
	private static final String BOLT_METRICS_PROCESSOR = "metrics_collector";

	public static void main(String args[]) throws IOException, AlreadyAliveException,
			InvalidTopologyException, AuthorizationException {
		if (args.length == 0) {
			throw new IllegalStateException(
					"First argument must be url ('file://....' or 'http://...') with config.");
		}

		deployRemote(args[0]);
		// deployLocal(args[0]);
	}

	private static void deployRemote(String configUrl)
			throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		Config conf = new Config();
		conf.put(IConfigurationManager.STORM_CONFIG_KEY, configUrl);
		conf.setNumEventLoggers(1);
		conf.setNumWorkers(2);
		// no more than ... in pending queue
		conf.setMaxSpoutPending(250);
		// 2 minutes in non-acked state
		conf.setMessageTimeoutSecs(120);
		StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, createTopology());
	}

	protected static StormTopology createTopology() {
		TopologyBuilder builder = new TopologyBuilder();
		// maintenance part
		builder.setSpout(SPOUT_MAINTENANCE_ENTRY_READER, new MaintenanceEntryReaderSpout(), 1);
		builder.setBolt(BOLT_MAINTENANCE_PROCESSOR, new MaintenanceBolt(), 1)
				.allGrouping(SPOUT_MAINTENANCE_ENTRY_READER);
// processing part
		builder.setSpout(SPOUT_CRAWLER_ENTRY_READER, new CrawlerEntryReaderSpout(), 1);
		builder.setBolt(BOLT_CONTENT_EXTRACTOR, new ContentExtractingBolt(), 8)
				.shuffleGrouping(SPOUT_CRAWLER_ENTRY_READER).allGrouping(SPOUT_MAINTENANCE_ENTRY_READER);
		builder.setBolt(BOLT_TEXT_PROCESSOR, new TextProcessingBolt(), 8)
				.shuffleGrouping(BOLT_CONTENT_EXTRACTOR).allGrouping(SPOUT_MAINTENANCE_ENTRY_READER);
		builder.setBolt(BOLT_ELASTICSEARCH_INDEXER, new ElasticsearchIndexingBolt(), 1)
				.shuffleGrouping(BOLT_TEXT_PROCESSOR).allGrouping(SPOUT_MAINTENANCE_ENTRY_READER);

// metrics part
		builder.setSpout(SPOUT_METRICS_EVENT_EMITTER, new MetricsEventEmitterSpout(), 1);
		builder.setBolt(BOLT_METRICS_PROCESSOR, new MetricsCollectorBolt(), 1)
				.allGrouping(SPOUT_METRICS_EVENT_EMITTER);

		return builder.createTopology();
	}
}
