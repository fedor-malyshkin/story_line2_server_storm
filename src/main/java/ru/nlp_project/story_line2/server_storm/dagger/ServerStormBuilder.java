package ru.nlp_project.story_line2.server_storm.dagger;

import java.util.Map;

import com.codahale.metrics.MetricRegistry;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class ServerStormBuilder {
	public static ServerStormTestModule getServerStormTestModule() {
		return serverStormTestModule;
	}


	public static void initializeTestMode() {
		ServerStormBuilder.testMode = true;
		MetricRegistry metricRegistry = new MetricRegistry();
		serverStormTestModule = new ServerStormTestModule(metricRegistry);
	}

	private static ServerStormComponent builder;
	private static boolean testMode;
	private static ServerStormModule serverStormModule;
	private static ServerStormTestModule serverStormTestModule;



	@SuppressWarnings("rawtypes")
	public static ServerStormComponent getBuilder(Map stormConf) {
		if (testMode)
			return DaggerServerStormTestComponent.builder()
					.serverStormTestModule(serverStormTestModule).build();
		if (builder == null) {

			String configurationUrl =
					(String) stormConf.get(IConfigurationManager.STORM_CONFIG_KEY);


			MetricRegistry metricRegistry = new MetricRegistry();
			serverStormModule = new ServerStormModule(configurationUrl, metricRegistry);
			builder = DaggerServerStormComponent.builder().serverStormModule(serverStormModule)
					.build();

		}
		return builder;
	}

}
