package ru.nlp_project.story_line2.server_storm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

import ru.nlp_project.story_line2.server_storm.bolts.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolts.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spouts.CrawlerNewsArticleReaderSpout;

/**
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * 
 * @author fedor
 *
 */
public class BaseLocalTopology {
	private static final String TOPOLOGY_NAME = "test";
	private static final String BOLT_ELASTICSEARCH_INDEXER = "elasticsearch_indexer";
	private static final String BOLT_TEXT_PROCESSOR = "text_processor";
	private static final String SPOUT_CRAWLER_NEWS_ARTICLE_READER = "crawler_news_article_reader";
	private static File confDir;

	public static void main(String args[]) throws IOException {
		// dir
		confDir = new File("/tmp/server_storm");
		FileUtils.forceMkdir(confDir);
		// glr
		unzipClasspathToDir("ru/nlp_project/story_line2/server_storm/TextAnalyserImpl.zip",
				confDir);
		// main
		InputStream resourceAsStream =
				Thread.currentThread().getContextClassLoader().getResourceAsStream(
						"ru/nlp_project/story_line2/server_storm/server_storm_config.yml");
		File mainConf = new File(
				confDir.getAbsolutePath() + File.separator + "server_storm_config.yml");
		FileOutputStream fos = new FileOutputStream(mainConf);
		IOUtils.copy(resourceAsStream, fos);
		FileUtils.forceDeleteOnExit(confDir);
		// init conf
		System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
				mainConf.toURI().toString());

		new BaseLocalTopology().run();
	}

	private void run() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SPOUT_CRAWLER_NEWS_ARTICLE_READER, new CrawlerNewsArticleReaderSpout(), 1);
		builder.setBolt(BOLT_TEXT_PROCESSOR, new TextProcessingBolt(), 1)
				.shuffleGrouping(SPOUT_CRAWLER_NEWS_ARTICLE_READER);
		builder.setBolt(BOLT_ELASTICSEARCH_INDEXER, new ElasticsearchIndexingBolt(), 1)
				.shuffleGrouping(BOLT_TEXT_PROCESSOR);

		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60 * 5);
		// conf.setMessageTimeoutSecs(secs);


		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());
		Utils.sleep(999_999_999);
		cluster.killTopology(TOPOLOGY_NAME);
		cluster.shutdown();
		

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
