package ru.nlp_project.story_line2.server_storm.topologies;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ServerWebRequestProcessingTopologyTest {

	private static LocalDRPC drpc;
	private static LocalCluster cluster;
	private static ServerStormTestModule serverStormTestModule;
	private static IMongoDBClient mongoDBClient;
	private static ISearchManager searchManager;

	@BeforeClass
	public static void setUpClass() {
		// dagger
		ServerStormBuilder.setTestMode(true);
		serverStormTestModule = ServerStormBuilder.getServerStormTestModule();

		// mocks
		mongoDBClient = mock(IMongoDBClient.class);
		serverStormTestModule.mongoDBClient = mongoDBClient;
		searchManager = mock(ISearchManager.class);
		serverStormTestModule.searchManager = searchManager;

		// storm
		drpc = new LocalDRPC();
		cluster = new LocalCluster();
		// Map<String, Object> config = Utils.readStormConfig();
		Map<String, Object> config = new HashMap<String, Object>();
		cluster.submitTopology(ServerWebRequestProcessingTopology.TOPOLOGY_NAME, config,
				ServerWebRequestProcessingTopology.createTopology(drpc));
	}

	@AfterClass
	public static void tearDown() {
		cluster.killTopology(ServerWebRequestProcessingTopology.TOPOLOGY_NAME);
		cluster.shutdown();
		drpc.shutdown();
	}

	@Test
	public void testGetNewsHeaders() {
		String source = "bnkomi.ru";
		int count = 25;
		// args
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("source", source);
		args.put("count", count);

		// search results
		Map<String, Object> entry1 = new HashMap<>();
		entry1.put("_id", 1);
		Map<String, Object> entry2 = new HashMap<>();
		entry2.put("_id", 2);
		entry2.put("title", "XXXX");
		List<Map<String, Object>> searchResult = Arrays.asList(entry1, entry2);

		when(searchManager.getNewsHeaders(eq(source), eq(count))).thenReturn(searchResult);

		String execute =
				drpc.execute(NamesUtil.FUN_NAME_GET_NEWS_HEADERS, JSONUtils.serialize(args));
		verify(searchManager, only()).getNewsHeaders(source, count);

		Assertions.assertThat(execute).isNotNull();
		String res = removeSurroundingBrackets(execute);

		List<Map<String, Object>> list = JSONUtils.deserializeList(res);
		Map<String, Object> map = list.get(1);
		Assertions.assertThat(map.get("_id")).isEqualTo(2);
		Assertions.assertThat(map.get("title")).isEqualTo("XXXX");
	}

	private String removeSurroundingBrackets(String execute) {
		String res = StringUtils.removeStartIgnoreCase(execute, "[[\"");
		res = StringUtils.removeEndIgnoreCase(res, "\"]]");
		res = StringEscapeUtils.unescapeJson(res);
		return res;
	}

}
