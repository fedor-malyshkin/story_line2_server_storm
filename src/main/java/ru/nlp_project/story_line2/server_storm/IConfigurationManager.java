package ru.nlp_project.story_line2.server_storm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface IConfigurationManager {

	public final static String STORM_CONFIG_KEY =
			"story_line2.server_storm.config";

	MasterConfiguration getMasterConfiguration();

	MetricsConfiguration getMetricsConfiguration();

	void initialize();

	// To ignore any unknown properties in JSON input without exception:
	@JsonIgnoreProperties(ignoreUnknown = true)
	class MasterConfiguration {

		@JsonProperty("server_storm.content_extraction.script_path")
		public String contentExtractionScriptPath;
		@JsonProperty("server_storm.mongodb.connection_url")
		public String mongoDBConnectionUrl;
		@JsonProperty("server_storm.elasticsearch.hostname")
		public String elasticsearchHostName;
		@JsonProperty("server_storm.elasticsearch.port")
		public int elasticsearchPort;
		@JsonProperty("server_storm.elasticsearch.index.real_name")
		public String elasticsearchRealIndex;
		@JsonProperty("server_storm.elasticsearch.index.read_alias")
		public String elasticsearchReadAlias;
		@JsonProperty("server_storm.elasticsearch.index.write_alias")
		public String elasticsearchWriteAlias;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	class MetricsConfiguration {

		@JsonProperty("server_storm.metrics.enabled")
		public boolean enabled = false;
		@JsonProperty("server_storm.metrics.collectiong_pertiod")
		public int collectingPeriod = 0;
		@JsonProperty("server_storm.influxdb_metrics.enabled")
		public boolean influxdbEnabled = false;
		@JsonProperty("server_storm.influxdb_metrics.influxdb_host")
		public String influxdbHost;
		@JsonProperty("server_storm.influxdb_metrics.influxdb_port")
		public int influxdbPort;
		@JsonProperty("server_storm.influxdb_metrics.influxdb_db")
		public String influxdbDb;
		@JsonProperty("server_storm.influxdb_metrics.influxdb_user")
		public String influxdbUser;
		@JsonProperty("server_storm.influxdb_metrics.influxdb_password")
		public String influxdbPassword;
		@JsonProperty("server_storm.influxdb_metrics.reporting_period")
		public int reportingPeriod = 0;
		@JsonProperty("server_storm.influxdb_metrics.log_reporting_period")
		public int logReportingPeriod = 0;
	}
}
