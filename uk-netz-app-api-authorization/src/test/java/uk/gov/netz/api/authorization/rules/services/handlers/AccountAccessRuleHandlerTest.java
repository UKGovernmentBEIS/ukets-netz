package uk.gov.netz.api.authorization.rules.services.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountAccessRuleHandlerTest {

    @InjectMocks
    private AccountAccessRuleHandler accountAccessRuleHandler;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    private final AppUser USER = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();

    @Test
    void single_rule() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .build();
        
        accountAccessRuleHandler.evaluateRules(Set.of(authorizationRulePermissionScope1), USER, "1");

        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria);
    }

    @Test
    void multiple_rules() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .build();
        
        AuthorizationRuleScopePermission authorizationRulePermissionScope2 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_CA_USERS_EDIT)
            .build();
        
        accountAccessRuleHandler.evaluateRules(Set.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2), USER, "1");

        AuthorizationCriteria authorizationCriteria1 = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria1);

        AuthorizationCriteria authorizationCriteria2 = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .permission(Permission.PERM_CA_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria2);
    }
}
