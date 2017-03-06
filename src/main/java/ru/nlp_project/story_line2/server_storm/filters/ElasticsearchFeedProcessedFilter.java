package ru.nlp_project.story_line2.server_storm.filters;

import java.util.Map;

import org.apache.storm.trident.operation.Filter;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;

public class ElasticsearchFeedProcessedFilter implements Filter {
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
	public boolean isKeep(TridentTuple tuple) {
		// TODO Auto-generated method stub
		return false;
	}

}
