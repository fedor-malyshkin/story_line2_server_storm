package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager.MasterConfiguration;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;

public class ElasticsearchManagerImpl implements ISearchManager {


	@Inject
	public IConfigurationManager configurationManager;
	private Logger logger;
	private boolean initialized = false;
	private RestClient restClient;

	@Inject
	public ElasticsearchManagerImpl() {}

	public void initialize() {
		MasterConfiguration configuration = configurationManager.getMasterConfiguration();
		try {
			restClient = RestClient.builder(new HttpHost(configuration.elasticsearchHostName,
					configuration.elasticsearchPort, "http")).build();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			initialized = true;
			restClient = null;
		}
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
		Response indexResponse = restClient.performRequest("PUT", "/twitter/tweet/1",
				Collections.<String, String>emptyMap(), entity);
	}

	private void initilizeIfNecessary() {
		if (!initialized)
			initialize();
	}



}
