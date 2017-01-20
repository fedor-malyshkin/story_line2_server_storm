package ru.nlp_project.story_line2.server_storm.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ru.nlp_project.story_line2.server_storm.datamodel.CrawlerNewsArticle;

public class JSONUtilsTest {


	@Test
	public void testJsonDeserialize_Simple() throws IOException {
		String res =
				"ru/nlp_project/story_line2/server_storm/util/JSONUtilsTest.testJsonDeserialize_Simple.json";
		String json = getStringFromCP(res);
		CrawlerNewsArticle result = JSONUtils.deserialize(json, CrawlerNewsArticle.class);
		assertNotNull(result);
		assertEquals("Title!!!", result.title);
		assertEquals("some_domain", result.domain);
		assertEquals(2000, result.creationDate.getTime());
	}

	@Test
	public void testJsonSerialize_Simple() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/util/JSONUtilsTest.testJsonSerialize_Simple.json";
		String val = JSONUtils.serialize(new CrawlerNewsArticle());
		assertEquals(getStringFromCP(expectedString), val);
	}

	private String getStringFromCP(String path) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream);
	}

}
