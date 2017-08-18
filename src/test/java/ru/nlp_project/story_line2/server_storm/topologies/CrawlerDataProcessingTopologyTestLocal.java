package ru.nlp_project.story_line2.server_storm.topologies;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.junit.Ignore;
import org.junit.Test;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class CrawlerDataProcessingTopologyTestLocal {

	private LocalCluster cluster;

	@Test
	@Ignore
	public void testMain() throws InterruptedException {
		cluster = new LocalCluster();
		// Map<String, Object> conf = Utils.readStormConfig();
		Config conf = new Config();
		conf.put(IConfigurationManager.STORM_CONFIG_KEY,
				"http://datahouse01.nlp-project.ru:9000/server_storm.yaml");
		conf.setNumWorkers(1);
		conf.setMaxSpoutPending(5000);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60 * 5);

		cluster.submitTopology(CrawlerDataProcessingTopology.TOPOLOGY_NAME, conf,
				CrawlerDataProcessingTopology.createTopology());
		Thread.sleep(1_000 * 60 * 60);
	}

}
