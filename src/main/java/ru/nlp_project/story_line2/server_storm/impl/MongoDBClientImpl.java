package ru.nlp_project.story_line2.server_storm.impl;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.ne;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.model.CrawlerNewsArticle;
import ru.nlp_project.story_line2.server_storm.model.NewsArticle;
import ru.nlp_project.story_line2.server_storm.utils.BSONUtils;
import ru.nlp_project.story_line2.server_storm.utils.NamesUtil;

/**
 * Клиент mongoDB для сохранения в БД.
 * 
 * 
 * @author fedor
 *
 */
public class MongoDBClientImpl implements IMongoDBClient {
	public static final String FIELD_ID = "_id";
	public static final String CRAWLER_ENTRY_FIELD_PROCESSED = "processed";
	public static final String CRAWLER_ENTRY_FIELD_IN_PROCESS = "in_process";
	@Inject
	public IConfigurationManager configurationManager;
	private MongoClient client;
	private Logger log;
	private MongoCollection<DBObject> storylineCollection;
	private MongoCollection<DBObject> crawlerCollection;
	private FindOneAndUpdateOptions afterFindOneAndUpdateOptions;
	private UpdateOptions upsertUpdateOptions;
	private FindOneAndUpdateOptions upsertAfterFindOneAndUpdateOptions;

