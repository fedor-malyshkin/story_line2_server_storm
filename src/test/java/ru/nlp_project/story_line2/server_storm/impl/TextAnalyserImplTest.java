package ru.nlp_project.story_line2.server_storm.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticleFact;


public class TextAnalyserImplTest {

	@BeforeClass
	public static void setUpClass() throws IOException {
		String parserConfigDir = unzipClasspathToDir(
				"ru/nlp_project/story_line2/server_storm/impl/TextAnalyserImplTest.zip", null);
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				new File(parserConfigDir + "/glr-config.yaml").toURI().toString());
	}

	private TextAnalyserImpl testable;

	@Before
	public void setUp() {
		testable = new TextAnalyserImpl(true, false);
		testable.initialize();
	}


	@Test
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


	/**
	 * Распаковать zip-файл во временную директорию и вернуть путь к ней.
	 * 
	 * @param cpZipFile
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static String unzipClasspathToDir(String cpZipFile, File newDir) throws IOException {
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(cpZipFile);
		File glrZipFile = File.createTempFile("glr-parser-config", ".zip");
		FileUtils.forceDeleteOnExit(glrZipFile);
		FileOutputStream fos = new FileOutputStream(glrZipFile);
		IOUtils.copy(resourceAsStream, fos);

		int BUFFER = 2048;

		ZipFile zip = new ZipFile(glrZipFile);
		String newPath = null;
		if (newDir == null)
			newPath = glrZipFile.getAbsolutePath().substring(0,
					glrZipFile.getAbsolutePath().length() - 4);
		else
			newPath = newDir.getAbsolutePath();

		new File(newPath).mkdir();
		Enumeration<ZipEntry> zipFileEntries = (Enumeration<ZipEntry>) zip.entries();

		// Process each entry
		while (zipFileEntries.hasMoreElements()) {
			// grab a zip file entry
			ZipEntry entry = zipFileEntries.nextElement();
			String currentEntry = entry.getName();
			File destFile = new File(newPath, currentEntry);
			// destFile = new File(newPath, destFile.getName());
			File destinationParent = destFile.getParentFile();

			// create the parent directory structure if needed
			destinationParent.mkdirs();

			if (!entry.isDirectory()) {
				BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
				int currentByte;
				// establish buffer for writing file
				byte data[] = new byte[BUFFER];

				// write the current file to disk
				FileOutputStream fos2 = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos2, BUFFER);

				// read and write until last byte is encountered
				while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, currentByte);
				}
				dest.flush();
				dest.close();
				is.close();
			}
		}
		IOUtils.closeQuietly(zip);
		FileUtils.forceDeleteOnExit(new File(newPath));
		return newPath;
	}

}
