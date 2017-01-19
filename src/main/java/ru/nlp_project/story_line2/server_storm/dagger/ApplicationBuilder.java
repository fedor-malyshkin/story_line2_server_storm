package ru.nlp_project.story_line2.server_storm.dagger;

import ru.nlp_project.story_line2.server_storm.bolts.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolts.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spouts.CrawlerNewsArticleReaderSpout;

public class ApplicationBuilder {

	private static ApplicationComponent builder;

	public static void inject(CrawlerNewsArticleReaderSpout instance) {
		getBuilder().inject(instance);

	}

	private static ApplicationComponent getBuilder() {
		if (builder == null)
			builder = DaggerApplicationComponent.create();
		return builder;

	}

	public static void inject(TextProcessingBolt instance) {
		getBuilder().inject(instance);
	}

	public static void inject(ElasticsearchIndexingBolt instance) {
		getBuilder().inject(instance);
	}
	
}
