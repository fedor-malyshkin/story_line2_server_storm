package ru.nlp_project.story_line2.server_storm.bolt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import ru.nlp_project.story_line2.server_storm.ITextAnalyser;
import ru.nlp_project.story_line2.server_storm.dagger.ApplicationBuilder;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class TextProcessingBolt implements IRichBolt {

	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public ITextAnalyser textAnalyser;

	private static final long serialVersionUID = 8970948884690790271L;
	private OutputCollector collector;
	private Logger logger;

	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		logger = LoggerFactory.getLogger(this.getClass());
		ApplicationBuilder.getBuilder().inject(this);
	}

	@Override
	public void execute(Tuple input) {
		String objectId = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_ID);
		String domain = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_DOMAIN);
		try {
			NewsArticle newsArticle = mongoDBClient.getNewsArticle(objectId);
			textAnalyser.parseText(newsArticle.content);
			collectExtractedFacts(newsArticle);
			storeExtractedFacts(newsArticle);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			collector.fail(input);
		}
		// emit new tuple
		collector.emit(input, Arrays.asList(domain, objectId));
		// ack prev tuples
		collector.ack(input);
	}

	private void collectExtractedFacts(NewsArticle newsArticle) {
		Map<String, List<NewsArticleFact>> facts = new HashMap<>();
		facts.put("geo", textAnalyser.getGeoFacts());
		facts.put("noun", textAnalyser.getNounFacts());
		facts.put("fio", textAnalyser.getFIOFacts());
		newsArticle.facts = facts;
	}

	private void storeExtractedFacts(NewsArticle newsArticle) throws Exception {
		mongoDBClient.updateNewsArticle(newsArticle);
	}

	@Override
	public void cleanup() {
		textAnalyser.shutdown();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(
				new Fields(NamesUtil.TUPLE_FIELD_NAME_DOMAIN, NamesUtil.TUPLE_FIELD_NAME_ID));
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

}
