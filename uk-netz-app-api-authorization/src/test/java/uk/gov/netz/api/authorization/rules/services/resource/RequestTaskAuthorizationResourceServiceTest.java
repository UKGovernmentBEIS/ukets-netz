package uk.gov.netz.api.authorization.rules.services.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskAuthorizationResourceServiceTest {
    private RequestTaskAuthorizationResourceService service;

    private RequestTaskRoleTypeAuthorizationQueryService operatorRequestTaskRoleTypeAuthorizationQueryService;
    private RequestTaskRoleTypeAuthorizationQueryService regulatorRequestTaskRoleTypeAuthorizationQueryService;
    private RequestTaskRoleTypeAuthorizationQueryService verifierRequestTaskRoleTypeAuthorizationQueryService;
    private ResourceScopePermissionService resourceScopePermissionService;
    private AppAuthorizationService appAuthorizationService;

    @BeforeEach
    void setup() {
        operatorRequestTaskRoleTypeAuthorizationQueryService = Mockito.mock(RequestTaskRoleTypeAuthorizationQueryService.class);
        regulatorRequestTaskRoleTypeAuthorizationQueryService = Mockito.mock(RequestTaskRoleTypeAuthorizationQueryService.class);
        verifierRequestTaskRoleTypeAuthorizationQueryService = Mockito.mock(RequestTaskRoleTypeAuthorizationQueryService.class);
        resourceScopePermissionService = Mockito.mock(ResourceScopePermissionService.class);
        appAuthorizationService = Mockito.mock(AppAuthorizationService.class);

        service = new RequestTaskAuthorizationResourceService(List.of(
                operatorRequestTaskRoleTypeAuthorizationQueryService,
                regulatorRequestTaskRoleTypeAuthorizationQueryService,
                verifierRequestTaskRoleTypeAuthorizationQueryService),
                resourceScopePermissionService,
                appAuthorizationService);
    }

    @Test
    void hasUserExecuteScopeOnRequestTaskType() {
    	String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        Long verificationBodyId = 2L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String scope = Scope.REQUEST_TASK_EXECUTE;

        ResourceScopePermission resourceScopePermission =
            ResourceScopePermission.builder().permission(Permission.PERM_CA_USERS_EDIT).build();

        when(
            resourceScopePermissionService.findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));

        ResourceCriteria resourceCriteria =
            ResourceCriteria.builder()
            .requestResources(Map.of(
            		ResourceType.ACCOUNT, accountId.toString(),
            		ResourceType.CA, competentAuthority.name(),
            		ResourceType.VERIFICATION_BODY, verificationBodyId.toString()
            		))
            .build();
        boolean result = service.hasUserExecuteScopeOnRequestTaskType(authUser, requestTaskType, resourceCriteria);

        assertThat(result).isTrue();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getCompetentAuthority()).isEqualTo(competentAuthority);
        assertThat(criteriaCaptured.getVerificationBodyId()).isEqualTo(verificationBodyId);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }


    @Test
    void hasUserExecuteScopeOnRequestTaskType_verifier() {
    	String roleType = RoleTypeConstants.VERIFIER;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String scope = Scope.REQUEST_TASK_EXECUTE;

        ResourceScopePermission resourceScopePermission =
                ResourceScopePermission.builder().permission(Permission.PERM_CA_USERS_EDIT).build();

        when(
                resourceScopePermissionService.findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
                .thenReturn(Optional.of(resourceScopePermission));

        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        boolean result = service.hasUserExecuteScopeOnRequestTaskType(authUser, requestTaskType, resourceCriteria);

        assertThat(result).isTrue();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
                ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getCompetentAuthority()).isNull();
        assertThat(criteriaCaptured.getVerificationBodyId()).isNull();
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }

    @Test
    void hasUserExecuteScopeOnRequestTaskType_not_authorized() {
    	String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String scope = Scope.REQUEST_TASK_EXECUTE;

        ResourceScopePermission resourceScopePermission =
            ResourceScopePermission.builder().permission(Permission.PERM_CA_USERS_EDIT).build();

        AuthorizationCriteria authCriteria =
            AuthorizationCriteria.builder()
            	.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString(), 
    				ResourceType.CA, competentAuthority.name()))
                .permission(Permission.PERM_CA_USERS_EDIT).build();

        when(
            resourceScopePermissionService.findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));

        doThrow(BusinessException.class).when(appAuthorizationService).authorize(authUser, authCriteria);

        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        boolean result = service.hasUserExecuteScopeOnRequestTaskType(authUser, requestTaskType, resourceCriteria);

        assertThat(result).isFalse();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getCompetentAuthority()).isEqualTo(competentAuthority);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }

    @Test
    void hasUserAssignScopeOnRequestTasks() {
    	String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String scope = Scope.REQUEST_TASK_ASSIGN;

        ResourceScopePermission resourceScopePermission =
            ResourceScopePermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build();

        when(resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));

        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                ))
                .build();
        boolean result = service.hasUserAssignScopeOnRequestTasks(authUser, resourceCriteria);

        assertThat(result).isTrue();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getCompetentAuthority()).isEqualTo(competentAuthority);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }

    @Test
    void hasUserAssignScopeOnRequestTasks_not_authorized() {
    	String roleType = RoleTypeConstants.OPERATOR;
        AppUser authUser = AppUser.builder().roleType(roleType).build();
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String scope = Scope.REQUEST_TASK_ASSIGN;

        ResourceScopePermission resourceScopePermission =
            ResourceScopePermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build();

        AuthorizationCriteria authCriteria =
            AuthorizationCriteria.builder()
            		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString(), 
            				ResourceType.CA, competentAuthority.name()))
    				.permission(Permission.PERM_TASK_ASSIGNMENT)
                .build();

        when(resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, roleType, scope))
            .thenReturn(Optional.of(resourceScopePermission));

        doThrow(BusinessException.class).when(appAuthorizationService).authorize(authUser, authCriteria);

        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        boolean result = service.hasUserAssignScopeOnRequestTasks(authUser, resourceCriteria);

        assertThat(result).isFalse();
        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(resourceScopePermissionService, times(1)).findByResourceTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, roleType, scope);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(authUser), criteriaCaptor.capture());
        AuthorizationCriteria criteriaCaptured = criteriaCaptor.getValue();
        assertThat(criteriaCaptured.getAccountId()).isEqualTo(accountId);
        assertThat(criteriaCaptured.getCompetentAuthority()).isEqualTo(competentAuthority);
        assertThat(criteriaCaptured.getPermission()).isEqualTo(resourceScopePermission.getPermission());
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_operator_requires_permission() {
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String roleType = RoleTypeConstants.OPERATOR;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        List<String> users = List.of("user");

        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(true);
        when(operatorRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
            requestTaskType, resourceCriteria, true))
            .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(operatorRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, true);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_operator_requires_no_permission() {
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String roleType = RoleTypeConstants.OPERATOR;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        List<String> users = List.of("user");

        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(false);
        when(operatorRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false))
                .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(operatorRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_regulator_requires_permission() {
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String roleType = RoleTypeConstants.REGULATOR;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        List<String> users = List.of("user");

        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
                ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
                .thenReturn(true);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, true))
                .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(regulatorRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, true);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_regulator_requires_no_permission() {
        String requestTaskType = "requestTaskType";
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String roleType = RoleTypeConstants.REGULATOR;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, competentAuthority.name()
                		))
                .build();
        List<String> users = List.of("user");

        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
                ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
                .thenReturn(false);
        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false))
                .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(regulatorRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_verifier_requires_permission() {
        String requestTaskType = "requestTaskType";
        Long verificationBodyId = 1L;
        String roleType = RoleTypeConstants.VERIFIER;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(ResourceType.VERIFICATION_BODY, verificationBodyId.toString()))
                .build();
        List<String> users = List.of("user");

        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(verifierRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.VERIFIER);
        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(true);
        when(verifierRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, true))
                .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(verifierRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, true);
    }

    @Test
    void findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType_verifier_requires_no_permission() {
        String requestTaskType = "requestTaskType";
        Long verificationBodyId = 1L;
        String roleType = RoleTypeConstants.VERIFIER;
        String scope = Scope.REQUEST_TASK_EXECUTE;
        ResourceCriteria resourceCriteria =
        		ResourceCriteria.builder()
                .requestResources(Map.of(ResourceType.VERIFICATION_BODY, verificationBodyId.toString()))
                .build();
        List<String> users = List.of("user");

        when(operatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(verifierRequestTaskRoleTypeAuthorizationQueryService.getRoleType()).thenReturn(RoleTypeConstants.VERIFIER);
        when(resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope))
            .thenReturn(false);
        when(verifierRequestTaskRoleTypeAuthorizationQueryService.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false))
                .thenReturn(users);

        List<String> usersFound =
            service.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTaskType, resourceCriteria, roleType);

        assertThat(usersFound).containsAll(users);
        verify(resourceScopePermissionService, times(1)).existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            ResourceType.REQUEST_TASK, requestTaskType, roleType, scope);
        verify(verifierRequestTaskRoleTypeAuthorizationQueryService).findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(
                requestTaskType, resourceCriteria, false);
    }

    @Test
    void findRequestTaskTypesByRoleType() {
    	String roleType = RoleTypeConstants.REGULATOR;

        when(resourceScopePermissionService.findByResourceTypeAndRoleType(ResourceType.REQUEST_TASK, roleType))
            .thenReturn(
                Set.of(
                    ResourceScopePermission.builder().resourceSubType("requestTaskType").build(),
                    ResourceScopePermission.builder().resourceSubType(null).build()
                )
            );

        Set<String> actualRequestTaskTypes = service.findRequestTaskTypesByRoleType(roleType);

        assertThat(actualRequestTaskTypes).containsOnly("requestTaskType");
    }

}
