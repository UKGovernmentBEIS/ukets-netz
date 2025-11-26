package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.Set;

@RequiredArgsConstructor
public abstract class RequestCreateAccountRelatedValidator implements RequestCreateByAccountValidator {

    private final RequestCreateValidatorService requestCreateValidatorService;

    @Override
    public RequestCreateValidationResult validateAction(final Long accountId) {
        return requestCreateValidatorService
                .validate(accountId, this.getApplicableAccountStatuses(), this.getMutuallyExclusiveRequests());
    }

    protected abstract Set<AccountStatus> getApplicableAccountStatuses();

    protected abstract Set<String> getMutuallyExclusiveRequests();
}
