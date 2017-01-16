package ru.nlp_project.story_line2.server_storm;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import javax.inject.Inject;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * Клиент mongoDB для сохранения в БД.
 * 
 * 
 * @author fedor
 *
 */
public class MongoDBClientImpl implements IMongoDBClient {
	@Inject
	public IConfigurationManager configurationManager;
	private MongoClient client;
	private MongoCollection<Document> collection;
	private Logger logger;

	@Inject
	public MongoDBClientImpl() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	public void initialize() {
		MongoClientURI mongoClientURI =
				new MongoClientURI(configurationManager.getMasterConfiguration().connectionUrl);
		this.client = new MongoClient(mongoClientURI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.crawler.IMongoDBClient#shutdown()
	 */
	@Override
	public void shutdown() {
		client.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.crawler.IMongoDBClient#writeNews(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void writeNews(String json, String domain, String path) {
		collection = getNewsCollections();
		FindIterable<Document> find = collection.find(and(eq("domain", domain), eq("path", path)));
		if (find.first() != null) {
			String msg =
					String.format("Record for (%s:%s) already exists in MongoDB.", domain, path);
			logger.debug(msg);
			// don nothing
			return;
		}

		Document document = Document.parse(json);
		try {
			collection.insertOne(document);
			if (logger.isTraceEnabled()) {
				String msg = String.format("Write record to MongoDB for (%s:%s) - '%s'.", domain,
						path, json);
				logger.trace(msg);
			} else {
				String msg =
						String.format("Write record to MongoDB for (%s:%s).", domain, path, json);
				logger.info(msg);
			}
		} catch (com.mongodb.MongoException e) {
			String msg = String.format("Exception while write record to MongoDB for (%s:%s): %s.",
					domain, path, e.getMessage());
			logger.error(msg);
		}
	}

	private MongoCollection<Document> getNewsCollections() {
		if (collection == null) {
			MongoDatabase database = client.getDatabase("crawler");
			collection = database.getCollection("news");
		}
		return collection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ru.nlp_project.story_line2.crawler.IMongoDBClient#dumpsAllNewsToFiles(ru.nlp_project.
	 * story_line2.crawler.MongoDBClientManager.IDumpRecordProcessor)
	 */
	@Override
	public void dumpsAllNewsToFiles(IRecordIterationProcessor reader) {
		collection = getNewsCollections();
		FindIterable<Document> find = collection.find();
		MongoCursor<Document> iterator = find.iterator();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			String domain = doc.getString("domain");
			String path = doc.getString("path");
			String content = doc.getString("content");
			reader.processRecord(domain, path, content);
		}


	}

}
