package uk.gov.netz.api.authorization.rules.services.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationServiceDelegator;

@RequiredArgsConstructor
@Service
public class AccountRequestAuthorizationResourceService {

    private final AuthorizationRuleRepository authorizationRuleRepository;
    private final RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator;
    private final AuthorizationRulesQueryService authorizationRulesQueryService;
    
    public Set<String> findRequestCreateActionsByAccountId(AppUser user, Long accountId) {
    	final Set<String> userAllowedRequestTypes = authorizationRulesQueryService
                .findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());
    	
        List<AuthorizationRuleScopePermission> compatibleRules = 
                authorizationRuleRepository
                        .findRulePermissionsByResourceTypeScopeAndRoleType(ResourceType.ACCOUNT, Scope.REQUEST_CREATE, user.getRoleType())
                        .stream()
                        .filter(rule -> userAllowedRequestTypes.contains(rule.getResourceSubType()))
                        .toList();   
        
        Set<String> allowedActions = new HashSet<>();
        compatibleRules.forEach(rule -> {
            if (roleTypeAuthorizationServiceDelegator.isAuthorized(user,
                    AuthorizationCriteria.builder()
                    .requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                    .permission(rule.getPermission())
                    .build())) {
                allowedActions.add(rule.getResourceSubType());
            }
        });
        
        return allowedActions;
    }
}
