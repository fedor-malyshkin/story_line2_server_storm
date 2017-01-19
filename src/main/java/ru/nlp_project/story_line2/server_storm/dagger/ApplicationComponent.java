package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.server_storm.bolts.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolts.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spouts.CrawlerNewsArticleReaderSpout;

@Component(modules = ApplicationModule.class)
@Singleton
public abstract class ApplicationComponent {
	public abstract void inject(CrawlerNewsArticleReaderSpout instance);

	public abstract void inject(TextProcessingBolt instance);

	public abstract void inject(ElasticsearchIndexingBolt instance);

}
