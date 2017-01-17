package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGLRParser;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.impl.ConfigurationManagerImpl;
import ru.nlp_project.story_line2.server_storm.impl.GLRParserImpl;
import ru.nlp_project.story_line2.server_storm.impl.MongoDBClientImpl;

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

	@Provides
	@Singleton
	public IGLRParser provideGLRParser(GLRParserImpl instance) {
		instance.initialize();
		return instance;
	}


}
