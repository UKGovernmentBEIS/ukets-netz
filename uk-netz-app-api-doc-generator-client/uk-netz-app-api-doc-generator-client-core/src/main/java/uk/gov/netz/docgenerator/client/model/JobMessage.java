package uk.gov.netz.docgenerator.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobMessage {

    private String jobId;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean normalize;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> metadata = new LinkedHashMap<>();

    public JobMessage(String jobId) {
        this(jobId, null);
    }

    public JobMessage(String jobId, boolean normalize) {
        this(jobId, null, normalize);
    }

    public JobMessage(String jobId, Map<String, String> metadata) {
        this(jobId, metadata, false);
    }

    public JobMessage(String jobId, Map<String, String> metadata, boolean normalize) {
        this.jobId = jobId;
        this.normalize = normalize;
        setMetadata(metadata);
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }
}
