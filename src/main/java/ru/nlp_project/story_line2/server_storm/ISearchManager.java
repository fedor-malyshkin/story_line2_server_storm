package ru.nlp_project.story_line2.server_storm;

import java.util.List;
import java.util.Map;

import ru.nlp_project.story_line2.server_storm.model.NewsArticle;

public interface ISearchManager {
	void shutdown();

	void index(NewsArticle newsArticle) throws Exception;

	List<Map<String, Object>> getNewsHeaders(String source, int count);
}
