package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationServiceDelegator;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AccountRequestAuthorizationResourceService {

    private final AuthorizationRuleRepository authorizationRuleRepository;
    private final RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator;
    
    public Set<String> findRequestCreateActionsByAccountId(AppUser user, Long accountId) {
        List<AuthorizationRuleScopePermission> rules = 
                authorizationRuleRepository
                        .findRulePermissionsByResourceTypeScopeAndRoleType(ResourceType.ACCOUNT, Scope.REQUEST_CREATE, user.getRoleType());
        
        Set<String> allowedActions = new HashSet<>();
        rules.forEach(rule -> {
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
