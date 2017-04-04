package ru.nlp_project.story_line2.server_storm.functions;


import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_RESULT;

import java.util.List;
import java.util.Map;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;

@SuppressWarnings("unused")
public class JSONConverterFunction implements Function {

	private static final long serialVersionUID = 3665130829460246745L;
	private String functionName;
	private Logger log;

	public JSONConverterFunction(String functionName) {
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
		List<Map<String, Object>> newsHeaders =
				(List<Map<String, Object>>) tuple.getValueByField(TUPLE_FIELD_NAME_RESULT);
		String json = JSONUtils.serialize(newsHeaders);
		collector.emit(new Values(json));
	}

}
