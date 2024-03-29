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
import ru.nlp_project.story_line2.server_storm.impl.ConfigurationManagerImpl;
import ru.nlp_project.story_line2.server_storm.impl.ElasticsearchManagerImpl;
import ru.nlp_project.story_line2.server_storm.impl.GroovyInterpreterImpl;
import ru.nlp_project.story_line2.server_storm.impl.ImageDownloaderImpl;
import ru.nlp_project.story_line2.server_storm.impl.MetricsManagerImpl;
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
	public IMongoDBClient provideMongoDBClient(IConfigurationManager configurationManager) {
		MongoDBClientImpl instance = new MongoDBClientImpl(configurationManager);
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
	public ITextAnalyser provideGLRParser() {
		TextAnalyserImpl instance = new TextAnalyserImpl();
		instance.initialize();
		return instance;
	}


	@Provides
	@Singleton
	public IImageDownloader provideImageDownloader() {
		ImageDownloaderImpl instance = new ImageDownloaderImpl();
		instance.initialize();
		return instance;
	}

	@Provides
	@Singleton
	public ISearchManager provideSearchManager(IConfigurationManager configurationManager) {
		ElasticsearchManagerImpl instance = new ElasticsearchManagerImpl(configurationManager);
		instance.initialize();
		return instance;
	}


	@Provides
	@Singleton
	public IGroovyInterpreter provideGroovyInterpreter(IConfigurationManager configurationManager) {
		GroovyInterpreterImpl instance = new GroovyInterpreterImpl(configurationManager);
		instance.initialize();
		return instance;
	}

	@Provides
	@Singleton
	public MetricRegistry provideMetricRegistry() {
		return metricRegistry;
	}

	@Provides
	@Singleton
	public IMetricsManager provideMetricsManager(IConfigurationManager configurationManager) {
		MetricsManagerImpl instance = new MetricsManagerImpl(configurationManager, metricRegistry);
		instance.initialize();
		return instance;
	}


}
