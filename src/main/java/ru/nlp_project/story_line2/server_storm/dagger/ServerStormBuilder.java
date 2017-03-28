package ru.nlp_project.story_line2.server_storm.dagger;

import java.util.Map;

import com.codahale.metrics.MetricRegistry;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class ServerStormBuilder {
	private static ServerStormComponent builder;

	@SuppressWarnings("rawtypes")
	public static ServerStormComponent getBuilder(Map stormConf) {
		if (builder == null) {
			MetricRegistry metricRegistry = new MetricRegistry();
			String configurationUrl =
					(String) stormConf.get(IConfigurationManager.STORM_CONFIG_KEY);
			ServerStormModule serverStormModule =
					new ServerStormModule(configurationUrl, metricRegistry);
			builder = DaggerServerStormComponent.builder().serverStormModule(serverStormModule)
					.build();
		}

		return builder;

	}

}
