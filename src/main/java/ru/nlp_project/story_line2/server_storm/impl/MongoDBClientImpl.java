package ru.nlp_project.story_line2.server_storm.impl;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.ne;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;

import ru.nlp_project.story_line2.server_storm.IConfigurationManager;
import ru.nlp_project.story_line2.server_storm.IGroovyInterpreter;
import ru.nlp_project.story_line2.server_storm.IMongoDBClient;
import ru.nlp_project.story_line2.server_storm.model.CrawlerEntry;
import ru.nlp_project.story_line2.server_storm.model.Id;
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
	@Inject
	public IConfigurationManager configurationManager;
	private MongoClient client;
	private Logger log;
	private MongoCollection<DBObject> storylineCollection;
	private MongoCollection<DBObject> crawlerCollection;
	private FindOneAndUpdateOptions afterFindOneAndUpdateOptions;
	private UpdateOptions upsertUpdateOptions;
	private FindOneAndUpdateOptions upsertAfterFindOneAndUpdateOptions;
	private MongoClientOptions options;
	private CodecRegistry codecRegistry;

	@Inject
	public MongoDBClientImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}


	private void createCrawlerIndexes() {
		try {
			MongoCollection<DBObject> collection = getCrawlerCollection();
			List<String> fields = Arrays.asList(FIELD_CRAWLER_ID, CRAWLER_ENTRY_FIELD_PROCESSED,
					CRAWLER_ENTRY_FIELD_ARCHIVED, CRAWLER_ENTRY_FIELD_IN_PROCESS);
			for (String field : fields) {
				// in_process
				BasicDBObject obj = new BasicDBObject();
				obj.put(field, 1);
				// bckg
				IndexOptions ndx = new IndexOptions();
				ndx.background(true);
				collection.createIndex(obj, ndx);
			}
			// _id (background is not valid keyword!!!)
			BasicDBObject obj = new BasicDBObject();
			obj.put(FIELD_ID, 1);
			// bckg
			IndexOptions ndx = new IndexOptions();
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
			// _id (background is not valid keyword!!!)
			obj = new BasicDBObject();
			obj.put(FIELD_ID, 1);
			// bckg
			ndx = new IndexOptions();
			collection.createIndex(obj, ndx);
		} catch (Exception e) {
			log.error("Error while creating index: '{}'", e.getMessage(), e);
		}

	}


	private MongoCollection<DBObject> getCrawlerCollection() throws Exception {
		if (crawlerCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.MONGODB_CRAWLER_DATABASE_NAME)
					.withCodecRegistry(codecRegistry);
			crawlerCollection = database.getCollection(NamesUtil.MONGODB_CRAWLER_COLLECTION_NAME,
					DBObject.class);
		}
		return crawlerCollection;
	}


	@Override
	public Map<String, Object> getNewsArticle(String newsArticleId) throws Exception {
		MongoCollection<DBObject> serverCollections = getStorylineCollection();
		Bson filter = eq(FIELD_ID, new Id(newsArticleId));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return BSONUtils.deserialize(dbObject);
	}

	@Override
	public Map<String, Object> getNextUnprocessedCrawlerEntry() throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		// in_processed != true AND proceed != true
		Bson filter = and(ne(CRAWLER_ENTRY_FIELD_IN_PROCESS, true),
				ne(CRAWLER_ENTRY_FIELD_PROCESSED, true), ne(CRAWLER_ENTRY_FIELD_ARCHIVED, true));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null)
			return BSONUtils.deserialize(item);
		return null;
	}


	private MongoCollection<DBObject> getStorylineCollection() throws Exception {
		if (storylineCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.MONGODB_STORYLINE_DATABASE_NAME)
					.withCodecRegistry(codecRegistry);
			storylineCollection = database
					.getCollection(NamesUtil.MONGODB_STORYLINE_COLLECTION_NAME, DBObject.class);
		}
		return storylineCollection;
	}

	public void initialize() {
		registerCodecs();
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

	private void registerCodecs() {
		Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
		replacements.put(BsonType.OBJECT_ID, Id.class);
		BsonTypeClassMap bsonTypeClassMap = new BsonTypeClassMap(replacements);
		CodecRegistry defaultCodecRegistry = MongoClient.getDefaultCodecRegistry();
		codecRegistry = CodecRegistries.fromRegistries(CodecRegistries.fromCodecs(new IdCodec()),
				defaultCodecRegistry);
		options = MongoClientOptions.builder().codecRegistry(codecRegistry).build();

	}


	@Override
	public void markCrawlerEntryAsProcessedByNewsArticleId(String newsArticleId) throws Exception {
		MongoCollection<DBObject> storylineCollections = getStorylineCollection();
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_ID, new Id(newsArticleId));
		FindIterable<DBObject> news = storylineCollections.find(filter).limit(1);
		DBObject object = news.first();
		// mark crawler news as processed
		Id crawlerId = (Id) object.get("crawler_id");
		filter = eq(FIELD_ID, crawlerId);
		// { "$set" : { "in_process" : "false", "processed" : "true" }}
		Bson setProcessed = BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_PROCESSED
				+ "' : true, '" + CRAWLER_ENTRY_FIELD_IN_PROCESS + "' : false }}");
		crawlerCollections.findOneAndUpdate(filter, setProcessed);
	}

	@Override
	public void shutdown() {
		client.close();
	}

	@Override
	public void unmarkCrawlerEntryAsInProcessByNewsArticleId(String newsArticleId)
			throws Exception {
		Map<String, Object> newsArticle = getNewsArticle(newsArticleId);
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		setObjectField(crawlerCollections, NewsArticle.crawlerIdString(newsArticle),
				CRAWLER_ENTRY_FIELD_IN_PROCESS, false);
	}


	@Override
	public void unmarkCrawlerEntryAsInProcess(String crawlerEntryId) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		setObjectField(crawlerCollections, crawlerEntryId, CRAWLER_ENTRY_FIELD_IN_PROCESS, false);
	}


	@Override
	public void updateCrawlerEntry(Map<String, Object> entry) throws Exception {
		MongoCollection<DBObject> crawlerCollection = getCrawlerCollection();
		Bson filter = eq(FIELD_ID, CrawlerEntry.id(entry));
		BasicDBObject dbObject = BSONUtils.serialize(entry);
		Bson newDocument = new Document("$set", dbObject);
		crawlerCollection.findOneAndUpdate(filter, newDocument);
	}


	@Override
	public void updateNewsArticle(Map<String, Object> newsArticle) throws Exception {
		MongoCollection<DBObject> serverCollection = getStorylineCollection();
		Bson filter = eq(FIELD_ID, NewsArticle.id(newsArticle));
		BasicDBObject dbObject = BSONUtils.serialize(newsArticle);
		Bson newDocument = new Document("$set", dbObject);
		serverCollection.findOneAndUpdate(filter, newDocument);
	}


	@Override
	public String upsertNewsArticleByCrawlerEntry(Map<String, Object> crawlerEntry)
			throws Exception {
		MongoCollection<DBObject> serverCollection = getStorylineCollection();
		// find by source:path
		Bson findSourcePath = and(eq("source", CrawlerEntry.source(crawlerEntry)),
				eq("path", CrawlerEntry.path(crawlerEntry)));
		FindIterable<DBObject> find = serverCollection.find(findSourcePath).limit(1);
		// если существует -- обновить "crawler_id"
		if (find.first() != null) {
			DBObject newsArticleDB = find.first();
			// update crawler_id
			newsArticleDB.put(FIELD_CRAWLER_ID, new Id(CrawlerEntry.idString(crawlerEntry)));
			// update by key
			Bson filter = eq(FIELD_ID, newsArticleDB.get(FIELD_ID));
			Bson newDocument = new Document("$set", newsArticleDB);
			serverCollection.updateOne(filter, newDocument);
			Id oid = (Id) newsArticleDB.get(FIELD_ID);
			return oid.getValue();
		} else {
			// если не существует -- создать новый, скопировать существующие атрибуты и бежать
			Map<String, Object> newsArticle = NewsArticle.newObject(crawlerEntry);
			BasicDBObject dbObject = BSONUtils.serialize(newsArticle);
			MongoCollection<DBObject> collection = getStorylineCollection();
			Bson newDocument = new Document("$set", dbObject);
			DBObject newRecord = collection.findOneAndUpdate(findSourcePath, newDocument,
					upsertAfterFindOneAndUpdateOptions);
			Id oid = (Id) newRecord.get(FIELD_ID);
			return oid.getValue();

		}
	}

	// Выбираются записи (!archived && !in_process &&
	// "дата публикации меньше указанной")
	@Override
	public Map<String, Object> getNextUnarchivedCrawlerEntry(Date date) throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		// in_processed != true AND proceed != true
		Bson filter = and(ne(CRAWLER_ENTRY_FIELD_ARCHIVED, true),
				ne(CRAWLER_ENTRY_FIELD_IN_PROCESS, true),
				lt(IGroovyInterpreter.EXTR_KEY_PUB_DATE, date));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + CRAWLER_ENTRY_FIELD_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null)
			return BSONUtils.deserialize(item);
		return null;
	}



	protected void setObjectField(MongoCollection<DBObject> collection, String objectId,
			String fieldName, boolean value) throws Exception {
		Bson filter = eq(FIELD_ID, new Id(objectId));
		Bson update = BasicDBObject
				.parse("{$set: {'" + fieldName + "' : " + Boolean.toString(value) + " }}");
		collection.findOneAndUpdate(filter, update);
	}


	@Override
	public Map<String, Object> getCrawlerEntry(String id) throws Exception {
		MongoCollection<DBObject> serverCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_ID, new Id(id));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return BSONUtils.deserialize(dbObject);

	}


}
