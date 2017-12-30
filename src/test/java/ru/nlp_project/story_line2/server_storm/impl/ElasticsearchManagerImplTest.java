package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class ElasticsearchManagerImplTest {

	private ElasticsearchManagerImpl testable;

	@Before
	public void setUp() {
		testable = new ElasticsearchManagerImpl();
		testable.writeIndex = "story_line2_write_index";
		testable.readIndex = "story_line2_read_index";
	}

	@Test
	public void testExtractResults() throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"ru/nlp_project/story_line2/server_storm/impl/searchResults.json");
		String string = IOUtils.toString(stream, Charset.defaultCharset());
		List<Map<String, Object>> results = testable.extractResults(string);

		Assertions.assertThat(results.size()).isEqualTo(1);
		Map<String, Object> map = results.get(0);
		Assertions.assertThat(map.get("path")).isEqualTo("/data/news/60544/");
	}

	@Test
	public void testExtractCount() throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"ru/nlp_project/story_line2/server_storm/impl/searchResults_Count.json");
		String string = IOUtils.toString(stream, Charset.defaultCharset());
		long count = testable.extractCount(string);
		Assertions.assertThat(count).isEqualTo(123);
	}


	@Test
	public void testFormatSearchEndpoint() {
		String endpoint = testable.formatSearchEndpoint();
		Assertions.assertThat(endpoint).isEqualTo("/story_line2_read_index/news_article/_search");
	}

	@Test
	public void testGetStringFromClasspath() {
		String res = testable.getStringFromClasspath("searchTemplate_getNewsHeaders.json");
		Assertions.assertThat(res).startsWith("{");
	}


	@Test(expected = IllegalStateException.class)
	public void testGetStringFromClasspath_WrongTemplate() {
		testable.getStringFromClasspath("XXXX.json");
	}


	@Test
	public void testFillTemplate() {
		Map<String, Object> subst = new HashMap<>();
		subst.put("source", "bnk");
		String str = testable.fillTemplate("----${source}----", subst);
		Assertions.assertThat(str).isEqualTo("----bnk----");
	}


}
