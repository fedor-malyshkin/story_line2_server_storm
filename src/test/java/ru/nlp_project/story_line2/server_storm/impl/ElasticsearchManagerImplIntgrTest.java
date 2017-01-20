package ru.nlp_project.story_line2.server_storm.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class ElasticsearchManagerImplIntgrTest {
	
	private static IConfigurationManager configurationManager;
	private ElasticsearchManagerImpl testable;

	@BeforeClass
	public static void setUpClass() throws IOException {
		configurationManager = new ConfigurationManagerImpl();
		configurationManager.initialize();
	}

	
	@Before
	public void setUp() {
		testable = new ElasticsearchManagerImpl();
		testable.configurationManager = configurationManager;
		testable.initialize();
	}

	@Test
	public void testCheckIndex() {
		fail("Not yet implemented");
	}

}
