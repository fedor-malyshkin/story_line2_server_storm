package ru.nlp_project.story_line2.server_storm.functions;

import java.io.StringReader;
import java.util.Map;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

public class FeedSplitFunction implements Function {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TridentOperationContext context) {

	}

	@Override
	public void cleanup() {

	}

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		try {
			String feed = tuple.getStringByField("feed");
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feedObj = input.build(new StringReader(feed));
			System.out.println(feedObj);
			
		} catch (IllegalArgumentException | FeedException e) {
			e.printStackTrace();
			collector.reportError(e);
		}
	}

}
