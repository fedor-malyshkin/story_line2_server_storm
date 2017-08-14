package ru.nlp_project.story_line2.server_storm.topologies;

import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.apache.storm.LocalCluster;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;

public class CrawlerDataProcessingTopologyTest {

	private static ServerStormTestModule serverStormTestModule;
	private static IMongoDBClient mongoDBClient;
	private static ISearchManager searchManager;
	private static LocalCluster cluster;

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
		cluster = new LocalCluster();
	}

	@AfterClass
	public static void tearDownClass() {
		cluster.shutdown();
	}

	private HashMap<String, Object> topologyConfig;

	@Before
	public void setUp() {
		topologyConfig = new HashMap<String, Object>();
		cluster.submitTopology(CrawlerDataProcessingTopology.TOPOLOGY_NAME, topologyConfig,
				CrawlerDataProcessingTopology.createTopology());
	}

	@After
	public void tearDown() {
		cluster.killTopology(CrawlerDataProcessingTopology.TOPOLOGY_NAME);
	}

	@Test
	public void testSuccessfullPass() throws InterruptedException {


	}

}
