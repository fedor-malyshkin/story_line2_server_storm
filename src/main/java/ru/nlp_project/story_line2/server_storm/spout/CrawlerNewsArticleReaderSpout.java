package ru.nlp_project.story_line2.server_storm.spout;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class CrawlerNewsArticleReaderSpout implements IRichSpout {
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;
	private Logger log;

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(conf).inject(this);
	}

	@Override
	public void close() {
		mongoDBClient.shutdown();
	}


	@Override
	public void activate() {}

	@Override
	public void deactivate() {}

	/**
	 * TODO: реализовать более сложное извлечение записей, т.к. текущий вариант, просто по
	 * сортировке неработоспосбоен при большом кол-ве записей или при наличии записей сделанных в
	 * одно время. Нужно, что-то вроде кэша забирающего данные на определенную дату/время и
	 * выдающего их из себя пока есть что-то в наличии, после чего забирает следующую партию и т.д.
	 * 
	 */
	@Override
	public void nextTuple() {

		try {
			CrawlerEntry crawlerNewsArticle = mongoDBClient.getNextUnprocessedCrawlerEntry();
			// there is no data
			if (null == crawlerNewsArticle)
				return;
			String id = mongoDBClient.writeNewNewsArticle(crawlerNewsArticle);
			collector.emit(Arrays.asList(crawlerNewsArticle.source, id), id);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@Override
	public void ack(Object msgId) {
		try {
			mongoDBClient.markCrawlerEntryAsProcessed((String) msgId);
			mongoDBClient.markNewsArticleAsProcessed((String) msgId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void fail(Object msgId) {
		try {
			mongoDBClient.unmarkCrawlerEntryAsInProcess((String) msgId);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
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
