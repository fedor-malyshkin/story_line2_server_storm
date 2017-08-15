package ru.nlp_project.story_line2.server_storm;

import java.util.Date;
import java.util.Map;


public interface IMongoDBClient {
	public static final String FIELD_ID = "_id";
	public static final String FIELD_CRAWLER_ID = "crawler_id";
	public static final String CRAWLER_ENTRY_FIELD_PROCESSED = "processed";
	public static final String CRAWLER_ENTRY_FIELD_ARCHIVED = "archived";
	public static final String CRAWLER_ENTRY_FIELD_ARCHIVE_PROCESSED = "archive_processed";
	public static final String CRAWLER_ENTRY_FIELD_IN_PROCESS = "in_process";


	Map<String, Object> getCrawlerEntry(String id) throws Exception;


	/**
	 * @param objectId
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	Map<String, Object> getNewsArticle(String newsArticleId) throws Exception;

	Map<String, Object> getNextUnarchivedCrawlerEntry(Date date) throws Exception;

	/**
	 * Получить следующую необработанную стать. При этом выставляется в документе (статье краулера)
	 * поле {"in_process": true }
	 * 
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	Map<String, Object> getNextUnprocessedCrawlerEntry() throws Exception;

	void markCrawlerEntryAsArchiveProcessed(String crawlerEntryId) throws Exception;

	void markCrawlerEntryAsProcessedByNewsArticleId(String newsArticleId) throws Exception;

	void shutdown();

	void unmarkCrawlerEntryAsInProcess(String crawlerEntryId) throws Exception;


	/**
	 * @param objectId
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	void unmarkCrawlerEntryAsInProcessByNewsArticleId(String newsArticleId) throws Exception;


	void unmarkUnarchivedCrawlerEntriesArchiveProcessed() throws Exception;


	void updateCrawlerEntry(Map<String, Object> entry) throws Exception;


	/**
	 * @param newsArticle
	 * @throws Exception
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	void updateNewsArticle(Map<String, Object> newsArticle) throws Exception;


	/**
	 * Сделать новый или найти существующий объект "NewsArticle" по данным "CrawlerEntry"
	 * (source:path).
	 * 
	 * 
	 * @param entry
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	String upsertNewsArticleByCrawlerEntry(Map<String, Object> entry) throws Exception;

}
