package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
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
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;

public class ElasticsearchManagerImpl implements ISearchManager {


	private static final String REQUEST_METHOD_PUT = "PUT";
	private static final String REQUST_METHOD_HEAD = "HEAD";
	private static final int RESPONSE_SUCCESS = 200;
	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private boolean initialized = false;
	private RestClient restClient;

	@Inject
	public ElasticsearchManagerImpl() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	public void initialize() {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		try {
			restClient = RestClient.builder(new HttpHost(configuration.elasticsearchHostName,
					configuration.elasticsearchPort, "http")).build();
			initializeIndex();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			initialized = true;
			restClient = null;
		}
	}

	/**
	 * Загружить текстовый шаблон из classpath'а.
	 * 
	 * @param classpath
	 * @return
	 * @throws IOException
	 */
	private String getTemplateFromClasspath(String classpath) throws IOException {
		InputStream stream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
		return IOUtils.toString(stream);
	}


	private void initializeIndex() throws IOException {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		if (isIndexExists(configuration.elasticsearchRealIndex))
			return;
		createIndex(configuration.elasticsearchRealIndex);
		initMapping(configuration.elasticsearchRealIndex);
		createIndexAlias(configuration.elasticsearchReadAlias,
				configuration.elasticsearchRealIndex);
		createIndexAlias(configuration.elasticsearchWriteAlias,
				configuration.elasticsearchRealIndex);
	}

	private void createIndexAlias(String aliasName, String realName) {
		// TODO Auto-generated method stub

	}

	private void initMapping(String indexName) {
		// TODO Auto-generated method stub

	}

	private void createIndex(String indexName) {
		// TODO Auto-generated method stub

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

	@Override
	public void index(NewsArticle newsArticle) throws Exception {
		initilizeIfNecessary();
		HttpEntity entity = new NStringEntity(
				"{\n" + "    \"user\" : \"kimchy\",\n"
						+ "    \"post_date\" : \"2009-11-15T14:12:12\",\n"
						+ "    \"message\" : \"trying out Elasticsearch\"\n" + "}",
				ContentType.APPLICATION_JSON);
		Response indexResponse = restClient.performRequest(REQUEST_METHOD_PUT, "/twitter/tweet/1",
				Collections.<String, String>emptyMap(), entity);
	}

	private void initilizeIfNecessary() {
		if (!initialized)
			initialize();
	}



}
