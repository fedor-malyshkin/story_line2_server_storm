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
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;


public class MaintenanceBolt implements IRichBolt {

	private static final long serialVersionUID = 1L;
	@Inject
	public IMongoDBClient mongoDBClient;
	@Inject
	public IGroovyInterpreter groovyInterpreter;
	@Inject
	public ISearchManager searchManager;
	@Inject
	public IConfigurationManager configurationManager;

	private OutputCollector collector;
	private Logger log;

	@Override
	public void cleanup() {
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields());
	}

	@Override
	public void execute(Tuple input) {
		String tupleType = input.getStringByField(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE);

		switch (tupleType) {
			case NamesUtil.TUPLE_TYPE_MAINTENANCE_COMMAND: {
				processMaintenanceCommand(input);
				break;
			}
			default: {
			}
		}
	}


	private void processMaintenanceCommand(Tuple input) {
		Map<String, Object> maintenanceCommand = (Map<String, Object>) input
				.getValueByField(TUPLE_FIELD_NAME_MAINTENANCE_COMMAND);
		String command = (String) maintenanceCommand
				.get(NamesUtil.MAINTENANCE_COMMAND_FIELD_NAME_COMMAND);
		switch (command) {
			case NamesUtil.MAINTENANCE_COMMAND_RELOAD_SCRIPS: {
				maintenanceReloadScripts();
			}
			break;
			case NamesUtil.MAINTENANCE_COMMAND_REINDEX_SOURCE: {
				maintenanceReindexSource(maintenanceCommand);
			}
			break;
			default: {
				throw new IllegalArgumentException("Unknown maintenance command: " + command);
			}
		}

	}

	private void maintenanceReindexSource(
			Map<String, Object> maintenanceCommand) {
		String source = (String) maintenanceCommand
				.get(NamesUtil.MAINTENANCE_COMMAND_FIELD_NAME_PARAM1);
		if (source == null || source.isEmpty()) {
			log.warn("No source specified in 'reindex source' maintenance command.");
			return;
		}
		if (NamesUtil.MAINTENANCE_COMMAND_REINDEX_SOURCE_PARAM1_ALL.equals(source)) {
			try {
				searchManager.deleteAllDocuments();
				mongoDBClient.deleteAllNewsArticles();
				mongoDBClient.unmarkAllCrawlerEntriesAsProcessed();
			} catch (Exception e) {
				log.error(
						String.format("Exception while reindex all sources: '%s'", e.getMessage()),
						e);
			}
		} else {
			try {
				searchManager.deleteDocuments(source);
				mongoDBClient.deleteNewsArticles(source);
				mongoDBClient.unmarkCrawlerEntriesAsProcessed(source);
			} catch (Exception e) {
				log.error(
						String.format("Exception while reindex source '%s': '%s'", source, e.getMessage()),
						e);
			}
		}
	}

	private void maintenanceReloadScripts() {
		groovyInterpreter.reloadScripts();
	}


	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}


	@SuppressWarnings("rawtypes")
	@Override
	public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		log = LoggerFactory.getLogger(this.getClass());
		ServerStormBuilder.getBuilder(stormConf).inject(this);
	}
}