	@Inject
	public MongoDBClientImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}


	private MongoCollection<DBObject> getCrawlerCollection() throws Exception {
		if (crawlerCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.MONGODB_CRAWLER_DATABASE_NAME);
			crawlerCollection = database.getCollection(NamesUtil.MONGODB_CRAWLER_COLLECTION_NAME,
					DBObject.class);
		}
		return crawlerCollection;
	}


	@Override
	public NewsArticle getNewsArticle(String objectId) throws Exception {
		MongoCollection<DBObject> serverCollections = getStorylineCollection();
		Bson filter = eq(FIELD_ID, new ObjectId(objectId));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return BSONUtils.deserialize(dbObject, NewsArticle.class);
	}


	@Override
	public CrawlerNewsArticle getNextUnprocessedCrawlerEntry() throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		// in_processed != true AND proceed != true
		Bson filter = and(ne(CRAWLER_ENTRY_FIELD_IN_PROCESS, true),
				ne(CRAWLER_ENTRY_FIELD_PROCESSED, true));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null)
			return BSONUtils.deserialize(item, CrawlerNewsArticle.class);
		return null;
	}

	private MongoCollection<DBObject> getStorylineCollection() throws Exception {
		if (storylineCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.MONGODB_STORYLINE_DATABASE_NAME);
			storylineCollection = database
					.getCollection(NamesUtil.MONGODB_STORYLINE_COLLECTION_NAME, DBObject.class);
		}
		return storylineCollection;
	}



	public void initialize() {
		MongoClientURI mongoClientURI = new MongoClientURI(
				configurationManager.getMasterConfiguration().mongoDBConnectionUrl);
		this.client = new MongoClient(mongoClientURI);
		// update: get updated element
		this.afterFindOneAndUpdateOptions = new FindOneAndUpdateOptions();
		this.afterFindOneAndUpdateOptions.returnDocument(ReturnDocument.AFTER);
		// update: get updated element + upsert
		this.upsertAfterFindOneAndUpdateOptions = new FindOneAndUpdateOptions();
		this.upsertAfterFindOneAndUpdateOptions.returnDocument(ReturnDocument.AFTER);
		this.upsertAfterFindOneAndUpdateOptions.upsert(true);
		// update: upsert
		this.upsertUpdateOptions = new UpdateOptions();
		this.upsertUpdateOptions.upsert(true);

		createCrawlerIndexes();
		createStorylineIndexes();
	}

	private void createCrawlerIndexes() {
		try {
			MongoCollection<DBObject> collection = getCrawlerCollection();
			// in_process

			BasicDBObject obj = new BasicDBObject();
			obj.put(CRAWLER_ENTRY_FIELD_IN_PROCESS, 1);
			// bckg
			IndexOptions ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
			// processed
			obj = new BasicDBObject();
			obj.put(CRAWLER_ENTRY_FIELD_PROCESSED, 1);
			// bckg
			ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
			// _id
			obj = new BasicDBObject();
			obj.put(FIELD_ID, 1);
			// bckg
			ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
		} catch (Exception e) {
			log.error("Error while creating index: '{}'", e.getMessage(), e);
		}

	}

	
	private void createStorylineIndexes() {
		try {
			MongoCollection<DBObject> collection = getStorylineCollection();
			// source
			BasicDBObject obj = new BasicDBObject();
			obj.put("source", 1);
			// bckg
			IndexOptions ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
			// path
			obj = new BasicDBObject();
			obj.put("path", 1);
			// bckg
			ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
	
			// _id
			obj = new BasicDBObject();
			obj.put(FIELD_ID, 1);
			// bckg
			ndx = new IndexOptions();
			ndx.background(true);
			collection.createIndex(obj, ndx);
		} catch (Exception e) {
			log.error("Error while creating index: '{}'", e.getMessage(), e);
		}

	}

	@Override
	public void markNewsArticleAsProcessed(String msgId) throws Exception {
		MongoCollection<DBObject> storylineCollections = getStorylineCollection();
		Bson filter = eq(FIELD_ID, new ObjectId(msgId));
		// { "$set" : { "in_process" : "true"}}
		Bson setProcessed =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_PROCESSED + "' : true}}");
		storylineCollections.updateOne(filter, setProcessed);
	}

	@Override
	public void shutdown() {
		client.close();
	}

	@Override
	public void unmarkCrawlerArticleAsInProcess(String msgId) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_ID, new ObjectId(msgId));
		Bson setProcessed =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_IN_PROCESS + "' : false}}");
		crawlerCollections.findOneAndUpdate(filter, setProcessed);
	}

	@Override
	public void updateNewsArticle(NewsArticle newsArticle) throws Exception {
		MongoCollection<DBObject> serverCollection = getStorylineCollection();
		Bson filter = eq(FIELD_ID, BSONUtils.createBsonObjectId(newsArticle._id));
		BasicDBObject dbObject = BSONUtils.serialize(newsArticle);
		Bson newDocument = new Document("$set", dbObject);
		serverCollection.findOneAndUpdate(filter, newDocument);
	}

	@Override
	public String writeNewNewsArticle(CrawlerNewsArticle crawlerNewsArticle) throws Exception {
		NewsArticle newsArticle = new NewsArticle(crawlerNewsArticle);
		BasicDBObject dbObject;
		dbObject = BSONUtils.serialize(newsArticle);


		MongoCollection<DBObject> collection = getStorylineCollection();
		Bson filter = and(eq("source", newsArticle.source), eq("path", newsArticle.path));
		Bson newDocument = new Document("$set", dbObject);
		DBObject newRecord = collection.findOneAndUpdate(filter, newDocument,
				upsertAfterFindOneAndUpdateOptions);
		ObjectId oid = (ObjectId) newRecord.get(FIELD_ID);

		return oid.toHexString();
	}


	@Override
	public void markCrawlerEntryAsProcessed(String msgId) throws Exception {
		MongoCollection<DBObject> storylineCollections = getStorylineCollection();
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_ID, new ObjectId(msgId));
		FindIterable<DBObject> news = storylineCollections.find(filter).limit(1);
		DBObject object = news.first();
		// mark crawler news as processed
		ObjectId crawlerId = (ObjectId) object.get("crawler_id");
		filter = eq(FIELD_ID, crawlerId);
		// { "$set" : { "in_process" : "true"}}
		Bson setProcessed =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_PROCESSED + "' : true}}");
		crawlerCollections.findOneAndUpdate(filter, setProcessed);
	}



}
