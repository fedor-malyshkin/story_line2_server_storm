package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager.MasterConfiguration;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;

public class ElasticsearchManagerImpl implements ISearchManager {

	private static final String CLASSPATH_PREFIX = "ru/nlp_project/story_line2/server_storm/impl/";
	private static final String REQUEST_METHOD_PUT = "PUT";
	private static final String REQUEST_METHOD_GET = "GET";
	private static final String REQUST_METHOD_HEAD = "HEAD";
	private static final int RESPONSE_SUCCESS = 200;
	private static final String CP_INDEX_MAPPING = "indexMapping.json";
	private static final String INDEX_TYPE_NEWS_ARTICLE = "news_article";
	private static final Charset UTF8 = Charset.forName("UTF-8");

	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private boolean initialized = false;
	private RestClient restClient;
	protected String readIndex;
	protected String writeIndex;

	@Inject
	public ElasticsearchManagerImpl() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	private void createIndex(String indexName) throws IOException {
		String template = getStringFromClasspath(CP_INDEX_MAPPING);
		template = String.format(template, INDEX_TYPE_NEWS_ARTICLE);
		HttpEntity entity = new NStringEntity(template, ContentType.APPLICATION_JSON);
		restClient.performRequest(REQUEST_METHOD_PUT, "/" + indexName,
				Collections.<String, String>emptyMap(), entity);
	}

	private void createIndexAlias(String aliasName, String realName) throws IOException {
		// PUT /logs_201305/_alias/2013
		restClient.performRequest(REQUEST_METHOD_PUT,
				String.format("/%s/_alias/%s", realName, aliasName),
				Collections.<String, String>emptyMap());
	}


	@Override
	public void indexNewsArticle(Map<String, Object> newsArticle) throws Exception {
		if (NewsArticle.id(newsArticle) == null)
			throw new IllegalArgumentException("id in object must be set to index it.");

		RestClient elClient = getRestClient();
		String endpoint = String.format("/%s/%s/%s", writeIndex, INDEX_TYPE_NEWS_ARTICLE,
				NewsArticle.idString(newsArticle));
		// set id to null to escape exception from ES "is a metadata field and cannot be added
		// inside a document. Use the index API request parameters."
		Id _id = NewsArticle.id(newsArticle);
		NewsArticle.id(newsArticle, null);
		String json = JSONUtils.serialize(newsArticle);
		NewsArticle.id(newsArticle, _id);
		HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
		// PUT /writeIndex/INDEX_TYPE_NEWS_ARTICLE/ID
		elClient.performRequest(REQUEST_METHOD_PUT, endpoint,
				Collections.<String, String>emptyMap(), entity);
		// String response = indexResponse.getEntity().toString();
		// Header[] headers = indexResponse.getHeaders();
	}

	private RestClient getRestClient() {
		if (!initialized)
			initialize();
		return restClient;
	}

	public void initialize() {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		readIndex = configurationManager.getMasterConfiguration().elasticsearchReadAlias;
		writeIndex = configurationManager.getMasterConfiguration().elasticsearchWriteAlias;
		try {
			restClient = RestClient.builder(new HttpHost(configuration.elasticsearchHostName,
					configuration.elasticsearchPort, "http")).build();
			initializeIndex();
			initialized = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			initialized = false;
			restClient = null;
		}
	}

	private void initializeIndex() throws IOException {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		if (isIndexExists(configuration.elasticsearchRealIndex))
			return;
		createIndex(configuration.elasticsearchRealIndex);
		createIndexAlias(configuration.elasticsearchReadAlias,
				configuration.elasticsearchRealIndex);
		createIndexAlias(configuration.elasticsearchWriteAlias,
				configuration.elasticsearchRealIndex);
	}

	private boolean isIndexExists(String indexName) throws IOException {
		// HEAD /index
		Response response = restClient.performRequest(REQUST_METHOD_HEAD, indexName,
				Collections.<String, String>emptyMap());
		if (getCode(response) != RESPONSE_SUCCESS)
			return false;
		return true;
	}

