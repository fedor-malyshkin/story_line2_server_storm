package ru.nlp_project.story_line2.server_storm.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
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

/**
 * Менеджер метрик.
 *
 * В связи с разными видами метрик и нюансами формирования тэгов в связке
 * "metrics" + "metrics_influxdb from davidB" будет несколько репортеров в InfluxDB, с разными
 * префиксами и разными способомаи формирования тэгов.
 */
public class MetricsManagerImpl implements IMetricsManager {

	private static final String PREFIX_TOPO_STATS = "topo_stats";
	private static final String PREFIX_METHOD_STATS = "method_stats";
	private static final String PREFIX_DATA_STATS = "data_stats";
	private static final String DB_WHOLE_COUNT = "mongodb_records_count";
	private static final String SEARCH_WHOLE_COUNT = "search_records_count";
	private static final String DB_PROCESSED_COUNT = "mongodb_records_processed_count";
	private static final String DB_UNPROCESSED_COUNT = "mongodb_records_unprocessed_count";
	private final static String CRAWLER_ENTRY = "crawler_entry";
	private final static String NEWS_ARTICLE = "news_article";
	private static final String METHOD_CALL_DURATION = "method_call_duration";
	private final Logger log;
	@Inject
	public IConfigurationManager configurationManager;
	@Inject
	public MetricRegistry metricRegistry;
	private HashMap<String, Timer> timerHashMap = new HashMap<>();
	private HashMap<String, Counter> counterHashMap = new HashMap<>();
	private Slf4jReporter slfjReporter;
	private ScheduledReporter dataStatsInfluxDBReporter;
	private HashMap<String, LongGaugeValueSupplier> gaugeHashMap = new HashMap<>();
	private ScheduledReporter methodStatsInfluxDBReporter;
	private ScheduledReporter topoStatsInfluxDBReporter;


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
			HttpInfluxdbProtocol httpInfluxdbProtocol = new HttpInfluxdbProtocol("http",
					metricsConfiguration.influxdbHost,
					metricsConfiguration.influxdbPort, metricsConfiguration.influxdbUser,
					metricsConfiguration.influxdbPassword, metricsConfiguration.influxdbDb);
			// data
			dataStatsInfluxDBReporter = InfluxdbReporter.forRegistry(metricRegistry)
					.protocol(httpInfluxdbProtocol)
					// rate + dim conversions
					.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
					// filter
					.filter(new PrefixMetricFilter(PREFIX_DATA_STATS, true))
					// don't skip
					.skipIdleMetrics(false)
					// hostname tag
					.tag("host", hostName)
					.tag("service", "server_storm")
					// converter
					// al influxdbMetrics must be of form: "PREFIX_DATA_STATS.crawler_entry.bnkomi_ru.processed_count" ->
					// measurement name: "processed_count" with tags [stats_type=PREFIX_DATA_STATS, source=bnkomi_ru, entry_type=crawler_entry] value=0.1"
					.transformer(
							new CategoriesMetricMeasurementTransformer("stats_type", "entry_type", "source"))
					.build();
			dataStatsInfluxDBReporter.start(metricsConfiguration.reportingPeriod, TimeUnit.SECONDS);
			// methods
			methodStatsInfluxDBReporter = InfluxdbReporter.forRegistry(metricRegistry)
					.protocol(httpInfluxdbProtocol)
					// rate + dim conversions
					.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
					// filter
					.filter(new PrefixMetricFilter(PREFIX_METHOD_STATS, true))
					// don't skip
					.skipIdleMetrics(false)
					// hostname tag
					.tag("host", hostName)
					.tag("service", "server_storm")
					// !!! converter
					// converter
					// al influxdbMetrics must be of form: "PREFIX_METHOD_STATS.ClassCaller.InvocableClass.Method.Duration" ->
					// measurement name: "Duration" with tags [stats_type=PREFIX_METHOD_STATS, caller=ClassCaller,class=InvocableClass, method=Method] value=0.1"
					.transformer(
							new CategoriesMetricMeasurementTransformer("stats_type", "caller", "class", "method"))
					.build();
			methodStatsInfluxDBReporter.start(metricsConfiguration.reportingPeriod, TimeUnit.SECONDS);
			// topo
			topoStatsInfluxDBReporter = InfluxdbReporter.forRegistry(metricRegistry)
					.protocol(httpInfluxdbProtocol)
					// rate + dim conversions
					.convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS)
					// filter
					.filter(new PrefixMetricFilter(PREFIX_TOPO_STATS, true))
					// don't skip
					.skipIdleMetrics(false)
					// hostname tag
					.tag("host", hostName)
					.tag("service", "server_storm")
					// !!! converter FILLL
					// al influxdbMetrics must be of form: "PREFIX_TOPO_STATS.BOLT.ContentExtractingBolt.invocation_count" ->
					// measurement name: "invocation_count" with tags [stats_type=PREFIX_TOPO_STATS, type=BOLT, part_name=ContentExtractingBolt] value=0.1"
					.transformer(
							new CategoriesMetricMeasurementTransformer("stats_type", "type", "part_name"))
					.build();
			topoStatsInfluxDBReporter.start(metricsConfiguration.reportingPeriod, TimeUnit.SECONDS);
		}
	}

	String generateTopoStatsMetricName(String type, String partName) {
		throw new IllegalArgumentException();
	}

	private String generateMethodStatMetricName(Class caller, Class clazz, String method,
			String measurementName) {
		return String
				.format("%s.%s.%s.%s.%s", PREFIX_METHOD_STATS, caller.getSimpleName(),
						clazz.getSimpleName(), method, measurementName);
	}

	private String generateDataStatMetricName(String entryType, String source,
			String measurementName) {
		return String
				.format("%s.%s.%s.%s", PREFIX_DATA_STATS, entryType,
						source.replace(".", "_"), measurementName);
	}


	private Counter getCounter(String name) {
		Counter result = counterHashMap.get(name);
		if (result == null) {
			result = createCounter(name);
			counterHashMap.put(name, result);
		}
		return result;
	}

	private Counter createCounter(String name) {
		return metricRegistry.counter(name);
	}

	private Timer getTimer(String name) {
		Timer result = timerHashMap.get(name);
		if (result == null) {
			result = createTimer(name);
			timerHashMap.put(name, result);
		}
		return result;
	}

	private Timer createTimer(String name) {
		return metricRegistry.timer(name);
	}

	private LongGaugeValueSupplier getGauge(String name) {
		LongGaugeValueSupplier result = gaugeHashMap.get(name);
		if (result == null) {
			result = createGauge(name);
			gaugeHashMap.put(name, result);
		}
		return result;
	}

	private LongGaugeValueSupplier createGauge(String name) {
		final LongGaugeValueSupplier longGaugeValueSupplier = new LongGaugeValueSupplier();
		metricRegistry.gauge(name, longGaugeValueSupplier);
		return longGaugeValueSupplier;
	}

	@Override
	public void crawlerEntriesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(CRAWLER_ENTRY, source, DB_WHOLE_COUNT));
		gauge.set(count);
	}

	@Override
	public void processedCrawlerEntriesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(CRAWLER_ENTRY, source, DB_PROCESSED_COUNT));
		gauge.set(count);

	}

	@Override
	public void unprocessedCrawlerEntriesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(CRAWLER_ENTRY, source, DB_UNPROCESSED_COUNT));
		gauge.set(count);

	}

	@Override
	public void newsArticlesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(NEWS_ARTICLE, source, DB_WHOLE_COUNT));
		gauge.set(count);

	}

	@Override
	public void processedNewsArticlesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(NEWS_ARTICLE, source, DB_PROCESSED_COUNT));
		gauge.set(count);

	}

	@Override
	public void unprocessedNewsArticlesCountDB(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(NEWS_ARTICLE, source, DB_UNPROCESSED_COUNT));
		gauge.set(count);

	}

	@Override
	public void newsArticlesCountSearch(String source, long count) {
		LongGaugeValueSupplier gauge = getGauge(
				generateDataStatMetricName(NEWS_ARTICLE, source, SEARCH_WHOLE_COUNT));
		gauge.set(count);
	}


	@Override
	public void callDuration(Class caller, Class clazz, String methodName, long callDuration) {
		Timer timer = getTimer(
				generateMethodStatMetricName(caller, clazz, methodName, METHOD_CALL_DURATION));
		timer.update(callDuration, TimeUnit.MILLISECONDS);
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

	private class LongGaugeValueSupplier implements MetricSupplier<Gauge> {

		private LongGauge gauge = new LongGauge();

		private void set(long value) {
			gauge.set(value);
		}

		@Override
		public Gauge newMetric() {
			return gauge;
		}
	}


	private class PrefixMetricFilter implements MetricFilter {

		private boolean matchPrefix = true;
		private String prefix = null;

		PrefixMetricFilter(String prefix, boolean matchPrefix) {
			this.prefix = prefix;
			this.matchPrefix = matchPrefix;
		}

		@Override
		public boolean matches(String name, Metric metric) {
			return matchPrefix == name.startsWith(prefix);
		}
	}
}