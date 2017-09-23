package ru.nlp_project.story_line2.server_storm.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewsArticle {

	public final static String ID = "_id";
	public final static String CRAWLER_ID = "crawler_id";
	public final static String CREATION_DATE = "creation_date";
	public final static String CONTENT = "content";
	public final static String PATH = "path";
	public final static String TITLE = "title";
	public final static String URL = "url";
	public final static String PUBLICATION_DATE = "publication_date";
	public final static String SOURCE = "source";
	public final static String IMAGE_URL = "image_url";
	public final static String IMAGE_DATA = "image_data";

	public static String content(Map<String, Object> entry) {
		return (String) entry.get(CONTENT);
	}

	public static void content(Map<String, Object> newsArticle, String content) {
		newsArticle.put(CONTENT, content);
	}

	public static Id crawlerId(Map<String, Object> newsArticle) {
		return (Id) newsArticle.get(CRAWLER_ID);
	}

	public static void crawlerId(Map<String, Object> entry, Id crawlerId) {
		entry.put(CRAWLER_ID, crawlerId);
	}

	public static String crawlerIdString(Map<String, Object> newsArticle) {
		return newsArticle.get(CRAWLER_ID).toString();
	}

	public static Date creationDate(Map<String, Object> entry) {
		return (Date) entry.get(CREATION_DATE);
	}

	public static void creationDate(Map<String, Object> entry, Date creationDate) {
		entry.put(CREATION_DATE, creationDate);
	}

	public static Id id(Map<String, Object> newsArticle) {
		return (Id) newsArticle.get(ID);
	}

	public static void id(Map<String, Object> entry, Id id) {
		entry.put(ID, id);
		if (null == id) {
			entry.remove(ID);
		}

	}

	public static String idString(Map<String, Object> newsArticle) {
		return newsArticle.get(ID).toString();
	}

	public static String imageUrl(Map<String, Object> entry) {
		return (String) entry.get(IMAGE_URL);
	}

	public static void imageUrl(Map<String, Object> newsArticle, String imageUrl) {
		newsArticle.put(IMAGE_URL, imageUrl);
	}


	public static byte[] imageData(Map<String, Object> entry) {
		Object obj = entry.get(IMAGE_DATA);
		if (obj == null || obj.getClass() != byte[].class) {
			return new byte[]{};
		}
		return (byte[]) obj;
	}

	public static void imageData(Map<String, Object> newsArticle, byte[] imageData) {
		newsArticle.put(IMAGE_DATA, imageData);
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
		return (String) entry.get(PATH);
	}

	public static void path(Map<String, Object> entry, String path) {
		entry.put(PATH, path);
	}

	public static Date publicationDate(Map<String, Object> entry) {
		return (Date) entry.get(PUBLICATION_DATE);
	}

	public static void publicationDate(Map<String, Object> newsArticle, Date publicationDate) {
		newsArticle.put(PUBLICATION_DATE, publicationDate);
	}

	public static String source(Map<String, Object> entry) {
		return (String) entry.get(SOURCE);
	}

	public static void source(Map<String, Object> entry, String source) {
		entry.put(SOURCE, source);

	}

	public static String title(Map<String, Object> entry) {
		return (String) entry.get(TITLE);
	}

	public static void title(Map<String, Object> newsArticle, String title) {
		newsArticle.put(TITLE, title);
	}

	public static String url(Map<String, Object> entry) {
		return (String) entry.get(URL);
	}

	public static void url(Map<String, Object> entry, String url) {
		entry.put(URL, url);
	}

}
