package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByAccountValidator;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByRequestValidator;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestCreateActionAccountResourceTypeHandler<T extends RequestCreateActionPayload> implements RequestCreateActionResourceTypeHandler<T> {
    private final List<RequestCreateByAccountValidator> requestCreateByAccountValidators;
    private final List<RequestCreateByRequestValidator<T>> requestCreateByRequestValidators;
    private final List<RequestAccountCreateActionHandler<T>> requestAccountCreateActionHandlers;
    private final AccountQueryService accountQueryService;

    @Override
    public String process(String resourceId, String requestType, T payload, AppUser appUser) {
        Long accountId = Long.parseLong(resourceId);

        // lock the account
        accountQueryService.exclusiveLockAccount(accountId);

        Optional<RequestCreateByAccountValidator> requestCreateByAccountValidatorOpt = requestCreateByAccountValidators
                .stream()
                .filter(requestCreateValidator -> requestCreateValidator.getRequestType().equals(requestType))
                .findFirst();

        Optional<RequestCreateByRequestValidator<T>> requestCreateByRequestValidatorOpt = requestCreateByRequestValidators
                .stream().filter(requestCreateValidator -> requestCreateValidator.getRequestType().equals(requestType)).findFirst();


        final RequestCreateValidationResult validationResult = requestCreateByAccountValidatorOpt
                .map(requestCreateByAccountValidator -> requestCreateByAccountValidator.validateAction(accountId))
                .orElse(requestCreateByRequestValidatorOpt
                        .map(requestCreateByRequestValidator -> requestCreateByRequestValidator.validateAction(accountId, payload))
                        .orElse(RequestCreateValidationResult.builder().valid(true).isAvailable(true).build())
                );


        if (!validationResult.isValid() || !validationResult.isAvailable()) {
            throw new BusinessException(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED, validationResult);
        }

        return requestAccountCreateActionHandlers.stream()
                .filter(handler -> handler.getRequestType().equals(requestType))
                .findFirst()
                .map(handler -> handler.process(accountId, payload, appUser))
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, requestType));
    }

    @Override
    public String getResourceType() {
        return ResourceType.ACCOUNT;
    }
}
