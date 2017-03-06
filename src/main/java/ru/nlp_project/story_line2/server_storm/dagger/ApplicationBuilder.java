package ru.nlp_project.story_line2.server_storm.dagger;

public class ApplicationBuilder {

	private static ApplicationComponent builder;


	public static ApplicationComponent getBuilder() {
		if (builder == null)
			builder = DaggerApplicationComponent.create();

		return builder;

	}

}
