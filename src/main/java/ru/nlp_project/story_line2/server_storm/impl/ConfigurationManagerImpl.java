package ru.nlp_project.story_line2.server_storm.impl;

import javax.inject.Inject;
import ru.nlp_project.story_line2.config.ConfigurationException;
import ru.nlp_project.story_line2.config.ConfigurationManager;
import ru.nlp_project.story_line2.config.IConfigurationSupplier;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;

public class ConfigurationManagerImpl implements IConfigurationManager {

	private MasterConfiguration masterConfiguration;
	private MetricsConfiguration metricsConfiguration;
	private String configurationUrl;

	public ConfigurationManagerImpl(String configurationUrl) {
		this.configurationUrl = configurationUrl;
	}

	@Override
	public void initialize() {
		try {
			readConfiguration();
		} catch (ConfigurationException e) {
			throw new IllegalStateException(e);
		}

	}

	private void readConfiguration() throws ConfigurationException {
		IConfigurationSupplier supplier =
				ConfigurationManager.getConfigurationSupplier(configurationUrl);
		masterConfiguration = supplier.getConfigurationObjectFromPath(configurationUrl,
				MasterConfiguration.class);
		metricsConfiguration = supplier.getConfigurationObjectFromPath(configurationUrl,
				MetricsConfiguration.class);
	}

	public MasterConfiguration getMasterConfiguration() {
		return masterConfiguration;
	}

	public MetricsConfiguration getMetricsConfiguration() {
		return metricsConfiguration;
	}
}
