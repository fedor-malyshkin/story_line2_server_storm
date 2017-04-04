package ru.nlp_project.story_line2.server_storm.functions;

import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;

@SuppressWarnings("unused")
public class NewsArticleFinderFunction implements Function {

	@Inject
	IMongoDBClient mongoDBClient;

	private static final long serialVersionUID = 1L;
	private String functionName;
	private Logger log;

	public NewsArticleFinderFunction(String functionName) {
		this.functionName = functionName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map conf, TridentOperationContext context) {
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(conf).inject(this);
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		tuple.getFields();

	}

}
