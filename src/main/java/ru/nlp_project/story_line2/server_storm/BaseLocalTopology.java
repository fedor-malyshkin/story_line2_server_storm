package ru.nlp_project.story_line2.server_storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

import ru.nlp_project.story_line2.server_storm.bolts.ElasticsearchIndexingBolt;
import ru.nlp_project.story_line2.server_storm.bolts.TextProcessingBolt;
import ru.nlp_project.story_line2.server_storm.spouts.CrawlerNewsArticleReaderSpout;

public class BaseLocalTopology {
	private static final String CLUSTER_NAME = "test";
	private static final String BOLT_ELASTICSEARCH_INDEXER = "elasticsearch_indexer";
	private static final String BOLT_TEXT_PROCESSOR = "text_processor";
	private static final String SPOUT_CRAWLER_NEWS_ARTICLE_READER = "crawler_news_article_reader";

	public static void main(String args[]) {
		new BaseLocalTopology().run();
		System.out.println(System.getProperties());
	}

	private void run() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SPOUT_CRAWLER_NEWS_ARTICLE_READER, new CrawlerNewsArticleReaderSpout(), 1);
		builder.setBolt(BOLT_TEXT_PROCESSOR, new TextProcessingBolt(), 1)
				.shuffleGrouping(SPOUT_CRAWLER_NEWS_ARTICLE_READER);
		builder.setBolt(BOLT_ELASTICSEARCH_INDEXER, new ElasticsearchIndexingBolt(), 1)
				.shuffleGrouping(BOLT_TEXT_PROCESSOR);

		Config conf = new Config();
		conf.setDebug(false);
		conf.setNumWorkers(1);
		// время обработки не более 5 минут
		conf.setMessageTimeoutSecs(60*5);
		//conf.setMessageTimeoutSecs(secs);
		

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology(CLUSTER_NAME, conf, builder.createTopology());
		Utils.sleep(999_999_999);
/*
		cluster.killTopology("test");
		cluster.shutdown();
*/

	}

}
