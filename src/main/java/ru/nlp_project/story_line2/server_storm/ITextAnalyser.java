package ru.nlp_project.story_line2.server_storm;

import java.io.IOException;
import java.util.List;

import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;

public interface ITextAnalyser {

	void shutdown();

	void parseText(String text) throws IOException;

	List<NewsArticleFact> getGeoFacts();

	List<NewsArticleFact> getFIOFacts();

	List<NewsArticleFact> getNounFacts();
}
