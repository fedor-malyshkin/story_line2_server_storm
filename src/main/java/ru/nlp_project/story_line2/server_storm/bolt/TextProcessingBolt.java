package ru.nlp_project.story_line2.server_storm.bolt;

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

	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;
	//@Inject
	//public ITextAnalyser textAnalyser;

	private static final long serialVersionUID = 8970948884690790271L;
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
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		String domain = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_SOURCE);
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
		collector.emit(input, Arrays.asList(domain, objectId));
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
				new Fields(NamesUtil.TUPLE_FIELD_NAME_SOURCE, NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
