package ru.nlp_project.story_line2.server_storm;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface IMongoDBClient {


	/**
	 * Get crawler entry by id.
	 */
	Map<String, Object> getCrawlerEntry(String id) throws Exception;


	/**
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 * обрабатывать
	 */
	Map<String, Object> getNewsArticle(String newsArticleId) throws Exception;

	/**
	 * получить следующий незаархивированный CE, старше указанной даты
	 *
	 * @param date дата, раньше которой выбираются CE
	 * @return следующий элемент или null при их отсуствии
	 */
	Map<String, Object> getNextUnarchivedCrawlerEntry(Date date) throws Exception;

	/**
	 * Получить следующую необработанную стать. При этом выставляется в документе (статье краулера)
	 * поле {"in_process": true }
	 *
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 * обрабатывать
	 */
	Map<String, Object> getNextUnprocessedCrawlerEntry() throws Exception;

	void markCrawlerEntryAsProcessedByNewsArticleId(String newsArticleId) throws Exception;

	void shutdown();

	void unmarkCrawlerEntryAsInProcess(String crawlerEntryId) throws Exception;


	/**
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 * обрабатывать
	 */
	void unmarkCrawlerEntryAsInProcessByNewsArticleId(String newsArticleId) throws Exception;

	void updateCrawlerEntry(Map<String, Object> entry) throws Exception;


	/**
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 * обрабатывать
	 */
	void updateNewsArticle(Map<String, Object> newsArticle) throws Exception;


	/**
	 * Сделать новый или найти существующий объект "NewsArticle" по данным "CrawlerEntry"
	 * (source:path).
	 *
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 * обрабатывать
	 */
	String upsertNewsArticleByCrawlerEntry(Map<String, Object> entry) throws Exception;

	void markCrawlerEntryAsArchived(String crawlerEntryId) throws Exception;

	Map<String, Object> getNextUnpurgedImagesNewsArticle(Date date) throws Exception;

	void unmarkNewsArticleAsInProcess(String newsArticleId) throws Exception;

	void markNewsArticleAsImagesPurged(String newsArticleId) throws Exception;

	void initialize();

	void insertMaintenanceCommandEntry(Map<String, Object> entry) throws Exception;

	/**
	 * Получить следующую запись обслуживания (или null).
	 *
	 * @return следующую запись обслуживания (или null)
	 */
	Map<String, Object> getNextMaintenanceCommandEntry() throws Exception;

	void deleteNewsArticles(String source) throws Exception;

	void unmarkCrawlerEntriesAsProcessed(String source) throws Exception;

	/**
	 * Delete all news articles.
	 */
	void deleteAllNewsArticles() throws Exception;

	void unmarkAllCrawlerEntriesAsProcessed() throws Exception;

	List<String> getCrawlerEntrySources() throws Exception;

	List<String> getNewsArticleSources() throws Exception;

	long getProcessedNewsArticlesCount(String source) throws Exception;

	long getUnprocessedNewsArticlesCount(String source) throws Exception;

	long getNewsArticlesCount(String source) throws Exception;

	long getProcessedCrawlerEntriesCount(String source) throws Exception;

	long getUnprocessedCrawlerEntriesCount(String source) throws Exception;

	long getCrawlerEntriesCount(String source) throws Exception;

}
