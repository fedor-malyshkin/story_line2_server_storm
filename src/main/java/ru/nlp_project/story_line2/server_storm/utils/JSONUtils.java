package ru.nlp_project.story_line2.server_storm.utils;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer.Vanilla;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ru.nlp_project.story_line2.server_storm.model.Id;

/**
 * Утилита для серипизации/десериализации данных в JSON формат.
 * 
 * WARN: Поля, являющиеся идентификаторами (или ссылками на идентификаторы) необходимо делать типа
 * {@link ru.nlp_project.story_line2.server_storm.model.Id}.
 * 
 * @author fedor
 *
 */
public class JSONUtils {
	private static ObjectMapper jsonMapper;

	/**
	 * Настройка mapper'а на другой формат хранения данных для даты -- UTC ()
	 * 
	 * <ul>
	 * <li>See: http://stackoverflow.com/questions/12463049/date-format-mapping-to-json-jackson</li>
	 * </ul>
	 * 
	 * @return
	 */
	public static ObjectMapper getJSONObjectMapper() {
		if (jsonMapper == null) {
			jsonMapper = new ObjectMapper(new JsonFactory());
			jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			jsonMapper.setSerializationInclusion(Include.NON_NULL);

			SimpleModule module = new SimpleModule();
			module.addDeserializer(Object.class, new ObjectDeserializer());
			module.addSerializer(Date.class, new JsonDateSerializer());
			module.addSerializer(Id.class, new JsonIdSerializer());
			jsonMapper.registerModule(module);
		}
		return jsonMapper;
	}


	public static class JsonIdDeserializer extends JsonDeserializer<Id> {

		@Override
		public Id deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			if (p.getCurrentToken() == JsonToken.VALUE_NULL)
				return null;
			if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
				return new Id(p.getValueAsString());
			} else
				throw new IllegalStateException();
		}

	}


	public static class JsonIdSerializer extends JsonSerializer<Id> {
		@Override
		public void serialize(final Id id, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException {
			if (id == null)
				gen.writeNull();
			else {
				gen.writeString(id.getValue());
			}
		}
	}



	public static class JsonDateSerializer extends JsonSerializer<Date> {


		public JsonDateSerializer() {}

		@Override
		public void serialize(final Date value, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException {
			if (value == null)
				gen.writeNull();
			else {
				gen.writeString(DateTimeUtils.serializeDate(value));
			}
		}
	}

	public static class ObjectDeserializer extends JsonDeserializer<Object> {


		private static JsonDeserializer<Object> vanillaSeserializer;

		public ObjectDeserializer() {
			vanillaSeserializer = new Vanilla();

		}

		@Override
		public Object deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			if (p.getCurrentToken() == JsonToken.VALUE_NULL)
				return null;
			if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
				String value = p.getValueAsString();
				// 1970-01-01T00:00:01Z or 1970-01-01T00:00:00.001Z
				if ((value.length() == 20 || value.length() == 24) && value.endsWith("Z")) {
					try {
						return DateTimeUtils.deserializeDate(value);
					} catch (DateTimeParseException e) {
					}
				}
				if (value.length() == 24 && ObjectId.isValid(value)) {
					return new Id(value);
				}
				return vanillaSeserializer.deserialize(p, ctxt);
			} else
				return vanillaSeserializer.deserialize(p, ctxt);
		}

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> deserialize(String json) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.readValue(json, HashMap.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	public static String serialize(Map<String, Object> instance) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.writeValueAsString(instance);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> deserializeList(String json) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.readValue(json, ArrayList.class);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String serializeList(List<Map<String, Object>> instance) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.writeValueAsString(instance);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}

}
