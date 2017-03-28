package ru.nlp_project.story_line2.server_storm.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.server_storm.model.NewsArticleFact;


@Ignore
public class TextAnalyserImplTest {

	private static String parserConfigDir;

	@BeforeClass
	public static void setUpClass() throws IOException {
		parserConfigDir = TestUtils.unzipClasspathToDir(
				"ru/nlp_project/story_line2/server_storm/impl/TextAnalyserImplTest.zip", null);
	}
	
	
	@AfterClass
	public static void tearDownClass() throws IOException {
		FileUtils.deleteQuietly(new File(parserConfigDir));
	}

	private TextAnalyserImpl testable;

	@Before
	public void setUp() {
		testable = new TextAnalyserImpl(true, false);
		testable.initialize();
	}

	
	
	

	@Test
	@Ignore
	public void testParseText1() throws IOException {
		testable.parseText("Это фото сегодня прислал наш читатель из деревни Ермица. "
				+ "Завтра, по данным Коми ЦГМС, в Усть-Цилемском районе -40...-42°C. "
				+ "Фото Дмитрия Торопова");
		List<NewsArticleFact> fioFacts = testable.getFIOFacts();
		assertNotNull(fioFacts);
		assertTrue(fioFacts.size() > 0);
		assertEquals("торопова дмитрий", fioFacts.get(0).factValue);
		List<NewsArticleFact> nounFacts = testable.getNounFacts();
		assertNotNull(nounFacts);
		assertTrue(nounFacts.size() > 0);
		assertEquals("Это фото", nounFacts.get(0).factValue);
	}


}
