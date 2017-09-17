package ru.nlp_project.story_line2.server_storm.functions;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ARGS;

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
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.Id;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

@SuppressWarnings("unused")
public class NewsHeaderFinderFunction implements Function {

	private static final long serialVersionUID = -8030809641382902919L;
	@Inject
	ISearchManager searchManager;
	private String functionName;
	private Logger log;

	public NewsHeaderFinderFunction(String functionName) {
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
		if (NamesUtil.FUN_NAME_GET_NEWS_HEADERS.equalsIgnoreCase(functionName)) {
			List<Map<String, Object>> newsHeaders = getNewsHeaders(params, collector);
			collector.emit(new Values(newsHeaders));
		}
	}

	private List<Map<String, Object>> getNewsHeaders(Map<String, Object> params,
			TridentCollector collector) {
		String source = (String) params.get("source");
		int count = (int) params.get("count");

		Id lastNewsId = null;
		if (null != params.get("last_news_id")) {
			lastNewsId = (Id) params.get("last_news_id");
		}
		return searchManager
				.getNewsHeaders(source, count, lastNewsId == null ? null : lastNewsId.toString());

	}

}
