package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.workflow.request.flow.common.domain.RequestCreateActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

public abstract class RequestCreateAccountRelatedNoPayloadValidator extends RequestCreateAccountRelatedValidator<RequestCreateActionEmptyPayload> {

    public RequestCreateAccountRelatedNoPayloadValidator(RequestCreateValidatorService requestCreateValidatorService) {
        super(requestCreateValidatorService);
    }

    @Override
    public RequestCreateValidationResult validateCreation(Long accountId, RequestCreateActionEmptyPayload payload) {
        return this.checkAvailability(accountId);
    }
}
