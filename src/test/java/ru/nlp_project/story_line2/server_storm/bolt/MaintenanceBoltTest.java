package ru.nlp_project.story_line2.server_storm.bolt;

import static org.mockito.Mockito.*;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.MAINTENANCE_COMMAND_FIELD_NAME_COMMAND;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.MAINTENANCE_COMMAND_RELOAD_SCRIPS;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IImageDownloader;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.ISearchManager;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormBuilder;
import ru.nlp_project.story_line2.server_storm.dagger.ServerStormTestModule;
import ru.nlp_project.story_line2.server_storm.impl.TestUtils.OutputCollectorStub;
import ru.nlp_project.story_line2.server_storm.impl.TestUtils.TupleStub;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

public class MaintenanceBoltTest {


	private ServerStormTestModule serverStormTestModule;
	private IMongoDBClient mongoDBClient;
	private IGroovyInterpreter groovyInterpreter;
	private IImageDownloader imageDownloader;
	private MaintenanceBolt testable;
	private OutputCollectorStub outputCollectorHolder;
	private IConfigurationManager configurationManager;
	private ISearchManager searchManager;


	@Before
	public void setUp() {
		// dagger
		ServerStormBuilder.initializeTestMode();
		serverStormTestModule = ServerStormBuilder.getServerStormTestModule();

		// mocks
		mongoDBClient = mock(IMongoDBClient.class);
		serverStormTestModule.mongoDBClient = mongoDBClient;

		groovyInterpreter = mock(IGroovyInterpreter.class);
		serverStormTestModule.groovyInterpreter = groovyInterpreter;

		configurationManager = mock(IConfigurationManager.class);
		serverStormTestModule.configurationManager = configurationManager;

		searchManager = mock(ISearchManager.class);
		serverStormTestModule.searchManager = searchManager;

		Map<String, Object> config = new HashMap<String, Object>();
		testable = new MaintenanceBolt();

		outputCollectorHolder = new OutputCollectorStub();
		testable.prepare(config, null, outputCollectorHolder);
	}


	/**
	 * @throws Exception
	 */
	@Test
	public void testExecute_ReloadScripts() throws Exception {
		Map<String, String> maintenanceCommand = new HashMap<>();
		maintenanceCommand
				.put(MAINTENANCE_COMMAND_FIELD_NAME_COMMAND, MAINTENANCE_COMMAND_RELOAD_SCRIPS);

		TupleStub tuple = new TupleStub();
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_TUPLE_TYPE, NamesUtil.TUPLE_TYPE_MAINTENANCE_COMMAND);
		tuple.put(NamesUtil.TUPLE_FIELD_NAME_MAINTENANCE_COMMAND, maintenanceCommand);

		// exec
		testable.execute(tuple);

		verify(groovyInterpreter).reloadScripts();
	}


}
