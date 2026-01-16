package uk.gov.netz.api.restlogging;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestLoggingUtils {
	
	public final String CORRELATION_ID_HEADER = "Correlation-Id";
	public final String CORRELATION_PARENT_ID_HEADER = "Correlation-Parent-Id";

	public boolean isUriContainedInList(String uri, List<String> uris) {
        List<Pattern> uriPatterns = uris.stream()
                .map(Pattern::compile)
                .toList();

        for (Pattern pattern : uriPatterns) {
            if (pattern.matcher(uri).find()) {
                return true;
            }
        }
        return false;
    }
	
	public Map<String, Object> getPayloadAsMap(byte[] buffer, ObjectMapper objectMapper) {
        if (buffer != null && buffer.length > 0) {
            try {
                return objectMapper.readValue(buffer, new TypeReference<>() {
                });
            } catch (IOException ex) {
                return Map.of();
            }
        }
        return Map.of();
    }
}
