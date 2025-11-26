package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;
import java.util.Set;

@Service("accountRequestCreateHandler")
@RequiredArgsConstructor
public class AccountRequestCreateRuleHandler implements AuthorizationResourceRuleHandler {
    
    private final AppAuthorizationService appAuthorizationService;
    
    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user,
            String resourceId) {
        if (authorizationRules.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (resourceId != null) {
            authorizationRules.forEach(rule -> appAuthorizationService.authorize(user,
                    AuthorizationCriteria.builder()
                    .requestResources(Map.of(ResourceType.ACCOUNT, resourceId))
                    .permission(rule.getPermission())
                    .build()));
        }
    }
}
