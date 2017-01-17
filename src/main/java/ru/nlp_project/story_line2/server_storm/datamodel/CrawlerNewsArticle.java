package ru.nlp_project.story_line2.server_storm.datamodel;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.undercouch.bson4jackson.types.ObjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlerNewsArticle {

	public CrawlerNewsArticle() {
		super();
	}

	public CrawlerNewsArticle(Date creationDate, Date date, String content, String path,
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
	 * Record id
	 */
	@JsonInclude(value=Include.NON_NULL)
	@JsonProperty("_id")
	public ObjectId _id;
	/**
	 * фактическая дата записи в БД
	 */
	@JsonProperty("creation_date")
	public Date creationDate = new Date(0);
	/**
	 * дата новости
	 */
	@JsonProperty("date")
	public Date date;
	/**
	 * текст новости/статьи
	 */
	@JsonProperty("content")
	public String content;
	/**
	 * путь внутри сайта
	 */
	@JsonProperty("path")
	public String path;
	/**
	 * домен сайта (без протокола)
	 */
	@JsonProperty("domain")
	public String domain;
	/**
	 * заголовок новости/статьи
	 */
	@JsonProperty("title")
	public String title;
	/**
	 * полный адрес статьи
	 */
	@JsonProperty("url")
	public String url;
	
	/**
	 * Признак обработки сообщения.
	 */
	@JsonProperty("processed")
	public boolean processed = false;
	/**
	 * Признак обработки сообщения.
	 */
	@JsonProperty("in_process")
	public boolean inProcess = false;
}
