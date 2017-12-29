package ru.nlp_project.story_line2.server_storm.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistry.MetricSupplier;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager.MetricsConfiguration;
import ru.nlp_project.story_line2.server_storm.IMetricsManager;

public class MetricsManagerImpl implements IMetricsManager {

	private static final String WHOLE_COUNT = "count";
	private static final String PROCESSED_COUNT = "processed_count";
	private static final String UNPROCESSED_COUNT = "unprocessed_count";
	private final static String CRAWLER_ENTRY = "crawler_entry";
	private final static String NEWS_ARTICLE = "news_article";
	private final Logger log;
	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public MetricRegistry metricRegistry;
	private HashMap<String, Timer> timerHashMap = new HashMap<>();
	private HashMap<String, Counter> counterHashMap = new HashMap<>();
	private Slf4jReporter slfjReporter;
	private ScheduledReporter inAppInfluxDBReporter;
	private HashMap<String, IntGaugeValueSupplier> gaugeHashMap = new HashMap<>();


	@Inject
	public MetricsManagerImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	public void initialize() {
		try {
			initializeMetricsLogging();
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
		}
	}

	private void initializeMetricsLogging() throws UnknownHostException {
		MetricsConfiguration metricsConfiguration = configurationManager
				.getMetricsConfiguration();

		slfjReporter = Slf4jReporter.forRegistry(metricRegistry)
				.outputTo(LoggerFactory.getLogger("ru.nlp_project.story_line2.server_storm"))
				.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
		slfjReporter.start(metricsConfiguration.logReportingPeriod, TimeUnit.SECONDS);

		if (metricsConfiguration.enabled) {
			String hostName = InetAddress.getLocalHost().getCanonicalHostName();
			inAppInfluxDBReporter = InfluxdbReporter.forRegistry(metricRegistry)
					.protocol(new HttpInfluxdbProtocol("http", metricsConfiguration.influxdbHost,
							metricsConfiguration.influxdbPort, metricsConfiguration.influxdbUser,
							metricsConfiguration.influxdbPassword, metricsConfiguration.influxdbDb))
					// rate + dim conversions
					.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
					// filter
					.filter(MetricFilter.ALL)
					// don't skip
					.skipIdleMetrics(false)
					// hostname tag
					.tag("host", hostName)
					.tag("service", "server_storm")
					// !!! converter
					// al influxdbMetrics must be of form: "crawler_entry.bnkomi_ru.processed_count" ->
					// measurement name: "processed_count" with tags [source=bnkomi_ru, entry_type=crawler_entry] value=0.1"
					.transformer(new CategoriesMetricMeasurementTransformer("entry_type", "source"))
					.build();
			inAppInfluxDBReporter.start(metricsConfiguration.reportingPeriod, TimeUnit.SECONDS);
		}
	}


	private Counter getCounter(String entryType, String source, String counterName) {
		Counter result = counterHashMap.get(entryType + "-" + source + "-" + counterName);
		if (result == null) {
			result = createCounter(entryType, source, counterName);
			counterHashMap.put(entryType + "-" + source + "-" + counterName, result);
		}
		return result;
	}

	private Counter createCounter(String entryType, String source, String counterName) {
		String name = String
				.format("%s.%s.%s", entryType,
						source.replace(".", "_"), counterName);
		return metricRegistry.counter(name);
	}

	private Timer getTimer(String entryType, String source, String counterName) {
		Timer result = timerHashMap.get(entryType + "-" + source + "-" + counterName);
		if (result == null) {
			result = createTimer(entryType, source, counterName);
			timerHashMap.put(entryType + "-" + source + "-" + counterName, result);
		}
		return result;
	}

	private Timer createTimer(String entryType, String source, String counterName) {
		String name = String
				.format("%s.%s.%s", entryType,
						source.replace(".", "_"), counterName);
		return metricRegistry.timer(name);
	}

	private IntGaugeValueSupplier getGauge(String entryType, String source, String counterName) {
		IntGaugeValueSupplier result = gaugeHashMap.get(entryType + "-" + source + "-" + counterName);
		if (result == null) {
			result = createGauge(entryType, source, counterName);
			gaugeHashMap.put(entryType + "-" + source + "-" + counterName, result);
		}
		return result;
	}

	private IntGaugeValueSupplier createGauge(String entryType, String source, String counterName) {
		final IntGaugeValueSupplier intGaugeValueSupplier = new IntGaugeValueSupplier();
		String name = String
				.format("%s.%s.%s", entryType,
						source.replace(".", "_"), counterName);
		metricRegistry.gauge(name, intGaugeValueSupplier);
		return intGaugeValueSupplier;
	}

	@Override
	public void crawlerEntriesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(CRAWLER_ENTRY, source, WHOLE_COUNT);
		gauge.set(count);
	}

	@Override
	public void processedCrawlerEntriesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(CRAWLER_ENTRY, source, PROCESSED_COUNT);
		gauge.set(count);

	}

	@Override
	public void unprocessedCrawlerEntriesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(CRAWLER_ENTRY, source, UNPROCESSED_COUNT);
		gauge.set(count);

	}

	@Override
	public void newsArticlesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(NEWS_ARTICLE, source, WHOLE_COUNT);
		gauge.set(count);

	}

	@Override
	public void processedNewsArticlesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(NEWS_ARTICLE, source, PROCESSED_COUNT);
		gauge.set(count);

	}

	@Override
	public void unprocessedNewsArticlesCount(String source, long count) {
		IntGaugeValueSupplier gauge = getGauge(NEWS_ARTICLE, source, UNPROCESSED_COUNT);
		gauge.set(count);

	}

	private class LongGauge implements Gauge<Long> {

		private long value;

		private void set(long value) {
			this.value = value;
		}

		@Override
		public Long getValue() {
			return this.value;
		}
	}

	private class IntGaugeValueSupplier implements MetricSupplier<Gauge> {

		private LongGauge gauge = new LongGauge();

		private void set(long value) {
			gauge.set(value);
		}

		@Override
		public Gauge newMetric() {
			return gauge;
		}
	}
}