package ru.nlp_project.story_line2.server_storm.util;

import java.io.IOException;
import java.util.Date;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.mongodb.BasicDBObject;

import static org.junit.Assert.*;

import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;

public class BSONUtilsTest {



	@Test
	public void testBsonProcessing() throws IOException {
		NewsArticle article = new NewsArticle();
		article.title = "some text for title!!";
		article.content = "some text for content!!";
		article.creationDate = new Date(100_0000);

		BasicDBObject val = BSONUtils.serialize(article);
		val.put("url", "add some url");
		ObjectId objectId = new ObjectId(100_0000, 123, (short) 23, 1231231);
		assertEquals(100_0000, objectId.getTimestamp());
		assertEquals("000f424000007b001712c97f", objectId.toHexString());
		val.put("_id", objectId);

		NewsArticle article2 = BSONUtils.deserialize(val, NewsArticle.class);
		assertEquals("some text for title!!", article2.title);
		assertEquals("some text for content!!", article2.content);
		assertEquals(new Date(100_0000), article2.creationDate);
		assertEquals("000f424000007b001712c97f", article2._id.value);
		assertEquals(100_0000, BSONUtils.createBsonObjectId(article2._id).getTimestamp());
		assertEquals("add some url", article2.url);
	}


}
