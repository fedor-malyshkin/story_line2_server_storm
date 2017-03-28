package ru.nlp_project.story_line2.server_storm.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrawlerNewsArticle {

	/**
	 * Record id
	 */
	@JsonInclude(value = Include.NON_NULL)
	@JsonProperty("_id")
	public Id _id;

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
	/**
	 * фактическая дата записис в БД
	 */
	@JsonProperty("processing_date")
	public Date processingDate = null;


	/**
	 * дата новости
	 */
	@JsonProperty("publication_date")
	public Date publicationDate;
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
	@JsonProperty("source")
	public String source;
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
	 * ссылка на страницу
	 */
	@JsonProperty("image_url")
	public String imageUrl;

	@JsonProperty("image_data")
	public byte[] imageData;

	public CrawlerNewsArticle() {
		super();
	}
}
