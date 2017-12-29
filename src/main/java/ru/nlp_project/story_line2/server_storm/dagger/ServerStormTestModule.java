package ru.nlp_project.story_line2.server_storm.dagger;

import com.codahale.metrics.MetricRegistry;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IImageDownloader;
import ru.nlp_project.story_line2.server_storm.IMetricsManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.ITextAnalyser;

@Module
public class ServerStormTestModule {

	public ISearchManager searchManager;
	public IConfigurationManager configurationManager;
	public ITextAnalyser textAnalyser;
	public IMongoDBClient mongoDBClient;
	public IGroovyInterpreter groovyInterpreter;
	public IImageDownloader imageDownloader;
	public IMetricsManager metricsManager;
	private MetricRegistry metricRegistry = null;


	public ServerStormTestModule(MetricRegistry metricRegistry) {
		super();
		this.metricRegistry = metricRegistry;
	}


	@Provides
	@Singleton
	public IMongoDBClient provideMongoDBClient() {
		return mongoDBClient;
	}


	@Provides
	@Singleton
	IConfigurationManager provideConfigurationManager() {
		return configurationManager;
	}

	@Provides
	@Singleton
	public ITextAnalyser provideGLRParser() {
		return textAnalyser;
	}

	@Provides
	@Singleton
	public ISearchManager provideSearchManager() {
		return searchManager;
	}

	@Provides
	@Singleton
	public MetricRegistry provideMetricRegistry() {
		return metricRegistry;
	}

	@Provides
	@Singleton
	public IGroovyInterpreter provideGroovyInterpreter() {
		return groovyInterpreter;
	}

	@Provides
	@Singleton
	public IImageDownloader provideImageDownloader() {
		return imageDownloader;
	}


	@Provides
	@Singleton
	public IMetricsManager provideMetricsManager() {
		return metricsManager;
	}

}
