package ru.nlp_project.story_line2.server_storm;

import java.util.List;
import java.util.Map;

public interface ISearchManager {
	void shutdown();

	void indexNewsArticle(Map<String, Object> newsArticle) throws Exception;

	List<Map<String, Object>> getNewsHeaders(String source, int count);

	List<Map<String, Object>> getNewsArticle(String id);
}
