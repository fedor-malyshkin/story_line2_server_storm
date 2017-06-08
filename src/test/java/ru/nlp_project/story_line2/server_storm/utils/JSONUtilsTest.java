package ru.nlp_project.story_line2.server_storm.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.Id;

public class JSONUtilsTest {


	@Test
	public void testJsonDeserialize_Simple() throws IOException {
		String res =
				"ru/nlp_project/story_line2/server_storm/utils/JSONUtilsTest.testJsonDeserialize_Simple.json";
		String json = getStringFromCP(res);
		Map<String, Object> result = JSONUtils.deserialize(json);
		assertNotNull(result);
		assertEquals("Title!!!", CrawlerEntry.title(result));
		assertEquals("some_domain", CrawlerEntry.source(result));
		assertEquals(2000, CrawlerEntry.publicationDate(result).getTime());
	}

	@Test
	public void testJsonDeserialize_Simple2() throws IOException {
		String json = "{ \"count\" : 25, \"source\" : \"bnkomi.ru\" }";
		Map<String, Object> result = JSONUtils.deserialize(json);
		assertNotNull(result);
		assertThat(result.get("count")).isEqualTo(25);
		assertThat(result.get("source")).isEqualTo("bnkomi.ru");
	}

	@Test
	public void testJsonSerialize_Simple() throws IOException {
		String expectedString =
				"ru/nlp_project/story_line2/server_storm/utils/JSONUtilsTest.testJsonSerialize_Simple.json";
		String val = JSONUtils.serialize(CrawlerEntry.newObject());
		assertEquals(getStringFromCP(expectedString), val);
	}

	/**
	 * Проверка поведения при сериализации массива с датой как объектом.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJsonSerialize_FieldWithDatas() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("dateField", new Date(1));
		String val = JSONUtils.serialize(map);
		Map<String, Object> newMap = JSONUtils.deserialize(val);
		assertThat(newMap.get("dateField").getClass()).isEqualTo(Date.class);
		assertThat(newMap.get("dateField")).isEqualTo(new Date(1));
	}

	/**
	 * Проверка поведения при сериализации массива с Id как объектом.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJsonSerialize_FieldWithId() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("idField", new Id(ObjectId.createFromLegacyFormat(123, 456, 789).toHexString()));
		String val = JSONUtils.serialize(map);
		Map<String, Object> newMap = JSONUtils.deserialize(val);
		assertThat(newMap.get("idField").getClass()).isEqualTo(Id.class);
		assertThat(newMap.get("idField"))
				.isEqualTo(new Id(ObjectId.createFromLegacyFormat(123, 456, 789).toHexString()));
	}



	/**
	 * Проверка поведения сериализации null значений -- не должно быть в результате (индексация
	 * "_id" в elastic).
	 * 
	 * Не появляется лишь в случае отсуствия клюяа (опции маппера не работают при сериализации
	 * массивов ассоциативных).
	 * 
	 * @throws IOException
	 */
	@Test
	public void testJsonSerialize_NoNullFiledsInResult() throws IOException {
		Map<String, Object> map = new HashMap<>();
		map.put("idField", null);
		map.put("filed01", "123_asdf");
		String json = JSONUtils.serialize(map);
		assertThat(json).contains("\"idField\"");
		map.remove("idField");
		json = JSONUtils.serialize(map);
		assertThat(json).doesNotContain("\"idField\"");
	}


	private String getStringFromCP(String path) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		return IOUtils.toString(resourceAsStream, Charset.defaultCharset());
	}

}
