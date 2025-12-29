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
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestTaskAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.ResourceAuthorityInfo;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.RequestTaskAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestTaskAccessRuleHandlerTest {
    @InjectMocks
    private RequestTaskAccessRuleHandler requestTaskAccessRuleHandler;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    @Mock
    private RequestTaskAuthorityInfoProvider requestTaskAuthorityInfoProvider;
    
    @Mock
    private AuthorizationRulesQueryService authorizationRulesQueryService;
    

    private final AppUser USER = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();

    @Test
    void wrong_resourceId_type() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .build();

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        assertThrows(NumberFormatException.class,
                () -> requestTaskAccessRuleHandler.evaluateRules(rules, USER, "wrong"));

        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void requestTask_does_not_exist() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .build();

        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1"));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void single_rule() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                    .resourceSubType("ACCOUNT_USERS_SETUP")
                    .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("ACCOUNT_USERS_SETUP")
                .requestType("ACCOUNT_USERS")
                .assignee(USER.getUserId())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "2", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenReturn(requestTaskInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("ACCOUNT_USERS"));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1");

        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(requestTaskInfoDTO.getAuthorityInfo().getRequestResources())
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria);
        verify(authorizationRulesQueryService, times(1)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType());
        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void single_rule_no_matching() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .resourceSubType("TYPE2_SUBMIT")
            .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("TYPE2_ACTIVATE")
                .requestType("TYPE2")
                .assignee(USER.getUserId())
                .build();
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenReturn(requestTaskInfoDTO);

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);

        //invoke
        BusinessException exception = assertThrows(BusinessException.class,
                () -> requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1"));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());

        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void multiple_rules() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .resourceSubType("ACCOUNT_USERS_SETUP")
            .build();

        AuthorizationRuleScopePermission authorizationRule2 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_CA_USERS_EDIT)
            .resourceSubType("ACCOUNT_USERS_SETUP")
            .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("ACCOUNT_USERS_SETUP")
                .requestType("ACCOUNT_USERS")
                .assignee(USER.getUserId())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "2", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "2"))
                        .build())
                .build();
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenReturn(requestTaskInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("ACCOUNT_USERS"));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1, authorizationRule2);
        requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1");

        AuthorizationCriteria authorizationCriteria1 = AuthorizationCriteria.builder()
        		.requestResources(requestTaskInfoDTO.getAuthorityInfo().getRequestResources())
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria1);

        AuthorizationCriteria authorizationCriteria2 = AuthorizationCriteria.builder()
        		.requestResources(requestTaskInfoDTO.getAuthorityInfo().getRequestResources())
                .permission(Permission.PERM_CA_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria2);
        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void multiple_rules_one_applicable() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .resourceSubType("ACCOUNT_USERS_SETUP")
            .build();

        AuthorizationRuleScopePermission authorizationRule2 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_CA_USERS_EDIT)
            .resourceSubType("TYPE")
            .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("ACCOUNT_USERS_SETUP")
                .requestType("ACCOUNT_USERS")
                .assignee(USER.getUserId())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "2", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "2"))
                        .build())
                .build();
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenReturn(requestTaskInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("ACCOUNT_USERS"));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1, authorizationRule2);
        requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1");

        AuthorizationCriteria authorizationCriteria1 = AuthorizationCriteria.builder()
        		.requestResources(requestTaskInfoDTO.getAuthorityInfo().getRequestResources())
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        verify(appAuthorizationService, times(1)).authorize(USER, authorizationCriteria1);
        verifyNoMoreInteractions(appAuthorizationService);
    }
    
    @Test
    void single_rule_unauthorized_request_type() {
        AuthorizationRuleScopePermission authorizationRule1 = 
                AuthorizationRuleScopePermission.builder()
                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                    .resourceSubType("ACCOUNT_USERS_SETUP")
                    .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("ACCOUNT_USERS_SETUP")
                .requestType("ACCOUNT_USERS")
                .assignee(USER.getUserId())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "2", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();
        
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(requestTaskInfoDTO.getAuthorityInfo().getRequestResources())
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build();
        
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(1L)).thenReturn(requestTaskInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("CA_USERS"));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        
        final BusinessException be = assertThrows(BusinessException.class,
				() -> requestTaskAccessRuleHandler.evaluateRules(rules, USER, "1"));
		
		assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(appAuthorizationService, times(0)).authorize(USER, authorizationCriteria);
        verify(authorizationRulesQueryService, times(1)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType());
        verifyNoMoreInteractions(appAuthorizationService);
    }
}
