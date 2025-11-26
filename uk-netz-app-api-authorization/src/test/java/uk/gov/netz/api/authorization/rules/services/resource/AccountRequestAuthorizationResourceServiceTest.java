package uk.gov.netz.api.authorization.rules.services.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationServiceDelegator;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRequestAuthorizationResourceServiceTest {

    @InjectMocks
    private AccountRequestAuthorizationResourceService service;
    
    @Mock
    private AuthorizationRuleRepository authorizationRuleRepository;
    
    @Mock
    private RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator;
    
    @Test
    void findRequestCreateActionsByAccountId() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        Long accountId = 1L;
        
        List<AuthorizationRuleScopePermission> rules = List.of(
                AuthorizationRuleScopePermission.builder().resourceSubType("requestType").handler("handler").permission(null).build());
        
        when(authorizationRuleRepository.findRulePermissionsByResourceTypeScopeAndRoleType(ResourceType.ACCOUNT, Scope.REQUEST_CREATE, RoleTypeConstants.OPERATOR)).thenReturn(rules);
        
        when(roleTypeAuthorizationServiceDelegator.isAuthorized(user, AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
        		.permission(null).build()))
        .thenReturn(true);
        
        Set<String> results = service.findRequestCreateActionsByAccountId(user, accountId);
        
        assertThat(results)
            .hasSize(1)
            .containsOnly("requestType");
    }
}
