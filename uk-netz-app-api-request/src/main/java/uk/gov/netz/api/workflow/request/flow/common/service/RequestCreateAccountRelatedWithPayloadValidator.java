package uk.gov.netz.api.workflow.request.flow.common.service;

import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;

public abstract class RequestCreateAccountRelatedWithPayloadValidator<T extends RequestCreateActionPayload>  extends RequestCreateAccountRelatedValidator<T> {

    public RequestCreateAccountRelatedWithPayloadValidator(RequestCreateValidatorService requestCreateValidatorService) {
        super(requestCreateValidatorService);
    }
}
