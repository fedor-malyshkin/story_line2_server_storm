package ru.nlp_project.story_line2.server_storm.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrawlerEntry {
	public final static String ID = "_id";
	public final static String PROCESSED = "processed";
	public final static String IN_PROCESS = "in_process";
	public final static String PUBLICATION_DATE = "publication_date";
	public final static String PROCESSING_DATE = "processing_date";
	public final static String CONTENT = "content";
	public final static String RAW_CONTENT = "raw_content";
	public final static String PATH = "path";
	public final static String SOURCE = "source";
	public final static String TITLE = "title";
	public final static String URL = "url";
	public final static String IMAGE_URL = "image_url";


	public static String content(Map<String, Object> entry) {
		return (String) entry.get(CONTENT);
	}

	public static Id id(Map<String, Object> entry) {
		return (Id) entry.get(ID);
	}

	public static void id(Map<String, Object> entry, Id id) {
		entry.put(ID, id);
		if (null == id)
			entry.remove(ID);

	}


	public static String idString(Map<String, Object> entry) {
		return (String) entry.get(ID).toString();
	}

	public static String imageUrl(Map<String, Object> entry) {
		return (String) entry.get(IMAGE_URL);
	}

	public static boolean inProcess(Map<String, Object> entry) {
		Boolean object = (Boolean) entry.get(IN_PROCESS);
		if (object != null)
			return object.booleanValue();
		else
			return false;
	}


	public static void inProcess(Map<String, Object> entry, boolean value) {
		entry.put(IN_PROCESS, value);
	}

	public static Map<String, Object> newObject() {
		Map<String, Object> result = new HashMap<>();
		processed(result, false);
		inProcess(result, false);
		return result;
	}



	public static String path(Map<String, Object> entry) {
		return (String) entry.get(PATH);
	}

	public static boolean processed(Map<String, Object> entry) {
		Boolean object = (Boolean) entry.get(PROCESSED);
		if (object != null)
			return object.booleanValue();
		else
			return false;
	}


	public static void processed(Map<String, Object> entry, boolean value) {
		entry.put(PROCESSED, value);
	}

	public static Date processingDate(Map<String, Object> entry) {
		return (Date) entry.get(PROCESSING_DATE);
	}

	public static Date publicationDate(Map<String, Object> entry) {
		return (Date) entry.get(PUBLICATION_DATE);
	}


	public static void publicationDate(Map<String, Object> entry, Date publicationDate) {
		entry.put(PUBLICATION_DATE, publicationDate);
	}

	public static String rawContent(Map<String, Object> entry) {
		return (String) entry.get(RAW_CONTENT);
	}



	public static void rawContent(Map<String, Object> entry, String rawContent) {
		entry.put(RAW_CONTENT, rawContent);
	}

	public static String source(Map<String, Object> entry) {
		return (String) entry.get(SOURCE);
	}

	public static void source(Map<String, Object> entry, String value) {
		entry.put(SOURCE, value);
	}


	public static String title(Map<String, Object> entry) {
		return (String) entry.get(TITLE);
	}


	public static void title(Map<String, Object> entry, String value) {
		entry.put(TITLE, value);
	}


	public static String url(Map<String, Object> entry) {
		return (String) entry.get(URL);
	}


	public static void url(Map<String, Object> entry, String value) {
		entry.put(URL, value);
	}

}
