package ru.nlp_project.story_line2.server_storm.datamodel;

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
		this.crawler_id = other._id;
		this.creationDate = new Date(other.creationDate.getTime());
		this.date = new Date(other.date.getTime());
		this.content = other.content;
		this.path = other.path;
		this.domain = other.domain;
		this.title = other.title;
		this.url = other.url;
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
	public Id crawler_id;
	/**
	 * фактическая дата записи в БД
	 */
	@JsonProperty("creation_date")
	public Date creationDate = new Date(System.currentTimeMillis());
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

	@JsonInclude(value = Include.NON_NULL)
	@JsonProperty("facts")
	public Map<String, List<NewsArticleFact>> facts = new HashedMap<>();

	@Override
	public String toString() {
		return "NewsArticle [_id=" + _id + ", crawler_id=" + crawler_id + ", creationDate="
				+ creationDate + ", date=" + date + ", content=" + content + ", path=" + path
				+ ", domain=" + domain + ", title=" + title + ", url=" + url + ", facts=" + facts
				+ "]";
	}


}
