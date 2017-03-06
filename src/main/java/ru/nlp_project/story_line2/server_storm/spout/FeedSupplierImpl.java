package ru.nlp_project.story_line2.server_storm.spout;

import javax.inject.Inject;

public class FeedSupplierImpl implements IFeedSupplier {

	@Inject
	public FeedSupplierImpl() {

	}

	@Override
	public String getNextFeed() {
		return "http://komiinform.ru/rss/news/";
	}


}
