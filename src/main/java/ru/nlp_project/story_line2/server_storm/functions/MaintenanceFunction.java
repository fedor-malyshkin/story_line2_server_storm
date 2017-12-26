package ru.nlp_project.story_line2.server_storm.functions;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ARGS;

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
import ru.nlp_project.story_line2.server_storm.utils.JSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MaintenanceFunction implements Function {

	private static final long serialVersionUID = 1L;
	@Inject
	IMongoDBClient mongoDBClient;
	private String functionName;
	private Logger log;

	public MaintenanceFunction(String functionName) {
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
			case NamesUtil.FUN_NAME_MAINTENANCE: {
				try {
					mongoDBClient.insertMaintenanceCommandEntry(params);
				} catch (Exception e) {
					log.error("Exception while storing maintenance message: " + params, e);
				}
				break;
			}
			default:
				throw new IllegalStateException("Unknown function name: " + functionName);
		}
	}

}