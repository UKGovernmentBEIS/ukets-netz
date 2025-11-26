package uk.gov.netz.api.workflow.request.core.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import org.springframework.util.CollectionUtils;

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
public abstract class RequestTaskPayload {

    private String payloadType;

    @Builder.Default
    private boolean sendEmailNotification = true;

    @JsonIgnore
    public Map<UUID, String> getAttachments() {
        return Collections.emptyMap();
    }

    @JsonIgnore
    public Set<UUID> getReferencedAttachmentIds() {
        return Collections.emptySet();
    }

    @JsonIgnore
    public void removeAttachments(final Collection<UUID> uuids) {

        if (CollectionUtils.isEmpty(uuids)) {
            return;
        }
        this.getAttachments().keySet().removeIf(uuids::contains);
    }
}
