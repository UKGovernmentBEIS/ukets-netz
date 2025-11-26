package uk.gov.netz.api.workflow.request.core.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.AccountRequestAuthorizationResourceService;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestCreateByAccountValidator;

@Service
@RequiredArgsConstructor
public class AvailableRequestAccountHandler implements AvailableRequestResourceTypeHandler {

	private final AccountRequestAuthorizationResourceService accountRequestAuthorizationResourceService;
    private final List<RequestCreateByAccountValidator> requestCreateByAccountValidators;
    
	@Override
	public Map<String, RequestCreateValidationResult> getAvailableRequestsForResource(
			final String resourceId, final Set<String> requestTypes, final AppUser appUser) {
		final Long accountId = Long.parseLong(resourceId);
		final Set<String> actions = getAvailableCreateActions(accountId, appUser, requestTypes);
		
		return actions.stream()
                .collect(Collectors.toMap(
                        requestType -> requestType,
                        requestType -> getAccountValidationResult(requestType, accountId)))
                .entrySet().stream()
                .filter(a -> a.getValue().isAvailable())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	private Set<String> getAvailableCreateActions(final Long accountId, final AppUser appUser,
			final Set<String> availableCreateRequestTypes) {
        return accountRequestAuthorizationResourceService
                .findRequestCreateActionsByAccountId(appUser, accountId).stream()
                .filter(availableCreateRequestTypes::contains)
                .collect(Collectors.toSet());
    }

    private RequestCreateValidationResult getAccountValidationResult(String type, long accountId) {
        return requestCreateByAccountValidators.stream()
                .filter(validator -> validator.getRequestType().equals(type))
                .findFirst()
                .map(validator -> validator.validateAction(accountId))
                .orElse(RequestCreateValidationResult.builder().valid(true).build());
    }

	@Override
	public String getResourceType() {
		return ResourceType.ACCOUNT;
	}

}
