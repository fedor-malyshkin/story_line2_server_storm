package ru.nlp_project.story_line2.server_storm;

import java.io.File;
import java.io.IOException;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.utils.Utils;

/**
 * Run with JVM args
 * "-Dru.nlp_project.story_line2.server_storm.config=file:${workspace_loc:server_storm}/src/main/resources/ru/nlp_project/story_line2/server_storm/server_storm_config.yml"
 * 
 * @author fedor
 *
 */
public class ClientLocalTopology {

	private static final String TOPOLOGY_NAME = "drpc-test";
	private static File confDir;

	public static void main(String args[]) throws IOException {
		// // dir
		// confDir = new File("/tmp/server_storm_drpc");
		// FileUtils.forceMkdir(confDir);
		// // main
		// InputStream resourceAsStream =
		// Thread.currentThread().getContextClassLoader().getResourceAsStream(
		// "ru/nlp_project/story_line2/server_storm/server_storm_config.yml");
		// File mainConf =
		// new File(confDir.getAbsolutePath() + File.separator + "server_storm_config.yml");
		// FileOutputStream fos = new FileOutputStream(mainConf);
		// IOUtils.copy(resourceAsStream, fos);
		// FileUtils.forceDeleteOnExit(confDir);
		// // init conf
		// System.setProperty(IConfigurationManager.CONFIGURATION_SYSTEM_KEY,
		// mainConf.toURI().toString());

		new ClientLocalTopology().run();
	}

	private void run() {

		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60 * 5);


		LocalCluster cluster = new LocalCluster();
		// build topology
		TopologyBuilder builder = new TopologyBuilder();

		cluster.submitTopology(TOPOLOGY_NAME, conf, builder.createTopology());

		Utils.sleep(999_999_999);

		cluster.killTopology(TOPOLOGY_NAME);
		cluster.shutdown();

	}



}
