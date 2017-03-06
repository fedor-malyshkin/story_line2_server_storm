package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;

/**
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * 
 * @author fedor
 *
 */
@Ignore
public class ElasticsearchManagerImplIntgrTest {

	private static IConfigurationManager configurationManager;
	private ElasticsearchManagerImpl testable;

	@BeforeClass
	public static void setUpClass() throws IOException {
		configurationManager = new ConfigurationManagerImpl();
		configurationManager.initialize();
	}


	@Before
	public void setUp() {
		testable = new ElasticsearchManagerImpl();
		testable.configurationManager = configurationManager;
		testable.initialize();
	}

	// curl -XGET
	// 'localhost:9200/story_line2_read_index/news_article/_search?q=facts.geo.fact_key:GE*'
	@Test
	public void testCheckIndex() throws Exception {
		NewsArticle newsArticle = new NewsArticle();
		List<NewsArticleFact> list = Arrays.asList(new NewsArticleFact(0, "GEO", 2, "Ашхабад"),
				new NewsArticleFact(0, "GEO", 3, "Душанбэ"));
		newsArticle.facts.put("geo", list);
		newsArticle._id = new Id("123");
		testable.index(newsArticle);

	}

}
