package uk.gov.netz.api.restlogging;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Facilitator class for logging rest api request/response.
 */
@Data
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class RestLoggingEntry {
    @ToString.Include
    private RestLoggingEntryType type;
    @ToString.Include
    private String correlationId;
    private String correlationParentId;
    @Builder.Default
    private final Map<String, String> headers = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> headersCapture = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();
    @Builder.Default
    private Map<String, Object> payloadCapture = new LinkedHashMap<>();
    private String uri;
    private String userId;
    private String httpMethod;
    private int httpStatus;
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private LocalDateTime timestamp = LocalDateTime.now();

    private Long responseTimeInMillis;

    public enum RestLoggingEntryType {
        REQUEST,
        RESPONSE
    }
}
