package ru.nlp_project.story_line2.server_storm;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;

public class JSONUtilsTest {



	@Test
	public void testDeserialize() {
		fail("Not yet implemented");
	}

	@Test
	public void testSerialize_Simple() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/JSONUtilsTest.testSerialize_Simple.json";
		String val = JSONUtils.serialize(new NewsArticle());
		assertEquals(getStringFromCP(expectedString), val);
	}

	private Object getStringFromCP(String path) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream);
	}

}
