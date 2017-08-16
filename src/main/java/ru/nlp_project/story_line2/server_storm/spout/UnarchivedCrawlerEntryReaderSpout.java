package ru.nlp_project.story_line2.server_storm.spout;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.Config;
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
import ru.nlp_project.story_line2.server_storm.utils.DateTimeUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

/**
 * Схема работы таковы:
 * <ol>
 * <li>Выбираются записи (!archived && !in_process && archive_processed && "дата публикации меньше
 * указанной")</li>
 * <li>выбранная записи запускаются в поток с установкой флага "in_process"</li>
 * <li>после отработки при положительном исходе выставляется in_process = false и archive_processed
 * = true</li>
 * <li>после отработки при отрицательном исходе выставляется in_process = false</li>
 * <li>после того как не удалось получить ни одной записи, у всех записей у которых archived &&
 * archive_processed, сниматеся флаг archive_processed и делается пауза на сутки</li>
 * </ol>
 * 
 * 
 * @author fedor
 *
 */
public class UnarchivedCrawlerEntryReaderSpout implements IRichSpout {

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
	public void activate() {

	}

	@Override
	public void deactivate() {

	}

	@Override
	public void nextTuple() {
		LocalDate localDate = LocalDate.now();
		localDate = localDate.minusYears(1);
		try {
			// получить следующий незаархивированный CE, старше указанной даты
			Map<String, Object> crawlerEntry =
					mongoDBClient.getNextUnarchivedCrawlerEntry(DateTimeUtils.asDate(localDate));
			// there is no data
			if (null == crawlerEntry) {
				return;
			}
			collector.emit(Arrays.asList(CrawlerEntry.idString(crawlerEntry)),
					CrawlerEntry.idString(crawlerEntry));
		} catch (Exception e) {
			log.error("---", e);
		}
	}

	@Override
	public void ack(Object msgId) {
		try {
			mongoDBClient.unmarkCrawlerEntryAsInProcess((String) msgId);
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
		declarer.declare(new Fields(NamesUtil.TUPLE_FIELD_NAME_ID));

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		Map<String, Object> conf = new HashMap<>();
		// 1 day
		conf.put(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS, 1 * 1_000 * 60 * 60 * 24);
		return conf;
	}

}
