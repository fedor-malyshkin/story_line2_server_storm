package ru.nlp_project.story_line2.server_storm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface IConfigurationManager {

	public final static String CONFIGURATION_SYSTEM_KEY =
			"ru.nlp_project.story_line2.server_storm.config";

	// To ignore any unknown properties in JSON input without exception:
	@JsonIgnoreProperties(ignoreUnknown = true)
	class MasterConfiguration {

		@JsonProperty("server_storm.mongodb.connection.url")
		public String mongoDBConnectionUrl;
		@JsonProperty("server_storm.elasticsearch.host.name")
		public String elasticsearchHostName;
		@JsonProperty("server_storm.elasticsearch.port")
		public int elasticsearchPort;
	}


	MasterConfiguration getMasterConfiguration();

	void initialize();

	String getAbsolutePath(String grammarPath);

}
