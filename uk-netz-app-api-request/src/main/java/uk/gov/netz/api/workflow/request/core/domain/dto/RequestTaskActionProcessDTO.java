package uk.gov.netz.api.workflow.request.core.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;

/**
 * The RequestTaskActionDTO for triggering request task types.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestTaskActionProcessDTO {

    /** The {@link RequestActionTypes}. */
    @NotNull(message = "{requestTaskActionProcess.requestTaskActionType.notEmpty}")
    private String requestTaskActionType;

    /** The {@link RequestActionPayload}. */
    @NotNull(message = "{requestTaskActionProcess.requestTaskActionPayload.notEmpty}")
    @Valid
    private RequestTaskActionPayload requestTaskActionPayload;

    /** The request task id. */
    @NotNull(message = "{requestTaskActionProcess.requestTaskId.notEmpty}")
    private Long requestTaskId;
}
