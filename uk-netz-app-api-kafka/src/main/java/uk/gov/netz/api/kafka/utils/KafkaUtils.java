package uk.gov.netz.api.kafka.utils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.common.header.Headers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.experimental.UtilityClass;

@UtilityClass
public class KafkaUtils {
	
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			;

	public final String resolveHeader(String key, Headers headers) {
		return Optional.ofNullable(headers.lastHeader(key))
				.map(header -> new String(header.value(), StandardCharsets.UTF_8)).orElse(null);
	}
	
	public <V> Map<String, Object> resolveRecordValueAsMap(V recordValue) {
		Object value;
		
		if (recordValue instanceof byte[]) {
			String payload = new String((byte[]) recordValue);
			try {
				value = getObjectMapper().readValue(payload, Map.class);
			} catch (Exception e) {
				value = payload;
			}
		} else {
			try {
				value = getObjectMapper().convertValue(recordValue, new TypeReference<>() {
				});
			} catch (Exception e) {
				value = recordValue.toString();
			}
		}
		
		final Map<String, Object> recordValueAsMap = new HashMap<>();
		recordValueAsMap.put("payload", value);
		return recordValueAsMap;
	}
	
	public final ObjectMapper getObjectMapper() {
        return objectMapper;
    }
	
}
