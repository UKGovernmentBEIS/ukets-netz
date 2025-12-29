package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestActionAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.RequestActionAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("requestActionAccountBasedViewHandler")
@RequiredArgsConstructor
public class RequestActionAccountBasedViewRuleHandler implements AuthorizationResourceRuleHandler {

    private final AppAuthorizationService appAuthorizationService;
    private final RequestActionAuthorityInfoProvider requestActionAuthorityInfoProvider;
    private final AuthorizationRulesQueryService authorizationRulesQueryService;

    /**
     * Evaluates the {@code authorizationRules} on the {@code resourceId}, which must correspond to an existing request action.
     *
     * @param authorizationRules the list of
     * @param user the authenticated user
     * @param resourceId the resourceId for which the rules apply.
     */
    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user, String resourceId) {
        RequestActionAuthorityInfoDTO requestActionInfo = requestActionAuthorityInfoProvider.getRequestActionAuthorityInfo(Long.valueOf(resourceId));

        List<AuthorizationRuleScopePermission> appliedRules =
                authorizationRules.stream()
                        .filter(rule -> requestActionInfo.getType().equals(rule.getResourceSubType()))
                        .toList();

        if (appliedRules.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        final Set<String> userAllowedRequestTypes = authorizationRulesQueryService
                .findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());
        
        if(!userAllowedRequestTypes.contains(requestActionInfo.getRequestType())) {
        	throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        appliedRules.forEach(rule ->
                appAuthorizationService.authorize(user, AuthorizationCriteria.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, requestActionInfo.getAuthorityInfo().getAccountId().toString()))
                        .permission(rule.getPermission()).build()));
    }
}