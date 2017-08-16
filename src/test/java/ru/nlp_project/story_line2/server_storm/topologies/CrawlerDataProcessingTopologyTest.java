package ru.nlp_project.story_line2.server_storm.topologies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

public class CrawlerDataProcessingTopologyTest {

	private IMongoDBClient mongoDBClient;
	private ISearchManager searchManager;
	private LocalCluster cluster;
	private IGroovyInterpreter groovyInterpreter;
	private IConfigurationManager configurationManager;
	private ITextAnalyser textAnalyser;

	@BeforeClass
	public static void setUpClass() {
		// dagger
		ServerStormBuilder.initializeTestMode();
	}

	private HashMap<String, Object> topologyConfig;

	@Before
	public void setUp() {
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

		// storm
		cluster = new LocalCluster();

		topologyConfig = new HashMap<String, Object>();
		cluster.submitTopology(CrawlerDataProcessingTopology.TOPOLOGY_NAME, topologyConfig,
				CrawlerDataProcessingTopology.createTopology());
	}

	@After
	public void tearDown() throws InterruptedException {
		KillOptions options = new KillOptions();
		options.set_wait_secs(20);
		cluster.killTopologyWithOpts(CrawlerDataProcessingTopology.TOPOLOGY_NAME, options);
		cluster.shutdown();
	}


	@Test
	// успешное, стандартное прохождение пакета через топологию
	public void testSuccessfullPass() throws Exception {
		// запросить CE
		String newsArticleId = "fake-newArticleId";
		String crawlerEntryId = "fake-crawlerEntryId";
		String source = "some source";
		String crawlerEntryRawValue = "some raw content";
		Map<String, Object> unprocessedCrawlerEntry = createUnprocessedCrawlerEntry();

		CrawlerEntry.rawContent(unprocessedCrawlerEntry, crawlerEntryRawValue);
		CrawlerEntry.source(unprocessedCrawlerEntry, source);
		when(mongoDBClient.getNextUnprocessedCrawlerEntry()).thenReturn(unprocessedCrawlerEntry);
		// создать на его базе NA
		when(mongoDBClient.upsertNewsArticleByCrawlerEntry(anyMap())).thenReturn(newsArticleId);
		Map<String, Object> newsArticle = createNewsArticle(newsArticleId, crawlerEntryId);
		// обработка
		// получить NA по идентификатору
		when(mongoDBClient.getNewsArticle(anyString())).thenReturn(newsArticle);
		// получить CE для извлечения данных
		when(mongoDBClient.getCrawlerEntry(eq(crawlerEntryId))).thenReturn(unprocessedCrawlerEntry);
		// получить из CE даннык
		Map<String, Object> extractedData = createExtractedData();
		when(groovyInterpreter.extractData(eq(source), any(), eq(crawlerEntryRawValue)))
				.thenReturn(extractedData);

		Thread.sleep(1 * 5 * 1_000);
		verify(mongoDBClient, atLeast(1))
				.updateNewsArticle(argThat(new ArgumentMatcher<Map<String, Object>>() {
					public boolean matches(Map<String, Object> argument) {
						if (!NewsArticle.content(argument).equalsIgnoreCase("EXTR_KEY_CONTENT"))
							return false;
						if (!NewsArticle.title(argument).equalsIgnoreCase("EXTR_KEY_TITLE"))
							return false;
						Date date = new Date(1);
						if (!NewsArticle.publicationDate(argument).equals(date))
							return false;
						return true;
					}
				}));
	}

	@Test
	// прохождение через топологию, где нет контента
	public void testNoRawContent() throws Exception {
		// запросить CE
		String newsArticleId = "fake-newArticleId";
		String crawlerEntryId = "fake-crawlerEntryId";
		String source = "some source";
		String crawlerEntryRawValue = "";
		Map<String, Object> unprocessedCrawlerEntry = createUnprocessedCrawlerEntry();

		CrawlerEntry.rawContent(unprocessedCrawlerEntry, crawlerEntryRawValue);
		CrawlerEntry.source(unprocessedCrawlerEntry, source);
		when(mongoDBClient.getNextUnprocessedCrawlerEntry()).thenReturn(unprocessedCrawlerEntry);
		// создать на его базе NA
		when(mongoDBClient.upsertNewsArticleByCrawlerEntry(anyMap())).thenReturn(newsArticleId);
		Map<String, Object> newsArticle = createNewsArticle(newsArticleId, crawlerEntryId);
		// обработка
		// получить NA по идентификатору
		when(mongoDBClient.getNewsArticle(anyString())).thenReturn(newsArticle);
		// получить CE для извлечения данных
		when(mongoDBClient.getCrawlerEntry(eq(crawlerEntryId))).thenReturn(unprocessedCrawlerEntry);
		// получить из CE даннык
		Map<String, Object> extractedData = createExtractedData();
		when(groovyInterpreter.extractData(eq(source), any(), eq(crawlerEntryRawValue)))
				.thenReturn(extractedData);

		Thread.sleep(1 * 5 * 1_000);
		verify(mongoDBClient, never()).updateNewsArticle(any());
	}

	private Map<String, Object> createExtractedData() {
		Map<String, Object> result = new HashMap<>();
		result.put(IGroovyInterpreter.EXTR_KEY_CONTENT, "EXTR_KEY_CONTENT");
		result.put(IGroovyInterpreter.EXTR_KEY_IMAGE_URL, "EXTR_KEY_IMAGE_URL");
		result.put(IGroovyInterpreter.EXTR_KEY_PUB_DATE, new Date(1));
		result.put(IGroovyInterpreter.EXTR_KEY_TITLE, "EXTR_KEY_TITLE");
		return result;
	}

	private Map<String, Object> createUnprocessedCrawlerEntry() {
		Map<String, Object> result = CrawlerEntry.newObject();
		CrawlerEntry.id(result, new Id("CrawlerEntry-fake-" + System.currentTimeMillis()));
		return result;
	}

	private Map<String, Object> createNewsArticle(String articleId, String crawlerId) {
		Map<String, Object> result = NewsArticle.newObject();
		NewsArticle.id(result, new Id(articleId));
		NewsArticle.crawlerId(result, new Id(crawlerId));
		return result;
	}

}
