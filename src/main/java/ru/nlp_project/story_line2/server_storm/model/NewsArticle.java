package ru.nlp_project.story_line2.server_storm.model;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewsArticle {




	public static String content(Map<String, Object> entry) {
		return (String) entry.get(NEWS_ARTICLE_FIELD_NAME_CONTENT);
	}

	public static void content(Map<String, Object> newsArticle, String content) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_CONTENT, content);
	}

	public static Id crawlerId(Map<String, Object> newsArticle) {
		return (Id) newsArticle.get(NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID);
	}

	public static void crawlerId(Map<String, Object> entry, Id crawlerId) {
		entry.put(NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID, crawlerId);
	}

	public static String crawlerIdString(Map<String, Object> newsArticle) {
		return newsArticle.get(NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID).toString();
	}

	public static Date creationDate(Map<String, Object> entry) {
		return (Date) entry.get(NEWS_ARTICLE_FIELD_NAME_CREATION_DATE);
	}

	public static void creationDate(Map<String, Object> entry, Date creationDate) {
		entry.put(NEWS_ARTICLE_FIELD_NAME_CREATION_DATE, creationDate);
	}

	public static Id id(Map<String, Object> newsArticle) {
		return (Id) newsArticle.get(FIELD_NAME_ID);
	}

	public static void id(Map<String, Object> entry, Id id) {
		entry.put(FIELD_NAME_ID, id);
		if (null == id) {
			entry.remove(FIELD_NAME_ID);
		}

	}

	public static String idString(Map<String, Object> newsArticle) {
		return newsArticle.get(FIELD_NAME_ID).toString();
	}

	public static String imageUrl(Map<String, Object> entry) {
		return (String) entry.get(NEWS_ARTICLE_FIELD_NAME_IMAGE_URL);
	}

	public static void imageUrl(Map<String, Object> newsArticle, String imageUrl) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_IMAGE_URL, imageUrl);
	}


	public static byte[] imageData(Map<String, Object> entry) {
		Object obj = entry.get(NEWS_ARTICLE_FIELD_NAME_IMAGE_DATA);
		if (obj == null || obj.getClass() != byte[].class) {
			return new byte[]{};
		}
		return (byte[]) obj;
	}

	public static void imageData(Map<String, Object> newsArticle, byte[] imageData) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_IMAGE_DATA, imageData);
	}


	public static Map<String, Object> newObject(Map<String, Object> crawlerEntry) {
		if (crawlerEntry == null) {
			throw new IllegalArgumentException("'crawlerEntry' ath must be not null");
		}
		Map<String, Object> result = new HashMap<>();
		crawlerId(result, CrawlerEntry.id(crawlerEntry));
		creationDate(result, new Date());
		content(result, CrawlerEntry.content(crawlerEntry));
		path(result, CrawlerEntry.path(crawlerEntry));
		title(result, CrawlerEntry.title(crawlerEntry));
		url(result, CrawlerEntry.url(crawlerEntry));
		publicationDate(result, CrawlerEntry.publicationDate(crawlerEntry));
		source(result, CrawlerEntry.source(crawlerEntry));
		imageUrl(result, CrawlerEntry.imageUrl(crawlerEntry));
		imageData(result, CrawlerEntry.imageData(crawlerEntry));
		return result;
	}

	public static Map<String, Object> newObject() {
		Map<String, Object> result = new HashMap<>();
		return result;
	}


	public static String path(Map<String, Object> entry) {
		return (String) entry.get(NEWS_ARTICLE_FIELD_NAME_PATH);
	}

	public static void path(Map<String, Object> entry, String path) {
		entry.put(NEWS_ARTICLE_FIELD_NAME_PATH, path);
	}

	public static Date publicationDate(Map<String, Object> entry) {
		return (Date) entry.get(NEWS_ARTICLE_FIELD_NAME_PUBLICATION_DATE);
	}

	public static void publicationDate(Map<String, Object> newsArticle, Date publicationDate) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_PUBLICATION_DATE, publicationDate);
	}

	public static Date processingDate(Map<String, Object> entry) {
		return (Date) entry.get(NEWS_ARTICLE_FIELD_NAME_PROCESSING_DATE);
	}

	public static void processingDate(Map<String, Object> newsArticle, Date processingDate) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_PROCESSING_DATE, processingDate);
	}


	public static String source(Map<String, Object> entry) {
		return (String) entry.get(FIELD_NAME_SOURCE);
	}

	public static void source(Map<String, Object> entry, String source) {
		entry.put(FIELD_NAME_SOURCE, source);

	}

	public static String title(Map<String, Object> entry) {
		return (String) entry.get(NEWS_ARTICLE_FIELD_NAME_TITLE);
	}

	public static void title(Map<String, Object> newsArticle, String title) {
		newsArticle.put(NEWS_ARTICLE_FIELD_NAME_TITLE, title);
	}

	public static String url(Map<String, Object> entry) {
		return (String) entry.get(NEWS_ARTICLE_FIELD_NAME_URL);
	}

	public static void url(Map<String, Object> entry, String url) {
		entry.put(NEWS_ARTICLE_FIELD_NAME_URL, url);
	}

}
