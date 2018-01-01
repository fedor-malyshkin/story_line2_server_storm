package ru.nlp_project.story_line2.server_storm.bolt;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_MAINTENANCE_COMMAND;

import java.util.Arrays;
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
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

@SuppressWarnings("unused")
public class TextProcessingBolt implements IRichBolt {

	private static final long serialVersionUID = 8970948884690790271L;
	@Inject
	public IMongoDBClient mongoDBClient;
	//@Inject
	//public ITextAnalyser textAnalyser;
	@Inject
	public IConfigurationManager configurationManager;
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
		String sourceName = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_SOURCE);
		try {
			Map<String, Object> newsArticle = mongoDBClient.getNewsArticle(objectId);
			//textAnalyser.parseText(newsArticle.content);
			// collectExtractedFacts(newsArticle);
			// storeExtractedFacts(newsArticle);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			collector.fail(input);
		}
		// emit new tuple
		collector.emit(input, Arrays.asList(NamesUtil.TUPLE_TYPE_NEWS_ENTRY, sourceName, objectId));
		// ack prev tuples
		collector.ack(input);

	}


	private void collectExtractedFacts(Map<String, Object> newsArticle) {
		// Map<String, List<NewsArticleFact>> facts = new HashMap<>();
		// facts.put("geo", textAnalyser.getGeoFacts());
		// facts.put("noun", textAnalyser.getNounFacts());
		// facts.put("fio", textAnalyser.getFIOFacts());
		// newsArticle.facts = facts;
	}

	private void storeExtractedFacts(Map<String, Object> newsArticle) throws Exception {
		mongoDBClient.updateNewsArticle(newsArticle);
	}

	@Override
	public void cleanup() {
		// textAnalyser.shutdown();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(
				new Fields(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE, NamesUtil.TUPLE_FIELD_NAME_SOURCE,
						NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
