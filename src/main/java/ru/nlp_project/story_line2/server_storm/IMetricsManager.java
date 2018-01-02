package ru.nlp_project.story_line2.server_storm;

public interface IMetricsManager {

	void crawlerEntriesCountDB(String source, long count);

	void processedCrawlerEntriesCountDB(String source, long count);

	void unprocessedCrawlerEntriesCountDB(String source, long count);

	void newsArticlesCountDB(String source, long count);

	void processedNewsArticlesCountDB(String source, long count);

	void unprocessedNewsArticlesCountDB(String source, long count);

	void newsArticlesCountSearch(String source, long count);

	void callDuration(Class caller, Class clazz, String methodName, long callDuration);

	void archivedCrawlerEntriesCountDB(String source, long count);
}
