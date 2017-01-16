package ru.nlp_project.story_line2.server_storm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface IConfigurationManager {

	public final static String CONFIGURATION_SYSTEM_KEY =
			"ru.nlp_project.story_line2.server_storm.config";

	// To ignore any unknown properties in JSON input without exception:
	@JsonIgnoreProperties(ignoreUnknown = true)
	class MasterConfiguration {

		@JsonProperty("server_storm.connection.url")
		public String connectionUrl;
	}


	MasterConfiguration getMasterConfiguration();

	void initialize();

	String getAbsolutePath(String grammarPath);

}
