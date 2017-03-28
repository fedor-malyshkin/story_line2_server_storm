package ru.nlp_project.story_line2.server_storm.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticle {

	public NewsArticle() {
		super();
	}

	public NewsArticle(CrawlerNewsArticle crawlerNewsArticle) {
		clone(crawlerNewsArticle);
	}

	protected void clone(CrawlerNewsArticle other) {
		this.crawlerId = other._id;
		this.creationDate = new Date();
		this.publicationDate = new Date(other.publicationDate.getTime());
		this.content = other.content;
		this.path = other.path;
		this.source = other.source;
		this.title = other.title;
		this.url = other.url;
		this.imageUrl = other.imageUrl;
		this.imageData = other.imageData;
	}

	/**
	 * Record id
	 */
	@JsonInclude(value = Include.NON_NULL)
	@JsonProperty("_id")
	public Id _id;

	/**
	 * Crawler "news" record id
	 */
	@JsonProperty("crawler_id")
	public Id crawlerId;
	/**
	 * фактическая дата записи в БД
	 */
	@JsonProperty("creation_date")
	public Date creationDate = null;
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
	 * дата новости
	 */
	@JsonProperty("publication_date")
	public Date publicationDate;
	/**
	 * домен сайта (без протокола)
	 */
	@JsonProperty("source")
	public String source;
	/**
	 * ссылка на страницу
	 */
	@JsonProperty("image_url")
	public String imageUrl;

	@JsonProperty("image_data")
	public byte[] imageData;



	@JsonInclude(value = Include.NON_NULL)
	@JsonProperty("facts")
	public Map<String, List<NewsArticleFact>> facts = new HashedMap<>();

}
