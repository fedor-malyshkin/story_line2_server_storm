package ru.nlp_project.story_line2.server_storm;

import java.util.List;

public interface IGLRParser {
	public class Fact {

	}

	void shutdown();

	void parseText(String text);

	List<Fact> getGeoFacts();

	List<Fact> getFIOFacts();

	List<Fact> getNounFacts();

	List<Fact> getAdjFacts();

}
