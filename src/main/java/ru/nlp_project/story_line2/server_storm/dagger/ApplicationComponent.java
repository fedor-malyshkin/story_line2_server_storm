package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;
import ru.nlp_project.story_line2.server_storm.spouts.CrawlerNewsArticleReaderSpout;

@Component(modules = ApplicationModule.class)
@Singleton
public abstract class ApplicationComponent {
	public abstract void inject(CrawlerNewsArticleReaderSpout instance);

}
