package ru.nlp_project.story_line2.server_storm.functions;

import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.TUPLE_FIELD_NAME_ID;

import java.util.Map;

import javax.inject.Inject;

import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.TridentOperationContext;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;

public class ArchiveCrawlerEntryFunction implements Function {

	private static final long serialVersionUID = 1L;
	private Logger log;


	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IConfigurationManager configurationManager;

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
		String id = (String) tuple.getValueByField(TUPLE_FIELD_NAME_ID);
		try {
			Map<String, Object> crawlerEntry = mongoDBClient.getCrawlerEntry(id);
			CrawlerEntry.rawContent(crawlerEntry, null);
			crawlerEntry.put(IMongoDBClient.CRAWLER_ENTRY_FIELD_ARCHIVED, true);
			mongoDBClient.updateCrawlerEntry(crawlerEntry);
			collector.emit(new Values(id));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
