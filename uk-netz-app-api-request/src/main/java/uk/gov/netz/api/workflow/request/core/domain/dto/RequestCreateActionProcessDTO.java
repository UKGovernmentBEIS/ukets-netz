package uk.gov.netz.api.workflow.request.core.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;

/**
 * The RequestActionRegistrationDTO for creating a new request.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateActionProcessDTO {

    @NotNull
    private String requestType;

    @NotNull
    @Valid
    private RequestCreateActionPayload requestCreateActionPayload;

}
