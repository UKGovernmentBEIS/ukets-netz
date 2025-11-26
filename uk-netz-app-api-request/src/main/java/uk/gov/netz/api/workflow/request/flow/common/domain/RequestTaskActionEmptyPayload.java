package uk.gov.netz.api.workflow.request.flow.common.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class RequestTaskActionEmptyPayload extends RequestTaskActionPayload {

}
