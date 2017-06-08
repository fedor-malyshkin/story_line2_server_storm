package ru.nlp_project.story_line2.server_storm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.mongodb.BasicDBObject;

import static org.junit.Assert.assertEquals;

import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;

public class BSONUtilsTest {

	@Test
	public void testSerialization() {
		String origId = "5882931787a1387bf7a398b6";
		Id id = new Id(origId);
		assertThat(ObjectId.isValid(origId)).isTrue();

		Map<String, Object> article = NewsArticle.newObject();
		NewsArticle.id(article, id);
		NewsArticle.crawlerId(article, id);
		BasicDBObject dbObject = BSONUtils.serialize(article);
		assertThat(dbObject.get("_id").getClass()).isEqualTo(Id.class);
		assertThat(dbObject.get("crawler_id").getClass()).isEqualTo(Id.class);


		assertThat(dbObject.toJson())
				.isEqualTo("{ \"_id\" : { \"$oid\" : \"5882931787a1387bf7a398b6\" }, "
						+ "\"crawler_id\" : { \"$oid\" : \"5882931787a1387bf7a398b6\" } }");
		Id objectId = (Id) dbObject.get("_id");
		assertThat(objectId.getValue()).isEqualTo(origId);
		objectId = (Id) dbObject.get("crawler_id");
		assertThat(objectId.toString()).isEqualTo(origId);
	}

	@Test
	public void testDeserialization() {
		BasicDBObject dbObject = new BasicDBObject();
		ObjectId objectId = new ObjectId(100_0000, 123, (short) 23, 1231231);
		dbObject.put("crawler_id", objectId);
		assertThat(dbObject.get("crawler_id").getClass()).isEqualTo(ObjectId.class);
		
		Map<String, Object> map = BSONUtils.deserialize(dbObject);
		assertThat(map.get("crawler_id").getClass()).isEqualTo(Id.class);
		Id id = (Id) map.get("crawler_id");
		// check assigned
		assertThat(id.getValue()).isEqualTo("000f424000007b001712c97f");
		assertThat(id.getValue()).isEqualTo(objectId.toHexString());
	}

	@Test
	public void testBsonModifications() throws IOException {
		Map<String, Object> article = NewsArticle.newObject();
		NewsArticle.title(article, "some text for title!!");
		NewsArticle.content(article, "some text for content!!");
		NewsArticle.creationDate(article, new Date(100_0000));

		// inserting new '_id' - without serialization
		BasicDBObject dbObject = BSONUtils.serialize(article);
		dbObject.put("url", "add some url");
		ObjectId objectId = new ObjectId(100_0000, 123, (short) 23, 1231231);
		assertEquals(100_0000, objectId.getTimestamp());
		assertEquals("000f424000007b001712c97f", objectId.toHexString());
		dbObject.put("_id", objectId);

		Map<String, Object> article2 = BSONUtils.deserialize(dbObject);
		assertEquals("some text for title!!", NewsArticle.title(article2));
		assertEquals("some text for content!!", NewsArticle.content(article2));
		assertEquals(new Date(100_0000), NewsArticle.creationDate(article2));
		assertEquals("add some url", NewsArticle.url(article2));

		assertEquals("000f424000007b001712c97f", NewsArticle.idString(article2));
		// check new '_id'
		assertThat(NewsArticle.id(article2).getClass()).isEqualTo(Id.class);
		assertThat(NewsArticle.id(article2)).isEqualTo(new Id("000f424000007b001712c97f"));

	}



}
