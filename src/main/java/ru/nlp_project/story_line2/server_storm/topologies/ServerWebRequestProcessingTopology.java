package ru.nlp_project.story_line2.server_storm.topologies;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FUN_NAME_GET_NEWS_ARTICLE;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FUN_NAME_GET_NEWS_HEADERS;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FUN_NAME_GET_NEWS_IMAGES;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ARGS;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_JSON;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_RESULT;

import java.io.IOException;
import org.apache.storm.Config;
import org.apache.storm.LocalDRPC;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.Stream;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.tuple.Fields;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.functions.JSONConverterFunction;
import ru.nlp_project.story_line2.server_storm.functions.NewsArticleFinderFunction;
import ru.nlp_project.story_line2.server_storm.functions.NewsHeaderFinderFunction;

/**
 * <p/>
 * Run with JVM args "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * <p/>
 * Deploy: ./storm jar server_storm-0.1-SNAPSHOT-all.jar  ru.nlp_project.story_line2.server_storm.topologies.ServerWebRequestProcessingTopology http://datahouse01.nlp-project.ru:9000/server_storm.yaml
 * <p/>
 *
 * @author fedor
 */
public class ServerWebRequestProcessingTopology {

	public static final String TOPOLOGY_NAME = "server_web_request_processing_topology";

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
		StormSubmitter.submitTopology(TOPOLOGY_NAME, conf, createTopology(null));
	}

	protected static void updateConfiguration(String configUrl, Config conf) {
		conf.put(IConfigurationManager.STORM_CONFIG_KEY, configUrl);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60);
	}

	protected static StormTopology createTopology(LocalDRPC drpc) {
		TridentTopology topo = new TridentTopology();

		// headers
		createNewDRPCStream(topo, FUN_NAME_GET_NEWS_HEADERS, drpc)
				.each(new Fields(TUPLE_FIELD_NAME_ARGS),
						new NewsHeaderFinderFunction(FUN_NAME_GET_NEWS_HEADERS),
						new Fields(TUPLE_FIELD_NAME_RESULT))
				.name("elastic-searcher-" + FUN_NAME_GET_NEWS_HEADERS)
				.each(new Fields(TUPLE_FIELD_NAME_RESULT),
						new JSONConverterFunction(FUN_NAME_GET_NEWS_HEADERS),
						new Fields(TUPLE_FIELD_NAME_JSON))
				.name("json-converter").project(new Fields(TUPLE_FIELD_NAME_JSON));

		//articles
		createNewDRPCStream(topo, FUN_NAME_GET_NEWS_ARTICLE, drpc)
				.each(new Fields(TUPLE_FIELD_NAME_ARGS),
						new NewsArticleFinderFunction(FUN_NAME_GET_NEWS_ARTICLE),
						new Fields(TUPLE_FIELD_NAME_RESULT))
				.name("elastic-extractor-" + FUN_NAME_GET_NEWS_ARTICLE)
				.each(new Fields(TUPLE_FIELD_NAME_RESULT),
						new JSONConverterFunction(FUN_NAME_GET_NEWS_ARTICLE),
						new Fields(TUPLE_FIELD_NAME_JSON))
				.name("json-converter").project(new Fields(TUPLE_FIELD_NAME_JSON));

		// images
		createNewDRPCStream(topo, FUN_NAME_GET_NEWS_IMAGES, drpc)
				.each(new Fields(TUPLE_FIELD_NAME_ARGS),
						new NewsArticleFinderFunction(FUN_NAME_GET_NEWS_IMAGES),
						new Fields(TUPLE_FIELD_NAME_RESULT))
				.name("elastic-extractor-" + FUN_NAME_GET_NEWS_IMAGES)
				.each(new Fields(TUPLE_FIELD_NAME_RESULT),
						new JSONConverterFunction(FUN_NAME_GET_NEWS_IMAGES),
						new Fields(TUPLE_FIELD_NAME_JSON))
				.name("json-converter").project(new Fields(TUPLE_FIELD_NAME_JSON));

		return topo.build();
	}

	private static Stream createNewDRPCStream(TridentTopology topology, String streamName,
			LocalDRPC LocalDRPC) {
		if (LocalDRPC == null) {
			return topology.newDRPCStream(streamName);
		} else {
			return topology.newDRPCStream(streamName, LocalDRPC);
		}
	}

}
