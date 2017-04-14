package ru.nlp_project.story_line2.server_storm.bolt;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;
import ru.nlp_project.story_line2.server_storm.impl.TestUtils;
import ru.nlp_project.story_line2.server_storm.impl.TestUtils.OutputCollectorStub;
import ru.nlp_project.story_line2.server_storm.impl.TestUtils.TupleStub;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ContentExtractingBoltTest {

	private static ServerStormTestModule serverStormTestModule;
	private static IMongoDBClient mongoDBClient;
	private static IGroovyInterpreter groovyInterpreter;
	private static ContentExtractingBolt testable;
	private OutputCollectorStub outputCollectorHolder;
	private IConfigurationManager configurationManager;



	@Before
	public void setUp() {
		// dagger
		ServerStormBuilder.setTestMode(true);
		serverStormTestModule = ServerStormBuilder.getServerStormTestModule();

		// mocks
		mongoDBClient = mock(IMongoDBClient.class);
		serverStormTestModule.mongoDBClient = mongoDBClient;

		groovyInterpreter = mock(IGroovyInterpreter.class);
		serverStormTestModule.groovyInterpreter = groovyInterpreter;

		configurationManager = mock(IConfigurationManager.class);
		serverStormTestModule.configurationManager = configurationManager;

		Map<String, Object> config = new HashMap<String, Object>();
		testable = new ContentExtractingBolt();

		outputCollectorHolder = new TestUtils.OutputCollectorStub();
		testable.prepare(config, null, outputCollectorHolder);
	}



	@Test
	public void testExecute_EmptyRawContent() throws Exception {
		TupleStub tuple = new TestUtils.TupleStub();
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_ID, "_id");
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_SOURCE, "_source");

		NewsArticle na = new NewsArticle();
		na.crawlerId = new Id("_crawlerId");
		when(mongoDBClient.getNewsArticle(anyString())).thenReturn(na);


		CrawlerEntry ce = new CrawlerEntry();
		ce.rawContent = null;
		when(mongoDBClient.getCrawlerEntry(anyString())).thenReturn(ce);

		// exec
		testable.execute(tuple);

		verify(mongoDBClient).getNewsArticle(anyString());
		verify(mongoDBClient).getCrawlerEntry(anyString());
		verifyZeroInteractions(groovyInterpreter);
	}



	@Test
	public void testExecute_ExistRawContent() throws Exception {
		TupleStub tuple = new TestUtils.TupleStub();
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_ID, "_id");
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_SOURCE, "_source");

		NewsArticle na = new NewsArticle();
		na.crawlerId = new Id("_crawlerId");
		when(mongoDBClient.getNewsArticle(anyString())).thenReturn(na);


		CrawlerEntry ce = new CrawlerEntry();
		ce.rawContent = "some text";
		ce.source = "some source";
		ce.url = "some url";
		when(mongoDBClient.getCrawlerEntry(anyString())).thenReturn(ce);

		Map<String, Object> data  = new HashMap<>();
		when(groovyInterpreter.extractData(anyString(), anyString(), eq("some text"))).thenReturn(data);
		// exec
		testable.execute(tuple);

		verify(mongoDBClient).getNewsArticle(anyString());
		verify(mongoDBClient).getCrawlerEntry(anyString());
		verify(groovyInterpreter).extractData(anyString(), anyString(), eq("some text"));
		
	}
}