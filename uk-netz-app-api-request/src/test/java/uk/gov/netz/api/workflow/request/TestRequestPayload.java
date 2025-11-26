package uk.gov.netz.api.workflow.request;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeData;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RequestPayloadRfiable;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiData;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class TestRequestPayload extends RequestPayload implements RequestPayloadRdeable, RequestPayloadRfiable {
    @JsonUnwrapped
    private RdeData rdeData;

    @JsonUnwrapped
    private RfiData rfiData;
}
