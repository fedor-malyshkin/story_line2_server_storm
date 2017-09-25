package ru.nlp_project.story_line2.server_storm.topologies;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.generated.KillOptions;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ServerWebRequestProcessingTopologyTest {

	private static LocalDRPC drpc;
	private static LocalCluster cluster;
	private static ServerStormTestModule serverStormTestModule;
	private static IMongoDBClient mongoDBClient;
	private static ISearchManager searchManager;
	private HashMap<String, Object> topologyConfig = new HashMap<>();

	@BeforeClass
	public static void setUpClass() {
		// dagger
		ServerStormBuilder.initializeTestMode();
		serverStormTestModule = ServerStormBuilder.getServerStormTestModule();

		// mocks
		mongoDBClient = mock(IMongoDBClient.class);
		serverStormTestModule.mongoDBClient = mongoDBClient;
		searchManager = mock(ISearchManager.class);
		serverStormTestModule.searchManager = searchManager;

	}

	@Before
	public void setUp() {
		reset(mongoDBClient);
		reset(searchManager);
	}

	@After
	public void tearDown() throws InterruptedException {
		KillOptions options = new KillOptions();
		options.set_wait_secs(20);
		cluster.killTopologyWithOpts(ServerWebRequestProcessingTopology.TOPOLOGY_NAME, options);
		cluster.shutdown();
		drpc.shutdown();
	}

	@Test
	public void testGetNewsHeaders() throws InterruptedException {
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

		when(searchManager.getNewsHeaders(eq(source), eq(count), isNull())).thenReturn(searchResult);

		startAndWaitTopo();

		String execute =
				drpc.execute(NamesUtil.FUN_NAME_GET_NEWS_HEADERS, JSONUtils.serialize(args));
		verify(searchManager, only()).getNewsHeaders(source, count, null);

		Assertions.assertThat(execute).isNotNull();
		String res = removeSurroundingBrackets(execute);

		List<Map<String, Object>> list = JSONUtils.deserializeList(res);
		Map<String, Object> map = list.get(1);
		Assertions.assertThat(map.get("_id")).isEqualTo(2);
		Assertions.assertThat(map.get("title")).isEqualTo("XXXX");
	}

	/**
	 * Получение списка заголовков с определённым сдвигом в виде идентификатора последнего заголовка
	 * (не включая его самого).
	 */
	@Test
	public void testGetNewsHeadersWithLastNewsId() throws InterruptedException {
		String source = "bnkomi.ru";
		int count = 25;
		// important must look like ID!
		Id lastNewsId = new Id("59b4cde7dd12c0cca3588129");
		// args
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("source", source);
		args.put("count", count);
		args.put("last_news_id", lastNewsId);

		// search results
		Map<String, Object> entry1 = new HashMap<>();
		entry1.put("_id", 1);
		Map<String, Object> entry2 = new HashMap<>();
		entry2.put("_id", 2);
		entry2.put("title", "XXXX");
		List<Map<String, Object>> searchResult = Arrays.asList(entry1, entry2);

		when(searchManager.getNewsHeaders(eq(source), eq(count), eq(lastNewsId.toString())))
				.thenReturn(searchResult);

		startAndWaitTopo();

		String execute =
				drpc.execute(NamesUtil.FUN_NAME_GET_NEWS_HEADERS, JSONUtils.serialize(args));
		verify(searchManager, only()).getNewsHeaders(source, count, lastNewsId.toString());

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

	protected void startAndWaitTopo() throws InterruptedException {
		// storm drpc
		drpc = new LocalDRPC();
		// storm cluster
		cluster = new LocalCluster();
		cluster.submitTopology(ServerWebRequestProcessingTopology.TOPOLOGY_NAME, topologyConfig,
				ServerWebRequestProcessingTopology.createTopology(drpc));
		Thread.sleep(1 * 5 * 1_000);
	}

}
