package ru.nlp_project.story_line2.server_storm.dagger;

import dagger.Component;
import javax.inject.Singleton;
import ru.nlp_project.story_line2.server_storm.bolt.ContentExtractingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolt.MaintenanceBolt;
import ru.nlp_project.story_line2.server_storm.bolt.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.functions.ArchiveCrawlerEntryFunction;
import ru.nlp_project.story_line2.server_storm.functions.JSONConverterFunction;
import ru.nlp_project.story_line2.server_storm.functions.MaintenanceFunction;
import ru.nlp_project.story_line2.server_storm.functions.NewsArticleFinderFunction;
import ru.nlp_project.story_line2.server_storm.functions.NewsHeaderFinderFunction;
import ru.nlp_project.story_line2.server_storm.functions.PurgeNewsArticleImagesFunction;
import ru.nlp_project.story_line2.server_storm.spout.CrawlerEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.MaintenanceEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.UnarchivedCrawlerEntryReaderSpout;
import ru.nlp_project.story_line2.server_storm.spout.UnpurgedNewsArticleReaderSpout;

@Singleton
@Component(modules = {ServerStormModule.class})
public abstract class ServerStormComponent {

	// utils
	public abstract void inject(ContentExtractingBolt instance);

	public abstract void inject(PurgeNewsArticleImagesFunction instance);

	public abstract void inject(UnpurgedNewsArticleReaderSpout instance);

	public abstract void inject(TextProcessingBolt instance);

	public abstract void inject(ElasticsearchIndexingBolt instance);

	public abstract void inject(NewsHeaderFinderFunction instance);

	public abstract void inject(JSONConverterFunction instance);

	public abstract void inject(NewsArticleFinderFunction instance);

	public abstract void inject(CrawlerEntryReaderSpout instance);

	public abstract void inject(UnarchivedCrawlerEntryReaderSpout instance);

	public abstract void inject(ArchiveCrawlerEntryFunction instance);

	public abstract void inject(MaintenanceFunction instance);

	public abstract void inject(MaintenanceEntryReaderSpout instance);

	public abstract void inject(MaintenanceBolt instance);
}
