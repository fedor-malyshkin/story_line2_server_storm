package ru.nlp_project.story_line2.server_storm.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class GroovyInterpreterImplTest {

	private GroovyInterpreterImpl testable;
	private IConfigurationManager.MasterConfiguration masterConfiguration;
	private String scriptPath;
	private IConfigurationManager.MetricsConfiguration metricsConfiguration;

	@Before
	public void setUp() throws Exception {
		scriptPath =
				"file:./src/test/resources/ru/nlp_project/story_line2/server_storm/impl/GroovyInterpreterImplTest.scripts.jar";
		masterConfiguration = new IConfigurationManager.MasterConfiguration();
		metricsConfiguration = new IConfigurationManager.MetricsConfiguration();
		masterConfiguration.contentExtractionScriptPath = scriptPath.toString();
		testable = new GroovyInterpreterImpl();
		testable.configurationManager = new ConfigurationManagerStub();
	}

	@After
	public void tearDown() throws IOException {
	}

	@Test(expected = IllegalStateException.class)
	public void testInitializeWithNonExistingScriptPath() {
		masterConfiguration.contentExtractionScriptPath = "/tmp/NON_EXISTING_DIR";
		testable.initialize();

	}

	@Test
	public void testInitializeWithSuccess() {
		testable.initialize();
		File file = new File(testable.directory,
				"ru/nlp_project/story_line2/crawler_scripts/bnkomi_ru.groovy");
		Assertions.assertThat(file.exists()).isTrue();
	}

	@Test
	public void testCallBnkomiRUShouldProcess() throws IOException {
		testable.initialize();
		Map<String, Object> extractData = testable.extractData("bnkomi.ru", "", "");
		Assertions.assertThat(extractData).isNotNull();
	}

	class ConfigurationManagerStub implements IConfigurationManager {

		@Override
		public MasterConfiguration getMasterConfiguration() {
			return masterConfiguration;
		}

		@Override
		public void initialize() {
		}

		@Override
		public MetricsConfiguration getMetricsConfiguration() {
			return metricsConfiguration;
		}
	}


}
