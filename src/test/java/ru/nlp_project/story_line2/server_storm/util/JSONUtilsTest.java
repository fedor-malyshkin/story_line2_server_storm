package ru.nlp_project.story_line2.server_storm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;

import com.mongodb.BasicDBObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ru.nlp_project.story_line2.server_storm.datamodel.CrawlerNewsArticle;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;

public class JSONUtilsTest {


	@Test
	public void testJsonDeserialize_Simple() throws IOException {
		String res =
				"ru/nlp_project/story_line2/server_storm/util/JSONUtilsTest.testJsonDeserialize_Simple.json";
		String json = getStringFromCP(res);
		CrawlerNewsArticle result = JSONUtils.jsonDeserialize(json, CrawlerNewsArticle.class);
		assertNotNull(result);
		assertEquals("Title!!!", result.title);
		assertEquals("some_domain", result.domain);
	}

	@Test
	public void testJsonSerialize_Simple() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/util/JSONUtilsTest.testJsonSerialize_Simple.json";
		String val = JSONUtils.jsonSerialize(new CrawlerNewsArticle());
		assertEquals(getStringFromCP(expectedString), val);
	}


	@Test
	public void testBsonProcessing() throws IOException {
		NewsArticle article = new NewsArticle();
		article.title = "some text for title!!";
		article.content = "some text for content!!";
		article.creationDate = new Date(100_0000);

		BasicDBObject val = JSONUtils.bsonSerialize(article);
		val.put("url", "add some url");
		ObjectId objectId = new ObjectId(new Date(100_0000));
		assertEquals(1000, objectId.getTimestamp());
		val.put("_id", objectId);

		NewsArticle article2 = JSONUtils.bsonDeserialize(val, NewsArticle.class);
		assertEquals("some text for title!!", article2.title);
		assertEquals("some text for content!!", article2.content);
		assertEquals(new Date(100_0000), article2.creationDate);
		assertEquals(1000, article2._id.getTime());
		assertEquals("add some url", article2.url);
	}

	private String getStringFromCP(String path) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream);
	}

}
