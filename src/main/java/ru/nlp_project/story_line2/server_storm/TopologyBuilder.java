package ru.nlp_project.story_line2.server_storm;

import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.Filter;
import org.apache.storm.trident.operation.Function;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.state.StateUpdater;
import org.apache.storm.tuple.Fields;

import ru.nlp_project.story_line2.server_storm.filters.ElasticsearchFeedProcessedFilter;
import ru.nlp_project.story_line2.server_storm.functions.FeedExtractionArticleFunction;
import ru.nlp_project.story_line2.server_storm.functions.FeedSplitFunction;
import ru.nlp_project.story_line2.server_storm.spout.FeedBatchSpoutType;
import ru.nlp_project.story_line2.server_storm.state.FeedDB;
import ru.nlp_project.story_line2.server_storm.state.MongoDBFeedStateFactory;
import ru.nlp_project.story_line2.server_storm.state.MongoDBFeedStateUpdater;

/**
 * 
 * Построитель топологий для кластера.
 * 
 * @author fedor
 *
 */
public class TopologyBuilder {
	private IBatchSpout feedSpout = new FeedBatchSpoutType();
	private Function feedSplit = new FeedSplitFunction();
	private Filter feedProcessedFilter = new ElasticsearchFeedProcessedFilter();
	private Function feedExtractor = new FeedExtractionArticleFunction();
	private StateFactory feedStateFactory = new MongoDBFeedStateFactory();
	private StateUpdater<FeedDB> feedUpdater = new MongoDBFeedStateUpdater();

	StormTopology createTopology() {
		TridentTopology topology = new TridentTopology();
		addFeedProcessingStream(topology);
		return topology.build();
	}

	protected void addFeedProcessingStream(TridentTopology topology) {
		topology
				// собрать feed'ы, которые требуется обработать
				.newStream("feed_spout", feedSpout)
				// разделить feed'ы на записи
				.each(new Fields("feed"), feedSplit, new Fields("feed_entry"))
				// отфильтровать ранее обработанные записи
				.filter(new Fields("feed_entry"), feedProcessedFilter)
				// извлечь из необработанных записей данные
				.each(new Fields("feed_entry"), feedExtractor, new Fields("article")).
				// сохранить данные
				partitionPersist(feedStateFactory, feedUpdater);
	}
}
