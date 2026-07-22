package uk.gov.netz.api.workflow.request.flow.common.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.Set;

public interface RequestCreateByAccountValidator<T extends RequestCreateActionPayload> extends RequestCreateValidator {

    @Transactional
    RequestCreateValidationResult checkAvailability(Long accountId);

    @Transactional
    RequestCreateValidationResult validateCreation(Long accountId, T payload);

    Set<AccountStatus> getApplicableAccountStatuses();

    Set<String> getMutuallyExclusiveRequests();
}
