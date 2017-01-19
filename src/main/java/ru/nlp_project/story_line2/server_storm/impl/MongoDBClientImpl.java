package ru.nlp_project.story_line2.server_storm.impl;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.or;

import java.util.Date;

import javax.inject.Inject;

import org.bson.BsonDateTime;
import org.bson.Document;
import org.bson.conversions.Bson;
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
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;

import de.undercouch.bson4jackson.types.ObjectId;
import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.datamodel.CrawlerNewsArticle;
import ru.nlp_project.story_line2.server_storm.datamodel.NewsArticle;
import ru.nlp_project.story_line2.server_storm.util.JSONUtils;
import ru.nlp_project.story_line2.server_storm.util.NamesUtil;

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
	private Logger logger;
	private MongoCollection<DBObject> serverStormCollection;
	private MongoCollection<DBObject> crawlerCollection;
	private FindOneAndUpdateOptions afterFindOneAndUpdateOptions;
	private UpdateOptions upsertUpdateOptions;
	private FindOneAndUpdateOptions upsertAfterFindOneAndUpdateOptions;

	@Inject
	public MongoDBClientImpl() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	protected org.bson.types.ObjectId createBsonObjectId(ObjectId objectId) {
		if (objectId == null)
			throw new IllegalArgumentException("objectId must be nut null");
		// TODO: rewrite serializer/deserializer for modern format
		return org.bson.types.ObjectId.createFromLegacyFormat(objectId.getTime(),
				objectId.getMachine(), objectId.getInc());
	}

	@Override
	public ObjectId createObjectId(String hexString) {
		if (!org.bson.types.ObjectId.isValid(hexString))
			throw new IllegalArgumentException(
					"hexString is not valid - may be its ti,e to revrite for moder ObjectIdFormat");
		org.bson.types.ObjectId objectIdInt = new org.bson.types.ObjectId(hexString);
		return new ObjectId(objectIdInt.getTimestamp(), objectIdInt.getMachineIdentifier(),
				objectIdInt.getCounter());
	}


	private MongoCollection<DBObject> getCrawlerNewsCollection() throws Exception {
		if (crawlerCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.CRAWLER_MONGODB_DATABASE_NAME);
			crawlerCollection = database
					.getCollection(NamesUtil.CRAWLER_MONGODB_NEWS_COLLECTION_NAME, DBObject.class);
		}
		return crawlerCollection;
	}

	@Override
	public String getHexString(ObjectId objectId) {
		if (objectId == null)
			throw new IllegalArgumentException("objectId must be nut null");
		// TODO: rewrite serializer/deserializer for modern format
		org.bson.types.ObjectId objectIdInt = org.bson.types.ObjectId.createFromLegacyFormat(
				objectId.getTime(), objectId.getMachine(), objectId.getInc());
		return objectIdInt.toHexString();
	}


	@Override
	public NewsArticle getNewsArticle(String objectId) throws Exception {
		MongoCollection<DBObject> serverCollections = getServerStormNewsArticleCollection();
		Bson filter = eq("_id", new org.bson.types.ObjectId(objectId));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return JSONUtils.bsonDeserialize(dbObject, NewsArticle.class);
	}


	@Override
	public CrawlerNewsArticle getNextUnprocessedCrawlerArticle(Date lastEmittedDate)
			throws Exception {
		MongoCollection<DBObject> collection = getCrawlerNewsCollection();
		if (null == lastEmittedDate)
			lastEmittedDate = new Date(1);
		Bson filter = and(gt("date", new BsonDateTime(lastEmittedDate.getTime())),
				or(ne("in_process", true), ne("processed", true)));
		// { "$set" : { "in_process" : "true"}}
		Bson update = BasicDBObject.parse("{$set: {'in_process' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null)
			return JSONUtils.bsonDeserialize(item, CrawlerNewsArticle.class);
		return null;
	}

	private MongoCollection<DBObject> getServerStormNewsArticleCollection() throws Exception {
		if (serverStormCollection == null) {
			MongoDatabase database =
					client.getDatabase(NamesUtil.SERVER_STORM_MONGODB_DATABASE_NAME);
			serverStormCollection = database.getCollection(
					NamesUtil.SERVER_STORM_MONGODB_NEWS_ARTICLE_COLLECTION_NAME, DBObject.class);
		}
		return serverStormCollection;
	}



	public void initialize() {
		MongoClientURI mongoClientURI =
				new MongoClientURI(configurationManager.getMasterConfiguration().mongoDBConnectionUrl);
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
	}

	@Override
	public void markNewsArticleAsProcessed(String msgId) throws Exception {
		MongoCollection<DBObject> serverCollections = getServerStormNewsArticleCollection();
		MongoCollection<DBObject> crawlerCollections = getCrawlerNewsCollection();
		Bson filter = eq("_id", new org.bson.types.ObjectId(msgId));
		// { "$set" : { "in_process" : "true"}}
		Bson setProcessed = BasicDBObject.parse("{$set: {'processed' : true}}");
		DBObject news = serverCollections.findOneAndUpdate(filter, setProcessed,
				afterFindOneAndUpdateOptions);
		// mark crawler news as processed
		org.bson.types.ObjectId crawlerId = (org.bson.types.ObjectId) news.get("crawler_id");
		filter = eq("_id", crawlerId);
		crawlerCollections.findOneAndUpdate(filter, setProcessed);
	}

	@Override
	public void shutdown() {
		client.close();
	}

	@Override
	public void unmarkCrawlerArticleAsInProcess(String msgId) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerNewsCollection();
		Bson filter = eq("_id", new org.bson.types.ObjectId(msgId));
		// { "$set" : { "in_process" : "true"}}
		Bson setProcessed = BasicDBObject.parse("{$set: {'in_process' : false}}");
		crawlerCollections.findOneAndUpdate(filter, setProcessed);
	}

	@Override
	public void updateNewsArticle(NewsArticle newsArticle) throws Exception {
		MongoCollection<DBObject> serverCollection = getServerStormNewsArticleCollection();
		Bson filter = eq("_id", createBsonObjectId(newsArticle._id));
		BasicDBObject dbObject = JSONUtils.bsonSerialize(newsArticle);
		Bson newDocument = new Document("$set", dbObject);
		serverCollection.findOneAndUpdate(filter, newDocument);
	}

	@Override
	public String writeNewNewsArticle(CrawlerNewsArticle crawlerNewsArticle) throws Exception {
		NewsArticle newsArticle = new NewsArticle(crawlerNewsArticle);
		BasicDBObject dbObject;
		dbObject = JSONUtils.bsonSerialize(newsArticle);
		MongoCollection<DBObject> collection = getServerStormNewsArticleCollection();
		Bson filter = and(eq("domain", newsArticle.domain), eq("path", newsArticle.path));
		Bson newDocument = new Document("$set", dbObject);
		DBObject findOneAndUpdate = collection.findOneAndUpdate(filter, newDocument,
				upsertAfterFindOneAndUpdateOptions);
		org.bson.types.ObjectId oid = (org.bson.types.ObjectId) findOneAndUpdate.get("_id");
		return oid.toHexString();
	}

}
