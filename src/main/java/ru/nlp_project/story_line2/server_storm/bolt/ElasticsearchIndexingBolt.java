package ru.nlp_project.story_line2.server_storm.bolt;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_MAINTENANCE_COMMAND;

import java.util.Map;
import javax.inject.Inject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class ElasticsearchIndexingBolt implements IRichBolt {

	private static final long serialVersionUID = -7621987543099710796L;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public ISearchManager searchManager;
	private OutputCollector collector;
	private Logger log;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(stormConf).inject(this);
	}

	@Override
	public void execute(Tuple input) {
		String tupleType = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE);

		switch (tupleType) {
			case NamesUtil.TUPLE_TYPE_NEWS_ENTRY: {
				processNewsEntry(input);
				break;
			}
			case NamesUtil.TUPLE_TYPE_MAINTENANCE_COMMAND: {
				processMaintenanceCommand(input);
				break;
			}
			default: {
				log.error("Unknown tuple type: " + tupleType);
				throw new IllegalStateException("Unknown tuple type: " + tupleType);

			}
		}
	}

	private void processMaintenanceCommand(Tuple input) {
		Map<String, String> maintenanceEntry = (Map<String, String>) input
				.getValueByField(TUPLE_FIELD_NAME_MAINTENANCE_COMMAND);
		// TODO: implement

	}

	private void processNewsEntry(Tuple input) {

		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		try {
			Map<String, Object> newsArticle = mongoDBClient.getNewsArticle(objectId);
			searchManager.indexNewsArticle(newsArticle);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			collector.fail(input);
		}
		// ack prev tuples
		collector.ack(input);
	}

	@Override
	public void cleanup() {
		searchManager.shutdown();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
