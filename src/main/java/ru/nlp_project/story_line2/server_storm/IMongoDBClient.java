package ru.nlp_project.story_line2.server_storm;

import java.util.Date;

import de.undercouch.bson4jackson.types.ObjectId;
import ru.nlp_project.story_line2.server_storm.datamodel.CrawlerNewsArticle;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;


public interface IMongoDBClient {
	void shutdown();

	String getHexString(ObjectId objectId);

	ObjectId createObjectId(String hexString);

	/**
	 * Получить следующую необработанную стать. При этом выставляется в документе (статье краулера)
	 * поле {"in_process": true }
	 * 
	 * @param lastEmittedDate
	 * @return
	 */
	CrawlerNewsArticle getNextUnprocessedCrawlerArticle(Date lastEmittedDate);

	String writeNewNewsArticle(CrawlerNewsArticle crawlerNewsArticle);

	/**
	 * Отметит новостную статью как обработанную.
	 * 
	 * При этом выставляется в документе поле {"processed": true }, так же аналогичный флаг
	 * выставляется в статье круалера.
	 * 
	 * @param msgId
	 */
	void markNewsArticleAsProcessed(String objectId);

	void unmarkCrawlerArticleAsInProcess(String objectId);

	NewsArticle getNewsArticle(String objectId);

}
