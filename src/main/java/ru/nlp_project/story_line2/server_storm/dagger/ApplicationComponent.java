package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.server_storm.bolt.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerNewsArticleReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.FeedBatchSpoutType;

@Singleton
@Component(modules = {ApplicationModule.class, TridentModule.class})
public abstract class ApplicationComponent {
	// utils
	public abstract void inject(CrawlerNewsArticleReaderSpout instance);

	public abstract void inject(TextProcessingBolt instance);

	public abstract void inject(ElasticsearchIndexingBolt instance);

	public abstract void inject(FeedBatchSpoutType instance);
}
