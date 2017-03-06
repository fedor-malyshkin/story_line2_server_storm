package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
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
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;

public class ElasticsearchManagerImpl implements ISearchManager {


	private static final String REQUEST_METHOD_PUT = "PUT";
	private static final String REQUST_METHOD_HEAD = "HEAD";
	private static final int RESPONSE_SUCCESS = 200;
	private static final String CP_TEMPLATE_CREATE_INDEX = "elasticsearchCreateIndex.json";
	private static final String INDEX_TYPE_NEWS_ARTICLE = "news_article";
	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private boolean initialized = false;
	private RestClient restClient;
	private String readIndex;
	private String writeIndex;

	@Inject
	public ElasticsearchManagerImpl() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	private void createIndex(String indexName) throws IOException {
		String template = getTemplateFromClasspath(CP_TEMPLATE_CREATE_INDEX);
		template = String.format(template, INDEX_TYPE_NEWS_ARTICLE);
		HttpEntity entity = new NStringEntity(template, ContentType.APPLICATION_JSON);
		restClient.performRequest(REQUEST_METHOD_PUT,  "/"+indexName,
				Collections.<String, String>emptyMap(), entity);


	}

	private void createIndexAlias(String aliasName, String realName) throws IOException {
		// PUT /logs_201305/_alias/2013
		restClient.performRequest(REQUEST_METHOD_PUT,
				String.format("/%s/_alias/%s", realName, aliasName),
				Collections.<String, String>emptyMap());
	}


	/**
	 * Загружить текстовый шаблон из classpath'а.
	 * 
	 * @param classpath
	 * @return
	 * @throws IOException
	 */
	private String getTemplateFromClasspath(String classpath) throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("ru/nlp_project/story_line2/server_storm/impl/" + classpath);
		return IOUtils.toString(stream);
	}

	@Override
	public void index(NewsArticle newsArticle) throws Exception {
		if (newsArticle._id == null || newsArticle._id.value == null)
			throw new IllegalArgumentException("id in object must be set to index it.");
		initilizeIfNecessary();

		String endpoint =
				String.format("/%s/%s/%s", writeIndex, INDEX_TYPE_NEWS_ARTICLE, newsArticle._id);
		// set id to null to escape exception from ES "is a metadata field and cannot be added
		// inside a document. Use the index API request parameters."
		Id _id = newsArticle._id;
		newsArticle._id = null;
		String json = JSONUtils.serialize(newsArticle);
		newsArticle._id = _id;
		HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
		Response indexResponse = restClient.performRequest(REQUEST_METHOD_PUT, endpoint,
				Collections.<String, String>emptyMap(), entity);
		String response = indexResponse.getEntity().toString();
		Header[] headers = indexResponse.getHeaders();
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

	private void initilizeIfNecessary() {
		if (!initialized)
			initialize();
	}

	private boolean isIndexExists(String indexName) throws IOException {
		Response response = restClient.performRequest(REQUST_METHOD_HEAD, indexName,
				Collections.<String, String>emptyMap());
		if (response.getStatusLine().getStatusCode() != RESPONSE_SUCCESS)
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



}
