package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import com.codahale.metrics.MetricRegistry;

import dagger.Module;
import dagger.Provides;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.ITextAnalyser;

@Module
public class ServerStormTestModule {
	private MetricRegistry metricRegistry = null;
	public ISearchManager searchManager;
	public IConfigurationManager configurationManager;
	public ITextAnalyser textAnalyser;
	public IMongoDBClient mongoDBClient;
	public IGroovyInterpreter groovyInterpreter;


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

}
