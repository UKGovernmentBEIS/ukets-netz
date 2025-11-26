package uk.gov.netz.api.workflow.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class TestRequestTaskActionPayload extends RequestTaskActionPayload {
}
