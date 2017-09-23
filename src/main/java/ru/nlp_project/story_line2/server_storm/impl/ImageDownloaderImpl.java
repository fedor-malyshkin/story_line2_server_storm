package ru.nlp_project.story_line2.server_storm.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nlp_project.story_line2.server_storm.IImageDownloader;

public class ImageDownloaderImpl implements IImageDownloader {

	private final Logger log;

	@Inject
	public ImageDownloaderImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public byte[] downloadImage(String url) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(20_000);
		URL url1;
		InputStream inputStream;
		try {
			url1 = new URL(url);
		} catch (MalformedURLException e) {
			log.warn("Incorrect url: '{}': {}", url, e.getMessage());
			return new byte[]{};
		}
		try {
			inputStream = url1.openStream();
		} catch (IOException e) {
			log.warn("Cannot open connection to url: '{}' : {}", url, e.getMessage());
			return new byte[]{};
		}
		try {
			IOUtils.copy(inputStream, outputStream);
		} catch (IOException e) {
			log.warn("Cannot copy data from url: '{}' : {}", url, e.getMessage());
		}
		return outputStream.toByteArray();
	}

	public void initialize() {
		initializeAllTrustCert();
	}

	// see:
	// http://stackoverflow.com/questions/875467/java-client-certificates-over-https-ssl/876785#876785
	private void initializeAllTrustCert() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}};

		// Ignore differences between given hostname and certificate hostname
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
		} catch (Exception ignored) {
		}

	}

}
