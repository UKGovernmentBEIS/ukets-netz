package uk.gov.netz.api.workflow.request.flow;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RequestTaskPayloadRfiAttachable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class TestRequestTaskPayload extends RequestTaskPayload implements RequestTaskPayloadRfiAttachable {
    @Builder.Default
    private Map<UUID, String> rfiAttachments = new HashMap<>();
}
