package ru.nlp_project.story_line2.server_storm.utils;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
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
				gen.writeString(id.value);
			}
		}
	}


	public static class JsonDateDeserializer extends JsonDeserializer<Date> {

		private DateTimeFormatter formatter;
		private ZoneId defaultZoneId;

		public JsonDateDeserializer() {
			super();
			defaultZoneId = ZoneId.systemDefault();
			formatter = DateTimeFormatter.ISO_INSTANT.withZone(defaultZoneId);

		}

		@Override
		public Date deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			if (p.getCurrentToken() == JsonToken.VALUE_NULL)
				return null;
			if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
				// 1970-01-01T00:00:01Z
				ZonedDateTime zdt = ZonedDateTime.parse(p.getValueAsString(), formatter);
				return Date.from(zdt.toInstant());
			} else
				throw new IllegalStateException();
		}
	}

	public static class JsonDateSerializer extends JsonSerializer<Date> {

		private DateTimeFormatter formatter;

		public JsonDateSerializer() {
			formatter = DateTimeFormatter.ISO_INSTANT;
		}

		@Override
		public void serialize(final Date value, final JsonGenerator gen,
				final SerializerProvider provider) throws IOException {
			if (value == null)
				gen.writeNull();
			else {
				Instant instant = value.toInstant();
				ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
				gen.writeString(formatter.format(zonedDateTime));
			}
		}
	}



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

			final SimpleModule module = new SimpleModule("", Version.unknownVersion());
			module.addSerializer(Date.class, new JsonDateSerializer());
			module.addDeserializer(Date.class, new JsonDateDeserializer());
			module.addSerializer(Id.class, new JsonIdSerializer());
			module.addDeserializer(Id.class, new JsonIdDeserializer());

			jsonMapper.registerModule(module);


		}
		return jsonMapper;
	}

	public static <T> T deserialize(String json, Class<T> clazz) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	public static String serialize(Object instance) {
		ObjectMapper mapper = getJSONObjectMapper();
		try {
			return mapper.writeValueAsString(instance);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException(e);
		}
	}


}
