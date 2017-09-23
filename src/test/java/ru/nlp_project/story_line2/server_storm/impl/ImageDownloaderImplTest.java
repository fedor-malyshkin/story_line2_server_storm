package ru.nlp_project.story_line2.server_storm.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;

public class ImageDownloaderImplTest {

	private ImageDownloaderImpl testable;

	@Before
	public void setUp() {
		testable = new ImageDownloaderImpl();
		testable.initialize();
	}

	@Test
	public void downloadImage() throws Exception {
		byte[] bytes = testable.downloadImage("");
		assertThat(bytes).isNotNull().isEmpty();
	}


	@Test
	public void downloadImageFromClasspath() throws Exception {
		URL url = Thread.currentThread().getContextClassLoader().getResource("logback-test.xml");
		byte[] bytes = testable.downloadImage(url.toString());
		assertThat(bytes).isNotNull().isNotEmpty();
	}
}