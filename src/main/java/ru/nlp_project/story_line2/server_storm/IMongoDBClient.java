package ru.nlp_project.story_line2.server_storm;

import java.util.Date;

import ru.nlp_project.story_line2.server_storm.datamodel.CrawlerNewsArticle;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;


public interface IMongoDBClient {

	/**
	 * @param objectId
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	NewsArticle getNewsArticle(String objectId) throws Exception;

	/**
	 * Получить следующую необработанную стать. При этом выставляется в документе (статье краулера)
	 * поле {"in_process": true }
	 * 
	 * @param lastEmittedDate
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	CrawlerNewsArticle getNextUnprocessedCrawlerArticle(Date lastEmittedDate) throws Exception;

	/**
	 * Отметит новостную статью как обработанную.
	 * 
	 * При этом выставляется в документе поле {"processed": true }, так же аналогичный флаг
	 * выставляется в статье круалера.
	 * 
	 * @param msgId
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	void markNewsArticleAsProcessed(String objectId) throws Exception;

	void shutdown();

	/**
	 * @param objectId
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	void unmarkCrawlerArticleAsInProcess(String objectId) throws Exception;

	/**
	 * @param newsArticle
	 * @throws Exception
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	void updateNewsArticle(NewsArticle newsArticle) throws Exception;

	/**
	 * @param crawlerNewsArticle
	 * @return
	 * @throws Exception произвольное исключение (например при работе с БД) код должен усеть их обрабатывать
	 */
	String writeNewNewsArticle(CrawlerNewsArticle crawlerNewsArticle) throws Exception;

}
