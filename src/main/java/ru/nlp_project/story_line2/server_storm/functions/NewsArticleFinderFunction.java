package ru.nlp_project.story_line2.server_storm.functions;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ARGS;

import java.util.Map;
import javax.inject.Inject;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

@SuppressWarnings("unused")
public class NewsArticleFinderFunction implements Function {

	private static final long serialVersionUID = 1L;
	@Inject
	ISearchManager searchManager;
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
		String json = tuple.getStringByField(TUPLE_FIELD_NAME_ARGS);
		Map<String, Object> params = JSONUtils.deserialize(json);
		switch (functionName) {
			case NamesUtil.FUN_NAME_GET_NEWS_ARTICLE: {
				Map<String, Object> newsArticles = getNewsArticle(params, collector);
				collector.emit(new Values(newsArticles));
				break;
			}
			case NamesUtil.FUN_NAME_GET_NEWS_IMAGES: {
				Map<String, Object> articleData = getNewsArticleImageData(params, collector);
				collector.emit(new Values(articleData));
				break;
			}
			default:
				throw new IllegalStateException("Unknown function name: " + functionName);
		}
	}


	private Map<String, Object> getNewsArticle(Map<String, Object> params,
			TridentCollector collector) {
		Id id = (Id) params.get("id");
		return searchManager.getNewsArticle(id.toString());
	}

	private Map<String, Object> getNewsArticleImageData(Map<String, Object> params,
			TridentCollector collector) {
		Id id = (Id) params.get("id");
		return searchManager.getNewsArticleImageData(id.toString(), 0);
	}
}
