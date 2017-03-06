package ru.nlp_project.story_line2.server_storm;

import ru.nlp_project.story_line2.server_storm.model.NewsArticle;

public interface ISearchManager {
	public void shutdown();

	public void index(NewsArticle newsArticle) throws Exception;
}
