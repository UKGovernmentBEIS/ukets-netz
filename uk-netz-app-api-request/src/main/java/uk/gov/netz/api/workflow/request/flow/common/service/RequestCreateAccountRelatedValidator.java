package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

@RequiredArgsConstructor
public abstract class RequestCreateAccountRelatedValidator<T extends RequestCreateActionPayload> implements RequestCreateByAccountValidator<T> {

    private final RequestCreateValidatorService requestCreateValidatorService;

    @Override
    public RequestCreateValidationResult checkAvailability(final Long accountId) {
        return requestCreateValidatorService
                .validate(accountId, this.getApplicableAccountStatuses(), this.getMutuallyExclusiveRequests());
    }
}
