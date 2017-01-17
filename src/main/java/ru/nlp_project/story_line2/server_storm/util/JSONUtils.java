package ru.nlp_project.story_line2.server_storm.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.bson.BSON;
import org.bson.BSONObject;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import de.undercouch.bson4jackson.BsonConstants;
import de.undercouch.bson4jackson.BsonFactory;
import de.undercouch.bson4jackson.BsonGenerator;
import de.undercouch.bson4jackson.BsonParser;
import de.undercouch.bson4jackson.types.ObjectId;

public class JSONUtils {
	public static class BsonDateDeserializer extends JsonDeserializer<Date> {

		@Override
		public Date deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			if (p instanceof BsonParser) {
				BsonParser bsonParser = (BsonParser) p;
				if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT
						|| bsonParser.getCurrentBsonType() != BsonConstants.TYPE_DATETIME) {
					throw ctxt.mappingException(Date.class.getCanonicalName());
				}
				return (Date) bsonParser.getEmbeddedObject();
			} else {
				return new Date(p.getLongValue());
			}
		}

	}


	public static class BsonDateSerializer extends JsonSerializer<Date> {

		@Override
		public void serialize(final Date value, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException {

			if (gen instanceof BsonGenerator) {
				BsonGenerator bgen = (BsonGenerator) gen;
				if (value == null)
					bgen.writeNull();
				else
					bgen.writeDateTime(value);
			} else {
				gen.writeNumber(value.getTime());
			}
		}
	}


	public static class BsonObjectIdDeserializer extends JsonDeserializer<ObjectId> {

		@Override
		public ObjectId deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			if (p instanceof BsonParser) {
				BsonParser bsonParser = (BsonParser) p;
				if (bsonParser.getCurrentToken() != JsonToken.VALUE_EMBEDDED_OBJECT
						|| bsonParser.getCurrentBsonType() != BsonConstants.TYPE_OBJECTID) {
					throw ctxt.mappingException(ObjectId.class.getCanonicalName());
				}
				return (ObjectId) bsonParser.getEmbeddedObject();
			} else {
				TreeNode tree = p.getCodec().readTree(p);
				int time = ((ValueNode) tree.get("$time")).asInt();
				int machine = ((ValueNode) tree.get("$machine")).asInt();
				int inc = ((ValueNode) tree.get("$inc")).asInt();
				return new ObjectId(time, machine, inc);
			}
		}

	}


	public static class BsonObjectIdSerializer extends JsonSerializer<ObjectId> {

		@Override
		public void serialize(final ObjectId value, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException {

			if (gen instanceof BsonGenerator) {
				BsonGenerator bgen = (BsonGenerator) gen;
				if (value == null)
					bgen.writeNull();
				else
					bgen.writeObjectId(value);
			} else {
				gen.writeNumber(value.getTime());
			}
		}
	}


	private static ObjectMapper jsonMapper;
	private static ObjectMapper bsonMapper;


	public static <T> T bsonDeserialize(DBObject item, Class<T> class1) {
		ObjectMapper objectMapper = getBSONObjectMapper();
		byte[] encode = BSON.encode(item);
		try {
			return objectMapper.readValue(encode, class1);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static BasicDBObject bsonSerialize(Object instance) {
		ObjectMapper mapper = getBSONObjectMapper();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			mapper.writeValue(baos, instance);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		BSONObject decode = BSON.decode(baos.toByteArray());
		return new BasicDBObject(decode.toMap());
	}



	/**
	 * See: http://stackoverflow.com/questions/12463049/date-format-mapping-to-json-jackson See:
	 * https://github.com/michel-kraemer/bson4jackson/tree/master/src/main/java/de/undercouch/bson4jackson
	 * 
	 * @return
	 */
	public static ObjectMapper getBSONObjectMapper() {
		if (bsonMapper == null) {
			BsonFactory bsonFactory = new BsonFactory();
			bsonFactory.enable(BsonParser.Feature.HONOR_DOCUMENT_LENGTH);
			bsonMapper = new ObjectMapper(bsonFactory);
			final SimpleModule module = new SimpleModule("", Version.unknownVersion());
			module.addSerializer(Date.class, new BsonDateSerializer());
			module.addDeserializer(Date.class, new BsonDateDeserializer());
			module.addSerializer(ObjectId.class, new BsonObjectIdSerializer());
			module.addDeserializer(ObjectId.class, new BsonObjectIdDeserializer());
			bsonMapper.registerModule(module);
		}
		return bsonMapper;
	}


	public static ObjectMapper getJSONObjectMapper() {
		if (jsonMapper == null) {
			jsonMapper = new ObjectMapper(new JsonFactory());
			jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		}
		return jsonMapper;
	}

	public static <T> T jsonDeserialize(String json, Class<T> clazz) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	public static String jsonSerialize(Object instance) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.writeValueAsString(instance);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

}
