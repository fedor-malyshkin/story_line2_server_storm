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
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerEntryReaderSpout;

/**
 * <p/>
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * <p/>
 * Deploy: ./storm jar server_storm-0.1-SNAPSHOT-all.jar  ru.nlp_project.story_line2.server_storm.topologies.CrawlerDataProcessingTopology http://datahouse01.nlp-project.ru:9000/server_storm.yaml
 * <p/>
 * 
 * @author fedor
 *
 */
public class CrawlerDataProcessingTopology {
	public static final String TOPOLOGY_NAME = "crawler_data_processing_topology";
	public static final String BOLT_ELASTICSEARCH_INDEXER = "elasticsearch_indexer";
	public static final String BOLT_CONTENT_EXTRACTOR = "content_extractor";
	public static final String BOLT_TEXT_PROCESSOR = "text_processor";
	public static final String SPOUT_CRAWLER_ENTRY_READER = "crawler_entry_reader";

	public static void main(String args[]) throws IOException, AlreadyAliveException,
			InvalidTopologyException, AuthorizationException {
		if (args.length == 0)
			throw new IllegalStateException(
					"First argument must be url ('file://....' or 'http://...') with config.");

		deployRemote(args[0]);
		// deployLocal(args[0]);
	}

	private static void deployRemote(String configUrl)
			throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		Config conf = new Config();
		conf.put(IConfigurationManager.STORM_CONFIG_KEY, configUrl);
		conf.setNumWorkers(2);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60);

		StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, createTopology());
	}

	protected static StormTopology createTopology() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SPOUT_CRAWLER_ENTRY_READER, new CrawlerEntryReaderSpout(), 1);
		builder.setBolt(BOLT_CONTENT_EXTRACTOR, new ContentExtractingBolt(), 4)
				.shuffleGrouping(SPOUT_CRAWLER_ENTRY_READER);
		builder.setBolt(BOLT_TEXT_PROCESSOR, new TextProcessingBolt(), 1)
				.shuffleGrouping(BOLT_CONTENT_EXTRACTOR);
		builder.setBolt(BOLT_ELASTICSEARCH_INDEXER, new ElasticsearchIndexingBolt(), 1)
				.shuffleGrouping(BOLT_TEXT_PROCESSOR);
		return builder.createTopology();
	}
}
