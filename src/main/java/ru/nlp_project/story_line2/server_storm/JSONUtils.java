package ru.nlp_project.story_line2.server_storm;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtils {


	private static ObjectMapper mapper;



	public static ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper(new JsonFactory());
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		}
		return mapper;
	}



	public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
		ObjectMapper mapper = getObjectMapper();
		return mapper.readValue(json, clazz);
	}


	public static String serialize(Object instance) throws IOException {
		ObjectMapper mapper = getObjectMapper();
		return mapper.writeValueAsString(instance);
	}
}
