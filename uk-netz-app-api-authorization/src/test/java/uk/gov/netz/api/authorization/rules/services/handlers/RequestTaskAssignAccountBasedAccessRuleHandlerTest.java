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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestTaskAssignAccountBasedAccessRuleHandlerTest {

    @InjectMocks
    private RequestTaskAssignAccountBasedAccessRuleHandler requestTaskAssignAccessRuleHandler;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    @Mock
    private RequestTaskAuthorityInfoProvider requestTaskAuthorityInfoProvider;

    @Test
    void evaluateRules() {
        AppUser user = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();
        Long requestTaskId = 1L;
        AuthorizationRuleScopePermission authorizationRule1 =
                AuthorizationRuleScopePermission.builder()
                        .permission(Permission.PERM_TASK_ASSIGNMENT)
                        .build();

        RequestTaskAuthorityInfoDTO requestTaskInfoDTO = RequestTaskAuthorityInfoDTO.builder()
                .type("ACCOUNT_USERS_SETUP")
                .assignee(user.getUserId())
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();
        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(requestTaskId)).thenReturn(requestTaskInfoDTO);

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        AuthorizationCriteria authorizationCriteria1 = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, requestTaskInfoDTO.getAuthorityInfo().getAccountId().toString()))
                .permission(Permission.PERM_TASK_ASSIGNMENT)
                .build();
        requestTaskAssignAccessRuleHandler.evaluateRules(rules, user, requestTaskId.toString());

        verify(appAuthorizationService, times(1)).authorize(user, authorizationCriteria1);

        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void evaluateRules_wrong_resourceId_type() {
        AppUser user = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationRuleScopePermission authorizationRule1 =
                AuthorizationRuleScopePermission.builder()
                        .permission(Permission.PERM_TASK_ASSIGNMENT)
                        .build();

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        assertThrows(NumberFormatException.class,
                () -> requestTaskAssignAccessRuleHandler.evaluateRules(rules, user, "wrong"));

        verifyNoMoreInteractions(appAuthorizationService);
    }

    @Test
    void evaluateRules_requestTask_does_not_exist() {
        AppUser user = AppUser.builder().userId("userId").roleType(RoleTypeConstants.OPERATOR).build();
        Long requestTaskId = 1L;
        AuthorizationRuleScopePermission authorizationRule1 =
                AuthorizationRuleScopePermission.builder()
                        .permission(Permission.PERM_TASK_ASSIGNMENT)
                        .build();

        when(requestTaskAuthorityInfoProvider.getRequestTaskInfo(requestTaskId)).thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> requestTaskAssignAccessRuleHandler.evaluateRules(rules, user, requestTaskId.toString()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verifyNoMoreInteractions(appAuthorizationService);
    }

}