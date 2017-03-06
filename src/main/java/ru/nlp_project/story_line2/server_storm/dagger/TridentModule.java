package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.nlp_project.story_line2.server_storm.spout.FeedSupplierImpl;
import ru.nlp_project.story_line2.server_storm.spout.IFeedSupplier;

@Module
public class TridentModule {
	@Singleton
	@Provides
	IFeedSupplier proviceFeedSupplier(FeedSupplierImpl instance) {
		return instance;
	}

}
