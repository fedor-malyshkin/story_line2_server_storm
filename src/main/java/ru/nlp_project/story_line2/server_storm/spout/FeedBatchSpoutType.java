package ru.nlp_project.story_line2.server_storm.spout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.IBatchSpout;
import org.apache.storm.tuple.Fields;

import ru.nlp_project.story_line2.server_storm.dagger.ApplicationBuilder;

public class FeedBatchSpoutType implements IBatchSpout {
	@Inject
	public IFeedSupplier feedSupplier;

	private static final long serialVersionUID = 1L;

	public FeedBatchSpoutType() {

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map conf, TopologyContext context) {
		ApplicationBuilder.getBuilder().inject(this);
	}

	@Override
	public void emitBatch(long batchId, TridentCollector collector) {
		String nextFeed = feedSupplier.getNextFeed();
		try {
			InputStream inputStream = new URL(nextFeed).openStream();
			String feed = IOUtils.toString(inputStream);
			collector.emit(Arrays.asList(feed));
		} catch (IOException e) {
			e.printStackTrace();
			collector.reportError(e);
		}
	}

	@Override
	public void ack(long batchId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {

	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}

	@Override
	public Fields getOutputFields() {
		return new Fields("feed");
	}

}
