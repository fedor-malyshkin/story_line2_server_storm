package ru.nlp_project.story_line2.server_storm.datamodel;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticle {

	public NewsArticle() {
		super();
	}

	public NewsArticle(Date creationDate, Date date, String content, String path,
			String domain, String title, String url) {
		super();
		this.creationDate = creationDate;
		this.date = date;
		this.content = content;
		this.path = path;
		this.domain = domain;
		this.title = title;
		this.url = url;
	}

	/**
	 * фактическая дата записис в БД
	 */
	@JsonProperty("creation_date")
	Date creationDate = new Date(System.currentTimeMillis());
	/**
	 * дата новости
	 */
	@JsonProperty("date")
	Date date;
	/**
	 * текст новости/статьи
	 */
	@JsonProperty("content")
	String content;
	/**
	 * путь внутри сайта
	 */
	@JsonProperty("path")
	String path;
	/**
	 * домен сайта (без протокола)
	 */
	@JsonProperty("domain")
	String domain;
	/**
	 * заголовок новости/статьи
	 */
	@JsonProperty("title")
	String title;
	/**
	 * полный адрес статьи
	 */
	@JsonProperty("url")
	String url;
}
