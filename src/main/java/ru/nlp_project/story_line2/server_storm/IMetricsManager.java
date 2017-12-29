package ru.nlp_project.story_line2.server_storm;

public interface IMetricsManager {

	void crawlerEntriesCount(String source, long count);

	void processedCrawlerEntriesCount(String source, long count);

	void unprocessedCrawlerEntriesCount(String source, long count);

	void newsArticlesCount(String source, long count);

	void processedNewsArticlesCount(String source, long count);

	void unprocessedNewsArticlesCount(String source, long count);
}
