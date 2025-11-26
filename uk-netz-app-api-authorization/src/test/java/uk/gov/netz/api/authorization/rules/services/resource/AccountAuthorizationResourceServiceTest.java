package uk.gov.netz.api.authorization.rules.services.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.ResourceScopePermissionService;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAuthorizationResourceServiceTest {

    @InjectMocks
    private AccountAuthorizationResourceService service;
    
    @Mock
    private ResourceScopePermissionService resourceScopePermissionService;
    
    @Mock
    private AppAuthorizationService appAuthorizationService;
    
    @Test
    void hasUserScopeToAccount() {
    	String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        Long accountId = 1L;
        String scope = Scope.EDIT_USER;
        
        ResourceScopePermission resourceScopePermission = 
                ResourceScopePermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build();
        
        when(resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));
        
        boolean result = service.hasUserScopeToAccount(authUser, accountId, scope);
        
        assertThat(result).isTrue();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }

    @Test
    void hasUserScopeToAccount_no_permission() {
        String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        Long accountId = 1L;
        String scope = Scope.EDIT_USER;

        when(resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope))
                .thenReturn(Optional.empty());

        boolean result = service.hasUserScopeToAccount(authUser, accountId, scope);

        assertThat(result).isFalse();
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope);
        verify(appAuthorizationService, never()).authorize(any(), any());
    }
    
    @Test
    void hasUserScopeToAccount_not_authorized() {
    	String roleType = RoleTypeConstants.OPERATOR;
    	AppUser authUser = AppUser.builder().roleType(roleType).build();
        Long accountId = 1L;
        String scope = Scope.EDIT_USER;
        
        ResourceScopePermission resourceScopePermission = 
                ResourceScopePermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build();
        
        AuthorizationCriteria authCriteria = 
                AuthorizationCriteria.builder()
                	.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build();
        
        when(resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));
        
        doThrow(BusinessException.class).when(appAuthorizationService).authorize(authUser, authCriteria);
        
        boolean result = service.hasUserScopeToAccount(authUser, accountId, scope);
        
        assertThat(result).isFalse();
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, roleType, scope);
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }
}
