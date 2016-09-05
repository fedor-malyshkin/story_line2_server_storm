package ru.nlp_project.story_line2.server_storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

import ru.nlp_project.story_line2.server_storm.bolts.NameExtractorBolt;
import ru.nlp_project.story_line2.server_storm.bolts.NounExtractorBolt;
import ru.nlp_project.story_line2.server_storm.spunks.MongoDBNewsReaderSpout;

public class BaseLocalTopology {
	private static final String BOLT_NAME_EXTRACTOR = "name_extractor";
	private static final String BOLT_NOUN_EXTRACTOR = "noun_extractor";
	private static final String SPUNK_MONGODB_NEWS_READER = "mongodb_news_reader";

	public static void main(String args[]) {
		new BaseLocalTopology().run();
	}

	private void run() {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(SPUNK_MONGODB_NEWS_READER, new MongoDBNewsReaderSpout(), 1);
		builder.setBolt(BOLT_NOUN_EXTRACTOR, new NounExtractorBolt(), 1)
				.shuffleGrouping(SPUNK_MONGODB_NEWS_READER);
		builder.setBolt(BOLT_NAME_EXTRACTOR, new NameExtractorBolt(), 1)
				.shuffleGrouping(BOLT_NOUN_EXTRACTOR);

		Config conf = new Config();
		conf.setDebug(true);
		conf.setNumWorkers(2);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("test", conf, builder.createTopology());
		Utils.sleep(999_999_999);
/*
		cluster.killTopology("test");
		cluster.shutdown();
*/

	}

}
