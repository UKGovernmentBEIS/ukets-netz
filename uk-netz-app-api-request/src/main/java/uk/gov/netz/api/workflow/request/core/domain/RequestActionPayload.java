package uk.gov.netz.api.workflow.request.core.domain;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "payloadType", visible = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class RequestActionPayload {

    private String payloadType;

    @JsonIgnore
    public Map<UUID, String> getAttachments() {
        return Collections.emptyMap();
    }

    @JsonIgnore
    public Map<UUID, String> getFileDocuments() {
        return Collections.emptyMap();
    }

}
