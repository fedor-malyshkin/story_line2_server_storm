package ru.nlp_project.story_line2.server_storm.impl;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.not;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.CRAWLER_ENTRY_FIELD_NAME_ARCHIVED;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FIELD_NAME_ID;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FIELD_NAME_IN_PROCESS;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FIELD_NAME_PROCESSED;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.FIELD_NAME_SOURCE;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.MAINTENANCE_COMMAND_FIELD_NAME_COMMAND;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID;
import static ru.nlp_project.story_line2.server_storm.utils.NamesUtil.NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author fedor
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
	private MongoCollection<DBObject> maintenanceCollection;

	@Inject
	public MongoDBClientImpl() {
		log = LoggerFactory.getLogger(this.getClass());
	}


	private void createCrawlerIndexes() {
		try {
			MongoCollection<DBObject> collection = getCrawlerCollection();
			List<String> fields = Arrays.asList(NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID,
					FIELD_NAME_PROCESSED, FIELD_NAME_SOURCE,
					CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, FIELD_NAME_IN_PROCESS,
					IGroovyInterpreter.EXTR_KEY_PUB_DATE);
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
			obj.put(FIELD_NAME_ID, 1);
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
			List<String> fields = Arrays.asList(NamesUtil.CRAWLER_ENTRY_FIELD_NAME_PATH,
					FIELD_NAME_SOURCE, FIELD_NAME_PROCESSED,
					NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED, FIELD_NAME_IN_PROCESS);
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
			obj.put(FIELD_NAME_ID, 1);
			// bckg
			IndexOptions ndx = new IndexOptions();
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
		Bson filter = eq(FIELD_NAME_ID, new Id(newsArticleId));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return BSONUtils.deserialize(dbObject);
	}

	@Override
	public Map<String, Object> getNextUnprocessedCrawlerEntry() throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		// in_process != true AND processed != true AND archived != true
		Bson filter = and(ne(FIELD_NAME_IN_PROCESS, true),
				ne(FIELD_NAME_PROCESSED, true), ne(CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, true));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + FIELD_NAME_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null) {
			return BSONUtils.deserialize(item);
		}
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

	private MongoCollection<DBObject> getMaintenanceCollection() throws Exception {
		if (maintenanceCollection == null) {
			MongoDatabase database = client.getDatabase(NamesUtil.MONGODB_MAINTENANCE_DATABASE_NAME)
					.withCodecRegistry(codecRegistry);
			maintenanceCollection = database
					.getCollection(NamesUtil.MONGODB_MAINTENANCE_COLLECTION_NAME, DBObject.class);
		}
		return maintenanceCollection;
	}


	@Override
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
		Bson filter = eq(FIELD_NAME_ID, new Id(newsArticleId));
		FindIterable<DBObject> news = storylineCollections.find(filter).limit(1);
		DBObject object = news.first();
		// mark crawler news as processed
		Id crawlerId = (Id) object.get("crawler_id");
		filter = eq(FIELD_NAME_ID, crawlerId);
		// { "$set" : { "in_process" : "false", "processed" : "true" }}
		Bson setProcessed = BasicDBObject.parse("{$set: {'" + FIELD_NAME_PROCESSED
				+ "' : true, '" + FIELD_NAME_IN_PROCESS + "' : false }}");
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
				FIELD_NAME_IN_PROCESS, false);
	}


	@Override
	public void unmarkCrawlerEntryAsInProcess(String crawlerEntryId) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		setObjectField(crawlerCollections, crawlerEntryId, FIELD_NAME_IN_PROCESS, false);
	}


	@Override
	public void updateCrawlerEntry(Map<String, Object> entry) throws Exception {
		MongoCollection<DBObject> crawlerCollection = getCrawlerCollection();
		Bson filter = eq(FIELD_NAME_ID, CrawlerEntry.id(entry));
		BasicDBObject dbObject = BSONUtils.serialize(entry);
		Bson newDocument = new Document("$set", dbObject);
		crawlerCollection.findOneAndUpdate(filter, newDocument);
	}


	@Override
	public void updateNewsArticle(Map<String, Object> newsArticle) throws Exception {
		MongoCollection<DBObject> serverCollection = getStorylineCollection();
		Bson filter = eq(FIELD_NAME_ID, NewsArticle.id(newsArticle));
		BasicDBObject dbObject = BSONUtils.serialize(newsArticle);
		Bson newDocument = new Document("$set", dbObject);
		serverCollection.findOneAndUpdate(filter, newDocument);
	}


	@Override
	public String upsertNewsArticleByCrawlerEntry(Map<String, Object> crawlerEntry)
			throws Exception {
		MongoCollection<DBObject> serverCollection = getStorylineCollection();
		// find by source:path
		Bson findSourcePath = and(
				eq(FIELD_NAME_SOURCE, CrawlerEntry.source(crawlerEntry)),
				eq(NamesUtil.CRAWLER_ENTRY_FIELD_NAME_PATH, CrawlerEntry.path(crawlerEntry)));
		FindIterable<DBObject> find = serverCollection.find(findSourcePath).limit(1);
		// если существует -- обновить "crawler_id"
		if (find.first() != null) {
			DBObject newsArticleDB = find.first();
			// update crawler_id
			newsArticleDB
					.put(NEWS_ARTICLE_FIELD_NAME_CRAWLER_ID, new Id(CrawlerEntry.idString(crawlerEntry)));
			// update by key
			Bson filter = eq(FIELD_NAME_ID, newsArticleDB.get(FIELD_NAME_ID));
			Bson newDocument = new Document("$set", newsArticleDB);
			serverCollection.updateOne(filter, newDocument);
			Id oid = (Id) newsArticleDB.get(FIELD_NAME_ID);
			return oid.getValue();
		} else {
			// если не существует -- создать новый, скопировать существующие атрибуты и бежать
			Map<String, Object> newsArticle = NewsArticle.newObject(crawlerEntry);
			BasicDBObject dbObject = BSONUtils.serialize(newsArticle);
			MongoCollection<DBObject> collection = getStorylineCollection();
			Bson newDocument = new Document("$set", dbObject);
			DBObject newRecord = collection.findOneAndUpdate(findSourcePath, newDocument,
					upsertAfterFindOneAndUpdateOptions);
			Id oid = (Id) newRecord.get(FIELD_NAME_ID);
			return oid.getValue();

		}
	}

	@Override
	public void markCrawlerEntryAsArchived(String crawlerEntryId) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		setObjectField(crawlerCollections, crawlerEntryId, CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, true);
	}

	@Override
	public Map<String, Object> getNextUnpurgedImagesNewsArticle(Date date) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		// in_processed != true AND proceed != true
		Bson filter = and(ne(NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED, true),
				ne(FIELD_NAME_IN_PROCESS, true),
				lt(IGroovyInterpreter.EXTR_KEY_PUB_DATE, date));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + FIELD_NAME_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null) {
			return BSONUtils.deserialize(item);
		}
		return null;

	}

	// Выбираются записи (!archived && !in_process &&
	// "дата публикации меньше указанной")
	@Override
	public Map<String, Object> getNextUnarchivedCrawlerEntry(Date date) throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		// in_processed != true AND proceed != true
		Bson filter = and(ne(CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, true),
				ne(FIELD_NAME_IN_PROCESS, true),
				lt(IGroovyInterpreter.EXTR_KEY_PUB_DATE, date));
		// { "$set" : { "in_process" : "true"}}
		Bson update =
				BasicDBObject.parse("{$set: {'" + FIELD_NAME_IN_PROCESS + "' : true}}");
		DBObject item = collection.findOneAndUpdate(filter, update, afterFindOneAndUpdateOptions);
		if (item != null) {
			return BSONUtils.deserialize(item);
		}
		return null;
	}


	protected void setObjectField(MongoCollection<DBObject> collection, String objectId,
			String fieldName, boolean value) throws Exception {
		Bson filter = eq(FIELD_NAME_ID, new Id(objectId));
		Bson update = BasicDBObject
				.parse("{$set: {'" + fieldName + "' : " + Boolean.toString(value) + " }}");
		collection.findOneAndUpdate(filter, update);
	}


	@Override
	public Map<String, Object> getCrawlerEntry(String id) throws Exception {
		MongoCollection<DBObject> serverCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_NAME_ID, new Id(id));
		FindIterable<DBObject> iter = serverCollections.find(filter).limit(1);
		DBObject dbObject = iter.first();
		return BSONUtils.deserialize(dbObject);

	}

	@Override
	public void unmarkNewsArticleAsInProcess(String newsArticleId) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		setObjectField(collection, newsArticleId, FIELD_NAME_IN_PROCESS, false);
	}

	@Override
	public void markNewsArticleAsImagesPurged(String newsArticleId) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		setObjectField(collection, newsArticleId, NEWS_ARTICLE_FIELD_NAME_IMAGES_PURGED, true);

	}

	@Override
	public Map<String, Object> getNextMaintenanceCommandEntry() throws Exception {
		MongoCollection<DBObject> collection = getMaintenanceCollection();
		if (collection.count() == 0) {
			return null;
		}

		Bson filter = exists(MAINTENANCE_COMMAND_FIELD_NAME_COMMAND);
		DBObject object = collection.findOneAndDelete(filter);
		return BSONUtils.deserialize(object);
	}

	@Override
	public void deleteNewsArticles(String source) throws Exception {
		MongoCollection<DBObject> storylineCollection = getStorylineCollection();
		Bson filter = eq(FIELD_NAME_SOURCE, source);
		storylineCollection.deleteMany(filter);
	}

	@Override
	public void unmarkCrawlerEntriesAsProcessed(String source) throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		Bson filter = eq(FIELD_NAME_SOURCE, source);
		Bson update = BasicDBObject
				.parse("{$set: {'" + FIELD_NAME_PROCESSED + "' : " + Boolean.toString(false) + " }}");
		crawlerCollections.updateMany(filter, update);
	}


	@Override
	public void insertMaintenanceCommandEntry(Map<String, Object> entry) throws Exception {
		BasicDBObject dbObject = BSONUtils.serialize(entry);
		MongoCollection<DBObject> collection = getMaintenanceCollection();
		collection.insertOne(dbObject);
	}

	@Override
	public void deleteAllNewsArticles() throws Exception {
		MongoCollection<DBObject> storylineCollection = getStorylineCollection();
		storylineCollection.drop();
	}

	@Override
	public void unmarkAllCrawlerEntriesAsProcessed() throws Exception {
		MongoCollection<DBObject> crawlerCollections = getCrawlerCollection();
		Bson update = BasicDBObject
				.parse("{$set: {'" + FIELD_NAME_PROCESSED + "' : " + Boolean.toString(false) + " }}");
		crawlerCollections.updateMany(new BsonDocument(), update);
	}

	@Override
	public List<String> getCrawlerEntrySources() throws Exception {
		List<String> result = new ArrayList<>();
		MongoCollection<DBObject> collection = getCrawlerCollection();
		DistinctIterable<String> sources = collection.distinct(FIELD_NAME_SOURCE, String.class);
		for (String source : sources) {
			result.add(source);
		}
		return result;
	}

	@Override
	public List<String> getNewsArticleSources() throws Exception {
		List<String> result = new ArrayList<>();
		MongoCollection<DBObject> collection = getStorylineCollection();
		DistinctIterable<String> sources = collection.distinct(FIELD_NAME_SOURCE, String.class);
		for (String source : sources) {
			result.add(source);
		}
		return result;
	}

	@Override
	public long getProcessedNewsArticlesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		Bson filter = and(eq(FIELD_NAME_PROCESSED, true), eq(FIELD_NAME_SOURCE, source));
		return collection.count(filter);
	}

	@Override
	public long getUnprocessedNewsArticlesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		Bson filter = and(not(eq(FIELD_NAME_PROCESSED, true)), eq(FIELD_NAME_SOURCE, source));
		return collection.count(filter);
	}

	@Override
	public long getNewsArticlesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getStorylineCollection();
		Bson filter = eq(FIELD_NAME_SOURCE, source);
		return collection.count(filter);
	}

	@Override
	public long getProcessedCrawlerEntriesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		Bson filter = and(eq(FIELD_NAME_PROCESSED, true), eq(FIELD_NAME_SOURCE, source));
		return collection.count(filter);
	}

	@Override
	public long getUnprocessedCrawlerEntriesCount(String source, boolean considerArchived)
			throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		Bson filter = null;
		if (considerArchived) {
			filter = and(ne(FIELD_NAME_PROCESSED, true), eq(FIELD_NAME_SOURCE, source),
					ne(CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, true));
		} else {
			filter = and(ne(FIELD_NAME_PROCESSED, true), eq(FIELD_NAME_SOURCE, source));
		}
		return collection.count(filter);
	}

	@Override
	public long getCrawlerEntriesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		Bson filter = eq(FIELD_NAME_SOURCE, source);
		return collection.count(filter);
	}

	@Override
	public long getArchivedCrawlerEntriesCount(String source) throws Exception {
		MongoCollection<DBObject> collection = getCrawlerCollection();
		Bson filter = and(eq(FIELD_NAME_SOURCE, source),
				ne(CRAWLER_ENTRY_FIELD_NAME_ARCHIVED, true));
		return collection.count(filter);

	}


}
