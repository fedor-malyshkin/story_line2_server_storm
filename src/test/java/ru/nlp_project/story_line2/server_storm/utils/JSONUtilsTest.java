package ru.nlp_project.story_line2.server_storm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;

public class JSONUtilsTest {


	@Test
	public void testJsonDeserialize_Simple() throws IOException {
		String res =
				"ru/nlp_project/story_line2/server_storm/utils/JSONUtilsTest.testJsonDeserialize_Simple.json";
		String json = getStringFromCP(res);
		CrawlerEntry result = JSONUtils.deserialize(json, CrawlerEntry.class);
		assertNotNull(result);
		assertEquals("Title!!!", result.title);
		assertEquals("some_domain", result.source);
		assertEquals(2000, result.publicationDate.getTime());
	}

	@Test
	public void testJsonSerialize_Simple() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/utils/JSONUtilsTest.testJsonSerialize_Simple.json";
		String val = JSONUtils.serialize(new CrawlerEntry());
		assertEquals(getStringFromCP(expectedString), val);
	}


	/**
	 * @throws IOException
	 */
	@Test
	public void testJsonSerialize_Complex() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/utils/JSONUtilsTest.testJsonSerialize_Complex.json";
		NewsArticle newsArticle = new NewsArticle();

		List<NewsArticleFact> list = Arrays.asList(new NewsArticleFact(0, "GEO", 2, "Ашхабад"),
				new NewsArticleFact(0, "GEO", 3, "Душанбэ"));
		newsArticle.facts.put("geo", list);
		newsArticle._id = new Id("123");

		String val = JSONUtils.serialize(newsArticle);

		assertEquals(getStringFromCP(expectedString), val);
	}

	private String getStringFromCP(String path) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream, Charset.defaultCharset());
	}

}
