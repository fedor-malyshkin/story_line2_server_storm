package ru.nlp_project.story_line2.server_storm.utils;

import java.util.Map;

import org.bson.BSON;
import org.bson.BSONObject;
import org.bson.Transformer;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import ru.nlp_project.story_line2.server_storm.model.Id;

/**
 * Утилита для серипизации/десериализации данных BSON формат.
 * 
 * WARN: Поля, являющиеся идентификаторами (или ссылками на идентификаторы) необходимо делать типа
 * {@link ru.nlp_project.story_line2.server_storm.model.Id}.
 * 
 * @author fedor
 *
 */
public class BSONUtils {
	private static Transformer objectIdTransformer;
	private static Transformer idTransformer;

	static {

		idTransformer = new Transformer() {
			@Override
			public Object transform(Object objectToTransform) {
				Id id = (Id) objectToTransform;
				return new ObjectId(id.getValue());
			}

		};

		objectIdTransformer = new Transformer() {
			@Override
			public Object transform(Object objectToTransform) {
				ObjectId id = (ObjectId) objectToTransform;
				return new Id(id.toHexString());
			}

		};

		BSON.addEncodingHook(Id.class, idTransformer);
		BSON.addDecodingHook(ObjectId.class, objectIdTransformer);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> deserialize(DBObject item) {
		// in code+decode comple donig conversion
		BSONObject decode = BSON.decode(BSON.encode(item));
		return decode.toMap();
	}

	public static BasicDBObject serialize(Map<String, Object> instance) {
		return new BasicDBObject(instance);
	}

	public static ObjectId createBsonObjectId(Id id) {
		if (id == null)
			throw new IllegalArgumentException("objectId must not be null");
		return new ObjectId(id.getValue());
	}

}