	@Override
	public void shutdown() {
		if (restClient != null)
			try {
				restClient.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

	}



	@Override
	public List<Map<String, Object>> getNewsHeaders(String source, int count) {
		String template = getStringFromClasspath("searchTemplate_getNewsHeaders.json");
		Map<String, Object> subst = new HashMap<>();
		subst.put("source", source);
		String requestData = fillTemplate(template, subst);

		Response response;
		try {
			response = searchNewsArticle(requestData, count, null);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Exception while search news headers: " + e.getMessage());
		}
		if (getCode(response) == 200)
			return extractResults(getContent(response));
		else
			throw new IllegalStateException("Unexpected response: " + response);
	}

	protected String fillTemplate(String template, Map<String, Object> subst) {
		StrSubstitutor sub = new StrSubstitutor(subst);
		return sub.replace(template);
	}

	protected String getStringFromClasspath(String classpath) {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(CLASSPATH_PREFIX + classpath);
		if (stream == null)
			throw new IllegalStateException("Illegal classpath: " + CLASSPATH_PREFIX + classpath);
		try {
			return IOUtils.toString(stream, UTF8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected String getContent(Response response) {
		InputStream stream = null;
		try {
			stream = response.getEntity().getContent();
			return IOUtils.toString(stream, UTF8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		} finally {
			if (stream != null)
				IOUtils.closeQuietly(stream);
		}
	}

	protected int getCode(Response response) {
		return response.getStatusLine().getStatusCode();
	}

	protected Response searchNewsArticle(String requestData, Integer size, Integer timeout)
			throws IOException {
		RestClient elClient = getRestClient();
		String endpoint = formatSearchEndpoint();
		Map<String, String> params = formatSearchParams(size, timeout);
		NStringEntity entity = new NStringEntity(requestData, ContentType.APPLICATION_JSON);
		// PUT /writeIndex/newArticle/ID
		return elClient.performRequest(REQUEST_METHOD_GET, endpoint, params, entity);
	}

	private Map<String, String> formatSearchParams(Integer size, Integer timeout) {
		if (size == null && timeout == null)
			return Collections.emptyMap();
		Map<String, String> result = new HashMap<>();
		if (size != null)
			result.put("size", size.toString());
		if (timeout != null)
			result.put("timeout", timeout.toString() + "ms");
		return result;
	}

	protected String formatSearchEndpoint() {
		return String.format("/%s/%s/_search", readIndex, INDEX_TYPE_NEWS_ARTICLE);
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> extractResults(String json) {
		List<Map<String, Object>> result = new ArrayList<>();


		Map<String, Object> map = JSONUtils.deserialize(json);
		Map<String, Object> hits1 = (Map<String, Object>) map.get("hits");
		int total = (int) hits1.get("total");
		if (total == 0)
			return Collections.emptyList();
		Collection<Map<String, Object>> hits2 = (Collection<Map<String, Object>>) hits1.get("hits");

		for (Map<String, Object> entry : hits2) {
			Map<String, Object> arrEntry = (Map<String, Object>) entry.get("_source");
			String id = (String) entry.get("_id");
			arrEntry.put("_id", id);
			result.add(arrEntry);
		}
		return result;

	}

	@Override
	public List<Map<String, Object>> getNewsArticle(String id) {
		String template = getStringFromClasspath("searchTemplate_getNewsArticle.json");
		Map<String, Object> subst = new HashMap<>();
		subst.put("id", id);
		String requestData = fillTemplate(template, subst);

		Response response;
		try {
			response = searchNewsArticle(requestData, 1, null);
		} catch (IOException e) {
			throw new IllegalStateException(
					"Exception while search news headers: " + e.getMessage());
		}
		if (getCode(response) == 200)
			return extractResults(getContent(response));
		else
			throw new IllegalStateException("Unexpected response: " + response);
	}



}
