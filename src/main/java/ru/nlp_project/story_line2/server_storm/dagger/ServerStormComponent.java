package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.server_storm.bolt.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerNewsArticleReaderSpout;

@Singleton
@Component(modules = {ServerStormModule.class})
public abstract class ServerStormComponent {
	// utils
	public abstract void inject(CrawlerNewsArticleReaderSpout instance);

	public abstract void inject(TextProcessingBolt instance);

	public abstract void inject(ElasticsearchIndexingBolt instance);
}
