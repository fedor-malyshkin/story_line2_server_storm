package ru.nlp_project.story_line2.server_storm.state;

import java.util.Map;

import org.apache.storm.task.IMetricsContext;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.state.StateFactory;

public class MongoDBFeedStateFactory implements StateFactory {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	@Override
	public State makeState(Map conf, IMetricsContext metrics, int partitionIndex,
			int numPartitions) {
		return new FeedDB();
	}

}
