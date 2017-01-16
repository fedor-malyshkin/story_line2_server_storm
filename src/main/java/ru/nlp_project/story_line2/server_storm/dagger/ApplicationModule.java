package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.nlp_project.story_line2.server_storm.ConfigurationManagerImpl;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.MongoDBClientImpl;

@Module
public class ApplicationModule {
	@Provides
	@Singleton
	public IMongoDBClient provideMongoDBClient(MongoDBClientImpl instance) {
		instance.initialize();
		return instance;
	}


	@Provides
	@Singleton
	IConfigurationManager provideConfigurationManager() {
		ConfigurationManagerImpl instance = new ConfigurationManagerImpl();
		instance.initialize();
		return instance;
	}

}
