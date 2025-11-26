package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestQueryService;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateAccountStatusValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateRequestTypeValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestCreateValidatorService {

    private final AccountQueryService accountQueryService;
    private final RequestQueryService requestQueryService;

    public RequestCreateValidationResult validate(final Long accountId,
                                                  Set<AccountStatus> applicableAccountStatuses, Set<String> mutuallyExclusiveRequestsTypes) {
        final RequestCreateValidationResult validationResult = RequestCreateValidationResult.builder().valid(true)
                .build();

        final RequestCreateAccountStatusValidationResult validationAccountStatusesResult = validateAccountStatuses(
                accountId, applicableAccountStatuses);
        if (!validationAccountStatusesResult.isValid()) {
            final Set<String> applicableAccountStatusSet = applicableAccountStatuses.stream()
                    .map(AccountStatus::getName)
                    .collect(Collectors.toSet());
            validationResult.setValid(false);
            validationResult.setApplicableAccountStatuses(applicableAccountStatusSet);
            validationResult.setReportedAccountStatus(validationAccountStatusesResult.getReportedAccountStatus());
        }

        final RequestCreateRequestTypeValidationResult validationConflictingRequestsTypesResult = validateConflictingRequestTypes(
                accountId, mutuallyExclusiveRequestsTypes);
        if (!validationConflictingRequestsTypesResult.isValid()) {
            validationResult.setValid(false);
            validationResult.setReportedRequestTypes(validationConflictingRequestsTypesResult.getReportedRequestTypes());
        }

        return validationResult;
    }

    public RequestCreateAccountStatusValidationResult validateAccountStatuses(final Long accountId,
                                                                              Set<AccountStatus> applicableAccountStatuses) {

        final AccountStatus accountStatus = accountQueryService.getAccountStatus(accountId);

        final boolean validAccountStatus = applicableAccountStatuses.isEmpty()
                || applicableAccountStatuses.contains(accountStatus);

        final RequestCreateAccountStatusValidationResult validationResult = !validAccountStatus
                ? new RequestCreateAccountStatusValidationResult(false, accountStatus)
                : new RequestCreateAccountStatusValidationResult(true);

        return validationResult;
    }

    public RequestCreateRequestTypeValidationResult validateConflictingRequestTypes(final Long accountId,
                                                                                    Set<String> mutuallyExclusiveRequestsTypes) {
        final RequestCreateRequestTypeValidationResult validationResult = RequestCreateRequestTypeValidationResult.builder().valid(true)
                .build();

        if (!mutuallyExclusiveRequestsTypes.isEmpty()) {
            final List<Request> inProgressRequests = requestQueryService.findInProgressRequestsByAccount(accountId);
            final Set<String> conflictingRequests = inProgressRequests.stream().map(Request::getType).map(RequestType::getCode)
                    .filter(mutuallyExclusiveRequestsTypes::contains).collect(Collectors.toSet());

            if (!conflictingRequests.isEmpty()) {
                validationResult.setValid(false);
                validationResult.setReportedRequestTypes(conflictingRequests);
            }
        }

        return validationResult;
    }
}
