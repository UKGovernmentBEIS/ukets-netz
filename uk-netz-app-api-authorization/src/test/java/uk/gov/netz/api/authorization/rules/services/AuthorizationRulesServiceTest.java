package uk.gov.netz.api.authorization.rules.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationRulesServiceTest {
    private AuthorizationRulesService authorizationRulesService;

    private AuthorizationRuleRepository authorizationRuleRepository;
    private AuthorizationResourceRuleHandler authorizationResourceRuleHandler1;
    private AuthorizationResourceRuleHandler authorizationResourceRuleHandler2;
    private AuthorizationRuleHandler authorizationRuleHandler1;
    private AuthorizationRuleHandler authorizationRuleHandler2;

    private final AppUser OPERATOR = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();

    @BeforeEach
    void setup() {
        authorizationRuleRepository = Mockito.mock(AuthorizationRuleRepository.class);
        authorizationResourceRuleHandler1 = Mockito.mock(AuthorizationResourceRuleHandler.class);
        authorizationResourceRuleHandler2 = Mockito.mock(AuthorizationResourceRuleHandler.class);
        authorizationRuleHandler1 = Mockito.mock(AuthorizationRuleHandler.class);
        authorizationRuleHandler2 = Mockito.mock(AuthorizationRuleHandler.class);

        authorizationRulesService = new AuthorizationRulesService(authorizationRuleRepository,
                Map.of("authorizationResourceRuleHandler1", authorizationResourceRuleHandler1,
                        "authorizationResourceRuleHandler2", authorizationResourceRuleHandler2),
                Map.of("authorizationRuleHandler1", authorizationRuleHandler1,
                        "authorizationRuleHandler2", authorizationRuleHandler2));
    }

    @Test
    void evaluateRules_with_resource_param_not_authorized_service() {
        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null))
                .thenReturn(List.of());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, null));
        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_param_single_handler() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);


        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, null);

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(new HashSet<>(rules), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationResourceRuleHandler2, authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_param_multiple_handlers() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler2")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);


        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, null);

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope1), OPERATOR, "resourceId");
        verify(authorizationResourceRuleHandler2, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope2), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_no_resource_not_authorized_service() {
        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null))
                .thenReturn(List.of());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> authorizationRulesService.evaluateRules(OPERATOR, "service"));
        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_no_resource_single_handler() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationRuleHandler1")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationRuleHandler1")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);


        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verify(authorizationRuleHandler1, times(1))
                .evaluateRules(new HashSet<>(rules), OPERATOR);
        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_no_resource_multiple_handlers() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationRuleHandler1")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationRuleHandler2")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);


        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, null);
        verify(authorizationRuleHandler1, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope1), OPERATOR);
        verify(authorizationRuleHandler2, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope2), OPERATOR);
        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_param_not_authorized_service() {
        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType("service", RoleTypeConstants.OPERATOR, "resourceType", null))
                .thenReturn(List.of());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", null));
        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType", null);

        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_param_single_handler() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), "resourceType", null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", null);

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType", null);

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(new HashSet<>(rules), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_param_multiple_handlers() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler2")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), "resourceType", null))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", null);

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType", null);

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope1), OPERATOR, "resourceId");
        verify(authorizationResourceRuleHandler2, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope2), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_sub_type_param_not_authorized_service() {
        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType("service", RoleTypeConstants.OPERATOR, null, "resourceSybType"))
                .thenReturn(List.of());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, "resourceSybType"));
        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());
        
        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, "resourceSybType");

        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_sub_type_param_single_handler() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, "resourceSybType"))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, "resourceSybType");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null,  "resourceSybType");

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(new HashSet<>(rules), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_sub_type_param_multiple_handlers() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler2")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), null, "resourceSybType"))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", null, "resourceSybType");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, null, "resourceSybType");

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope1), OPERATOR, "resourceId");
        verify(authorizationResourceRuleHandler2, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope2), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_resource_sub_type_param_not_authorized_service() {
        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType("service", RoleTypeConstants.OPERATOR, "resourceType", "resourceSybType"))
                .thenReturn(List.of());

        BusinessException businessException = assertThrows(BusinessException.class,
                () -> authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", "resourceSybType"));
        assertEquals(ErrorCode.FORBIDDEN, businessException.getErrorCode());

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType", "resourceSybType");

        verifyNoInteractions(authorizationResourceRuleHandler1, authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_resource_sub_type_param_single_handler() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), "resourceType", "resourceSybType"))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", "resourceSybType");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType",  "resourceSybType");

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(new HashSet<>(rules), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationResourceRuleHandler2,
                authorizationRuleHandler1, authorizationRuleHandler2);
    }

    @Test
    void evaluateRules_with_resource_type_resource_sub_type_param_multiple_handlers() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler1")
                        .resourceSubType("resourceSybType")
                        .build();

        AuthorizationRuleScopePermission authorizationRulePermissionScope2 =
                AuthorizationRuleScopePermission.builder()
                        .handler("authorizationResourceRuleHandler2")
                        .resourceSubType("resourceSybType")
                        .build();

        List<AuthorizationRuleScopePermission> rules =
                List.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2);

        when(authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", OPERATOR.getRoleType(), "resourceType", "resourceSybType"))
                .thenReturn(rules);

        authorizationRulesService.evaluateRules(OPERATOR, "service", "resourceId", "resourceType", "resourceSybType");

        verify(authorizationRuleRepository, times(1)).findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.OPERATOR, "resourceType", "resourceSybType");

        verify(authorizationResourceRuleHandler1, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope1), OPERATOR, "resourceId");
        verify(authorizationResourceRuleHandler2, times(1))
                .evaluateRules(Set.of(authorizationRulePermissionScope2), OPERATOR, "resourceId");
        verifyNoInteractions(authorizationRuleHandler1, authorizationRuleHandler2);
    }
}
