package ru.nlp_project.story_line2.server_storm.model;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrawlerEntry {

	public static String content(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_CONTENT);
	}

	public static Id id(Map<String, Object> entry) {
		return (Id) entry.get(FIELD_NAME_ID);
	}

	public static void id(Map<String, Object> entry, Id id) {
		entry.put(FIELD_NAME_ID, id);
		if (null == id) {
			entry.remove(FIELD_NAME_ID);
		}

	}


	public static String idString(Map<String, Object> entry) {
		return (String) entry.get(FIELD_NAME_ID).toString();
	}

	public static String imageUrl(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_IMAGE_URL);
	}

	public static boolean inProcess(Map<String, Object> entry) {
		Boolean object = (Boolean) entry.get(FIELD_NAME_IN_PROCESS);
		if (object != null) {
			return object.booleanValue();
		} else {
			return false;
		}
	}


	public static void inProcess(Map<String, Object> entry, boolean value) {
		entry.put(FIELD_NAME_IN_PROCESS, value);
	}

	public static Map<String, Object> newObject() {
		Map<String, Object> result = new HashMap<>();
		processed(result, false);
		inProcess(result, false);
		return result;
	}


	public static String path(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_PATH);
	}

	public static boolean processed(Map<String, Object> entry) {
		Boolean object = (Boolean) entry.get(FIELD_NAME_PROCESSED);
		if (object != null) {
			return object.booleanValue();
		} else {
			return false;
		}
	}


	public static void processed(Map<String, Object> entry, boolean value) {
		entry.put(FIELD_NAME_PROCESSED, value);
	}

	public static Date processingDate(Map<String, Object> entry) {
		return (Date) entry.get(CRAWLER_ENTRY_FIELD_NAME_PROCESSING_DATE);
	}

	public static Date publicationDate(Map<String, Object> entry) {
		return (Date) entry.get(CRAWLER_ENTRY_FIELD_NAME_PUBLICATION_DATE);
	}


	public static void publicationDate(Map<String, Object> entry, Date publicationDate) {
		entry.put(CRAWLER_ENTRY_FIELD_NAME_PUBLICATION_DATE, publicationDate);
	}

	public static String rawContent(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_RAW_CONTENT);
	}


	public static void rawContent(Map<String, Object> entry, String rawContent) {
		entry.put(CRAWLER_ENTRY_FIELD_NAME_RAW_CONTENT, rawContent);
	}

	public static String source(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_SOURCE);
	}

	public static void source(Map<String, Object> entry, String value) {
		entry.put(CRAWLER_ENTRY_FIELD_NAME_SOURCE, value);
	}


	public static String title(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_TITLE);
	}


	public static void title(Map<String, Object> entry, String value) {
		entry.put(CRAWLER_ENTRY_FIELD_NAME_TITLE, value);
	}


	public static String url(Map<String, Object> entry) {
		return (String) entry.get(CRAWLER_ENTRY_FIELD_NAME_URL);
	}


	public static void url(Map<String, Object> entry, String value) {
		entry.put(CRAWLER_ENTRY_FIELD_NAME_URL, value);
	}

	public static byte[] imageData(Map<String, Object> entry) {
		Object obj = entry.get(CRAWLER_ENTRY_FIELD_NAME_IMAGE_DATA);
		if (obj == null || obj.getClass() != byte[].class) {
			return new byte[]{};
		}
		return (byte[]) obj;
	}

	public static void imageData(Map<String, Object> newsArticle, byte[] imageData) {
		newsArticle.put(CRAWLER_ENTRY_FIELD_NAME_IMAGE_DATA, imageData);
	}


}
