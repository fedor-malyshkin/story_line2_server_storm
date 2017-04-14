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
import ru.nlp_project.story_line2.server_storm.impl.ConfigurationManagerImpl;
import ru.nlp_project.story_line2.server_storm.impl.ElasticsearchManagerImpl;
import ru.nlp_project.story_line2.server_storm.impl.GroovyInterpreterImpl;
import ru.nlp_project.story_line2.server_storm.impl.MongoDBClientImpl;
import ru.nlp_project.story_line2.server_storm.impl.TextAnalyserImpl;

@Module
public class ServerStormModule {

	private String configurationUrl = null;
	private MetricRegistry metricRegistry = null;


	public ServerStormModule(String configurationUrl, MetricRegistry metricRegistry) {
		super();
		this.configurationUrl = configurationUrl;
		this.metricRegistry = metricRegistry;
	}


	@Provides
	@Singleton
	public IMongoDBClient provideMongoDBClient(MongoDBClientImpl instance) {
		instance.initialize();
		return instance;
	}


	@Provides
	@Singleton
	public IConfigurationManager provideConfigurationManager() {
		ConfigurationManagerImpl instance = new ConfigurationManagerImpl(configurationUrl);
		instance.initialize();
		return instance;
	}

	@Provides
	@Singleton
	public ITextAnalyser provideGLRParser(TextAnalyserImpl instance) {
		instance.initialize();
		return instance;
	}

	@Provides
	@Singleton
	public ISearchManager provideSearchManager(ElasticsearchManagerImpl instance) {
		instance.initialize();
		return instance;
	}



	@Provides
	@Singleton
	public IGroovyInterpreter provideGroovyInterpreter(GroovyInterpreterImpl instance) {
		instance.initialize();
		return instance;
	}

	@Provides
	@Singleton
	public MetricRegistry provideMetricRegistry() {
		return metricRegistry;
	}

}
