package ru.nlp_project.story_line2.server_storm.topologies;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.STREAM_ARCHIVE_OLD_CRAWLER_ENTRIES;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.STREAM_PURGE_NEWS_ARTICLE_IMAGES;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ID;

import java.io.IOException;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.functions.ArchiveCrawlerEntryFunction;
import ru.nlp_project.story_line2.server_storm.functions.PurgeNewsArticleImagesFunction;
import ru.nlp_project.story_line2.server_storm.spout.UnarchivedCrawlerEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.UnpurgedNewsArticleReaderSpout;

/**
 * <p/>
 * Run with JVM args "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * <p/>
 * Deploy: ./storm jar server_storm-0.1-SNAPSHOT-all.jar ru.nlp_project.story_line2.server_storm.topologies.MaintenanceTopology
 * http://datahouse01.nlp-project.ru:9000/server_storm.yaml
 * <p/>
 *
 * @author fedor
 */
public class MaintenanceTopology {

	public static final String TOPOLOGY_NAME = "maintenance_topology";

	public static void main(String args[]) throws IOException, AlreadyAliveException,
			InvalidTopologyException, AuthorizationException {
		if (args.length == 0) {
			throw new IllegalStateException(
					"First argument must be url ('file://....' or 'http://...') with config.");
		}

		deployRemote(args[0]);
	}

	private static void deployRemote(String configUrl)
			throws AlreadyAliveException, InvalidTopologyException, AuthorizationException {
		Config conf = new Config();
		updateConfiguration(configUrl, conf);
		StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, createTopology());
	}

	protected static void updateConfiguration(String configUrl, Config conf) {
		conf.put(IConfigurationManager.STORM_CONFIG_KEY, configUrl);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(30);
	}

	protected static StormTopology createTopology() {
		TridentTopology topo = new TridentTopology();

		topo.newStream(STREAM_ARCHIVE_OLD_CRAWLER_ENTRIES, new UnarchivedCrawlerEntryReaderSpout())
				.name("UnarchivedCrawlerEntryReaderSpout")
				.each(
						new Fields(TUPLE_FIELD_NAME_ID), new ArchiveCrawlerEntryFunction(),
						new Fields()).name("ArchiveCrawlerEntryFunction");
		topo.newStream(STREAM_PURGE_NEWS_ARTICLE_IMAGES, new UnpurgedNewsArticleReaderSpout())
				.name("UnpurgedNewsArticleReaderSpout").each(
				new Fields(TUPLE_FIELD_NAME_ID), new PurgeNewsArticleImagesFunction(),
				new Fields()).name("PurgeNewsArticleImagesFunction");

		return topo.build();
	}


}
