package uk.gov.netz.docgenerator.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConversionEvent {

    private String jobId;
    private String status;
    private String outputS3Key;
    private String errorReason;
    private long durationMs;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> metadata = new LinkedHashMap<>();

    public ConversionEvent(String jobId, String status, String outputS3Key, String errorReason, long durationMs) {
        this(jobId, status, outputS3Key, errorReason, durationMs, null);
    }

    @Builder
    public ConversionEvent(
        String jobId,
        String status,
        String outputS3Key,
        String errorReason,
        long durationMs,
        Map<String, String> metadata
    ) {
        this.jobId = jobId;
        this.status = status;
        this.outputS3Key = outputS3Key;
        this.errorReason = errorReason;
        this.durationMs = durationMs;
        setMetadata(metadata);
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }
}
