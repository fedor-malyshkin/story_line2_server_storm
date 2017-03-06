package ru.nlp_project.story_line2.server_storm.state;

import java.util.List;
import java.util.Map;

import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.state.StateUpdater;
import org.apache.storm.trident.tuple.TridentTuple;

public class MongoDBFeedStateUpdater implements StateUpdater<FeedDB> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateState(FeedDB state, List<TridentTuple> tuples, TridentCollector collector) {
		// TODO Auto-generated method stub

	}

}
