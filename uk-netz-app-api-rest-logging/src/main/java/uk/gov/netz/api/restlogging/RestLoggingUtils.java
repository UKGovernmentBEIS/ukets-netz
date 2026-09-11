package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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

    public boolean isJsonContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)
                    || mediaType.getSubtype().toLowerCase(Locale.ROOT).endsWith("+json");
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
    }

    public boolean isMultipart(String contentType) {
        if (contentType == null) {
            return false;
        }
        try {
            return MediaType.MULTIPART_FORM_DATA.isCompatibleWith(MediaType.parseMediaType(contentType));
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
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
