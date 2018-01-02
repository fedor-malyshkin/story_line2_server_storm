package ru.nlp_project.story_line2.server_storm.utils;

public class NamesUtil {

	public final static String CRAWLER_ENTRY_FIELD_NAME_PUBLICATION_DATE = "publication_date";
	public final static String CRAWLER_ENTRY_FIELD_NAME_PROCESSING_DATE = "processing_date";
	public final static String CRAWLER_ENTRY_FIELD_NAME_CONTENT = "content";
	public final static String CRAWLER_ENTRY_FIELD_NAME_RAW_CONTENT = "raw_content";
	public final static String CRAWLER_ENTRY_FIELD_NAME_TITLE = "title";
	public final static String CRAWLER_ENTRY_FIELD_NAME_URL = "url";
	public final static String CRAWLER_ENTRY_FIELD_NAME_IMAGE_URL = "image_url";
	public final static String CRAWLER_ENTRY_FIELD_NAME_IMAGE_DATA = "image_data";


	public final static String NEWS_ARTICLE_FIELD_NAME_CREATION_DATE = "creation_date";
	public final static String NEWS_ARTICLE_FIELD_NAME_CONTENT = "content";
	public final static String NEWS_ARTICLE_FIELD_NAME_PATH = "path";
	public final static String NEWS_ARTICLE_FIELD_NAME_TITLE = "title";
	public final static String NEWS_ARTICLE_FIELD_NAME_URL = "url";
	public final static String NEWS_ARTICLE_FIELD_NAME_PUBLICATION_DATE = "publication_date";
	public final static String NEWS_ARTICLE_FIELD_NAME_IMAGE_URL = "image_url";
	public final static String NEWS_ARTICLE_FIELD_NAME_IMAGE_DATA = "image_data";

	public static final String FIELD_NAME_ID = "_id";
	public static final String FIELD_NAME_SOURCE = "source";
	public static final String NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID = "crawler_id";
	public static final String FIELD_NAME_PROCESSED = "processed";
	public static final String CRAWLER_ENTRY_FIELD_NAME_ARCHIVED = "archived";
	public static final String FIELD_NAME_IN_PROCESS = "in_process";
	// images_purged
	public static final String NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED = "images_purged";
	public static final String CRAWLER_ENTRY_FIELD_NAME_PATH = "path";

	public static final String FUN_NAME_GET_NEWS_ARTICLE = "get_news_article";
	public static final String FUN_NAME_GET_NEWS_HEADERS = "get_news_headers";
	public static final String FUN_NAME_GET_NEWS_IMAGES = "get_news_images";
	public static final String FUN_NAME_MAINTENANCE = "maintenance";

	public static final String STREAM_ARCHIVE_OLD_CRAWLER_ENTRIES = "archive_old_crawler_entries";
	public static final String STREAM_PURGE_NEWS_ARTICLE_IMAGES = "purge_news_article_images";

	public static final String MONGODB_CRAWLER_COLLECTION_NAME = "crawler_entries";
	public static final String MONGODB_CRAWLER_DATABASE_NAME = "crawler";
	public static final String MONGODB_STORYLINE_COLLECTION_NAME = "news_entries";
	public static final String MONGODB_STORYLINE_DATABASE_NAME = "storyline";
	public static final String MONGODB_MAINTENANCE_COLLECTION_NAME = "maintenance_entries";
	public static final String MONGODB_MAINTENANCE_DATABASE_NAME = MONGODB_STORYLINE_DATABASE_NAME;

	public static final String TUPLE_FIELD_NAME_ARGS = "args";
	public static final String TUPLE_FIELD_NAME_ID = "object_id";
	public static final String TUPLE_FIELD_NAME_JSON = "json";
	public static final String TUPLE_FIELD_NAME_RESULT = "result";
	public static final String TUPLE_FIELD_NAME_SOURCE = "source";
	public static final String TUPLE_FIELD_NAME_TUPLE_TYPE = "tuple_type";
	public static final String TUPLE_FIELD_NAME_MAINTENANCE_COMMAND = "maintenance_command";
	public static final String TUPLE_TYPE_NEWS_ENTRY = "news_entry";
	public static final String TUPLE_TYPE_MAINTENANCE_COMMAND = "maintenance_command";
	public static final String TUPLE_TYPE_METRICS_COMMAND = "metrics_command";


	// maintenance
	public static final String MAINTENANCE_COMMAND_FIELD_NAME_COMMAND = "command";
	public static final String MAINTENANCE_COMMAND_FIELD_NAME_PARAM1 = "param1";
	public static final String MAINTENANCE_COMMAND_FIELD_NAME_PARAM2 = "param2";
	public static final String MAINTENANCE_COMMAND_RELOAD_SCRIPS = "reload_scripts";
	public static final String MAINTENANCE_COMMAND_REINDEX_SOURCE = "reindex_source";
	public static final String MAINTENANCE_COMMAND_REINDEX_SOURCE_PARAM1_ALL = "ALL";
}
