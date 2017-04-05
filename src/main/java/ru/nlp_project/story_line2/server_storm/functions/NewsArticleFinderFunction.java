package ru.nlp_project.story_line2.server_storm.functions;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ARGS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

@SuppressWarnings("unused")
public class NewsArticleFinderFunction implements Function {

	@Inject
	ISearchManager searchManager;

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

	@SuppressWarnings("unchecked")
	@Override
	public void execute(TridentTuple tuple, TridentCollector collector) {
		String json = tuple.getStringByField(TUPLE_FIELD_NAME_ARGS);
		Map<String, Object> params = JSONUtils.deserialize(json, HashMap.class);
		if (NamesUtil.FUN_NAME_GET_NEWS_ARTICLE.equalsIgnoreCase(functionName)) {
			List<Map<String, Object>> newsArticles = getNewsArticles(params, collector);
			collector.emit(new Values(newsArticles));
		}
	}



	private List<Map<String, Object>> getNewsArticles(Map<String, Object> params,
			TridentCollector collector) {
		String id = (String) params.get("id");
		return searchManager.getNewsArticle(id);

	}
}
