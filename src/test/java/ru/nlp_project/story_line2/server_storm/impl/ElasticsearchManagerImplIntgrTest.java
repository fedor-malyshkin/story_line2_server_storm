package ru.nlp_project.story_line2.server_storm.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

/**
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * 
 * @author fedor
 *
 */
@Ignore
public class ElasticsearchManagerImplIntgrTest {

	private static IConfigurationManager configurationManager;
	private static String parserConfigDir;
	private ElasticsearchManagerImpl testable;



	@BeforeClass
	public static void setUpClass() throws IOException {
		parserConfigDir = TestUtils.unzipClasspathToDir(
				"ru/nlp_project/story_line2/server_storm/impl/TextAnalyserImplTest.zip", null);
		configurationManager = new ConfigurationManagerImpl(null);
		configurationManager.initialize();

	}


	@AfterClass
	public static void tearDownClass() throws IOException {
		FileUtils.deleteQuietly(new File(parserConfigDir));
	}


	@Before
	public void setUp() {
		testable = new ElasticsearchManagerImpl();
		testable.configurationManager = configurationManager;
		testable.initialize();
	}

}
