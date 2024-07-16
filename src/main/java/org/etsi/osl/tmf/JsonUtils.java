package org.etsi.osl.tmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @author ctranoris
 *
 */
public class JsonUtils {

	public static byte[] toJson(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper.writeValueAsBytes(object);
	}

	public static String toJsonString(Object object) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper.writeValueAsString(object);
	}

	public static <T> T toJsonObj(String content, Class<T> valueType) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper.readValue(content, valueType);
	}

	public static <T> T toJsonObj(InputStream content, Class<T> valueType) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		return mapper.readValue(content, valueType);
	}

	public static <T> List<T> toListOfJsonObj(String content, Class<T> valueType) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		List<T> listOfJsonObj = mapper.readValue(
				content,
				mapper.getTypeFactory().constructCollectionType(
						List.class, valueType));
		return listOfJsonObj;
	}

}
