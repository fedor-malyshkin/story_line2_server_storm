package ru.nlp_project.story_line2.server_storm.topologies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.KillOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.ITextAnalyser;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MaintainceTopologyTest {

	private static IMongoDBClient mongoDBClient;
	private static ISearchManager searchManager;
	private static IGroovyInterpreter groovyInterpreter;
	private static IConfigurationManager configurationManager;
	private static ITextAnalyser textAnalyser;
	private LocalCluster cluster;
	private HashMap<String, Object> topologyConfig;

	@BeforeClass
	public static void setUpClass() {
		// dagger
		ServerStormBuilder.initializeTestMode();
		ServerStormTestModule serverStormTestModule = ServerStormBuilder.getServerStormTestModule();
		// mocks
		mongoDBClient = mock(IMongoDBClient.class);
		serverStormTestModule.mongoDBClient = mongoDBClient;
		searchManager = mock(ISearchManager.class);
		serverStormTestModule.searchManager = searchManager;
		configurationManager = mock(IConfigurationManager.class);
		serverStormTestModule.configurationManager = configurationManager;
		groovyInterpreter = mock(IGroovyInterpreter.class);
		serverStormTestModule.groovyInterpreter = groovyInterpreter;
		textAnalyser = mock(ITextAnalyser.class);
		serverStormTestModule.textAnalyser = textAnalyser;
	}

	@Before
	public void setUp() {
		reset(mongoDBClient);
		reset(searchManager);
		reset(configurationManager);
		reset(groovyInterpreter);
		reset(textAnalyser);
	}

	@After
	public void tearDown() throws InterruptedException {
		KillOptions options = new KillOptions();
		options.set_wait_secs(20);
		cluster.killTopologyWithOpts(MaintainceTopology.TOPOLOGY_NAME, options);
		cluster.shutdown();
	}


	@Test
	// успешное, стандартное прохождение пакета через топологию
	public void testSuccessfullPass_ArchiveCrawlerEntry() throws Exception {
		// запросить CE
		String crawlerEntryId = "fake-crawlerEntryId";
		String source = "some source";
		String crawlerEntryRawValue = "some raw content";
		Map<String, Object> unprocessedCrawlerEntry = createUnprocessedCrawlerEntry(crawlerEntryId);
		CrawlerEntry.rawContent(unprocessedCrawlerEntry, crawlerEntryRawValue);
		CrawlerEntry.source(unprocessedCrawlerEntry, source);

		when(mongoDBClient.getNextUnpurgedImagesNewsArticle(any(Date.class)))
				.thenReturn(null);
		when(mongoDBClient.getNextMaintenanceCommandEntry()).thenReturn(null);
		when(mongoDBClient.getNextUnarchivedCrawlerEntry(any(Date.class)))
				.thenReturn(unprocessedCrawlerEntry);
		when(mongoDBClient.getCrawlerEntry(startsWith(crawlerEntryId)))
				.thenReturn(unprocessedCrawlerEntry);

		startAndWaitTopo();

		verify(mongoDBClient, atLeast(1))
				.updateCrawlerEntry((argThat(new ArgumentMatcher<Map<String, Object>>() {
					public boolean matches(Map<String, Object> argument) {
						if (CrawlerEntry.rawContent(argument) != null) {
							return false;
						}
						return (Boolean) argument
								.get(NamesUtil.CRAWLER_ENTRY_FIELD_NAME_ARCHIVED);
					}
				})));
	}


	@Test
	// успешное, стандартное прохождение пакета через топологию
	public void testSuccessfullPass_PurgeNewsArticleImagesData() throws Exception {
		String unpurgedId = "fake-newsArticleId";
		Map<String, Object> unpurgedNewsArticle = createUnpurgedNewsArticle(unpurgedId);

		when(mongoDBClient.getNextUnpurgedImagesNewsArticle(any(Date.class)))
				.thenReturn(unpurgedNewsArticle);
		when(mongoDBClient.getNextUnarchivedCrawlerEntry(any(Date.class)))
				.thenReturn(null);
		when(mongoDBClient.getNextMaintenanceCommandEntry()).thenReturn(null);
		when(mongoDBClient.getNewsArticle(startsWith(unpurgedId))).thenReturn(unpurgedNewsArticle);

		startAndWaitTopo();

		verify(mongoDBClient, atLeastOnce())
				.updateNewsArticle((argThat(new ArgumentMatcher<Map<String, Object>>() {
					public boolean matches(Map<String, Object> argument) {
						if (NewsArticle.imageData(argument) == new byte[]{}) {
							return false;
						}
						return (Boolean) argument
								.get(NamesUtil.NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED);
					}
				})));
		verify(searchManager, atLeastOnce()).updateNewsArticle(any(Map.class));
	}


	protected void startAndWaitTopo() throws InterruptedException {
		// storm
		cluster = new LocalCluster();
		topologyConfig = new HashMap<String, Object>();
		cluster.submitTopology(MaintainceTopology.TOPOLOGY_NAME, topologyConfig,
				MaintainceTopology.createTopology());
		Thread.sleep(1 * 5 * 1_000);
	}


	private Map<String, Object> createUnprocessedCrawlerEntry(String id) {
		Map<String, Object> result = CrawlerEntry.newObject();
		CrawlerEntry.id(result, new Id(id + System.currentTimeMillis()));
		return result;
	}


	private Map<String, Object> createUnpurgedNewsArticle(String id) {
		Map<String, Object> result = NewsArticle.newObject();
		NewsArticle.id(result, new Id(id + +System.currentTimeMillis()));
		return result;
	}


}
