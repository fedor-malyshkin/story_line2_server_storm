package ru.nlp_project.story_line2.server_storm;

import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;


public interface IMongoDBClient {

	CrawlerEntry getCrawlerEntry(String objectId) throws Exception;


	/**
	 * @param objectId
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	NewsArticle getNewsArticle(String objectId) throws Exception;

	/**
	 * Получить следующую необработанную стать. При этом выставляется в документе (статье краулера)
	 * поле {"in_process": true }
	 * 
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	CrawlerEntry getNextUnprocessedCrawlerEntry() throws Exception;

	void markCrawlerEntryAsProcessed(String msgId) throws Exception;

	/**
	 * Отметит новостную статью как обработанную.
	 * 
	 * При этом выставляется в документе поле {"processed": true }, так же аналогичный флаг
	 * выставляется в статье круалера.
	 * 
	 * @param msgId
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	void markNewsArticleAsProcessed(String objectId) throws Exception;

	void shutdown();

	/**
	 * @param objectId
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	void unmarkCrawlerEntryAsInProcess(String objectId) throws Exception;

	void updateCrawlerEntry(CrawlerEntry entry) throws Exception;

	/**
	 * @param newsArticle
	 * @throws Exception
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их
	 *         обрабатывать
	 */
	void updateNewsArticle(NewsArticle newsArticle) throws Exception;


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
	String upsertNewsArticleByCrawlerEntry(CrawlerEntry entry) throws Exception;

}
