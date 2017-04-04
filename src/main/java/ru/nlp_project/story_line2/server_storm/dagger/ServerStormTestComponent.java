package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.server_storm.bolt.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.functions.NewsHeaderFinderFunction;
import ru.nlp_project.story_line2.server_storm.functions.NewsArticleFinderFunction;
import ru.nlp_project.story_line2.server_storm.functions.JSONConverterFunction;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerNewsArticleReaderSpout;

@Singleton
@Component(modules = {ServerStormTestModule.class})
public abstract class ServerStormTestComponent extends ServerStormComponent {
	
	public abstract void inject(CrawlerNewsArticleReaderSpout instance);

	public abstract void inject(TextProcessingBolt instance);

	public abstract void inject(ElasticsearchIndexingBolt instance);

	public abstract void inject(NewsHeaderFinderFunction instance);

	public abstract void inject(JSONConverterFunction instance);

	public abstract void inject(NewsArticleFinderFunction instance);
}
