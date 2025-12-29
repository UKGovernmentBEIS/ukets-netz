package uk.gov.netz.api.authorization.rules.services.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestActionAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.ResourceAuthorityInfo;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.RequestActionAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestActionAccountBasedViewRuleHandlerTest {

    @InjectMocks
    private RequestActionAccountBasedViewRuleHandler handler;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    @Mock
    private RequestActionAuthorityInfoProvider requestActionAuthorityInfoProvider;
    
    @Mock
    private AuthorizationRulesQueryService authorizationRulesQueryService;

    @Test
    void evaluateRules() {
        AppUser user = AppUser.builder().userId("user").roleType(RoleTypeConstants.REGULATOR).build();
        String resourceId = "1";
        Set<AuthorizationRuleScopePermission> authorizationRules = Set.of(
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType1")
                        .handler("handler")
                        .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build(),
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType2")
                        .handler("handler")
                        .permission(Permission.PERM_CA_USERS_EDIT).build()
        );
        RequestActionAuthorityInfoDTO requestActionInfoDTO = RequestActionAuthorityInfoDTO.builder()
                .id(Long.valueOf(resourceId))
                .type("requestActionType1")
                .requestType(resourceId)
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();

        when(requestActionAuthorityInfoProvider.getRequestActionAuthorityInfo(Long.valueOf(resourceId)))
                .thenReturn(requestActionInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType())).thenReturn(Set.of(resourceId));


        //invoke
        handler.evaluateRules(authorizationRules, user, resourceId);

        verify(requestActionAuthorityInfoProvider, times(1)).getRequestActionAuthorityInfo(Long.valueOf(resourceId));
        verify(authorizationRulesQueryService, times(1)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());

        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(appAuthorizationService, times(1)).authorize(Mockito.eq(user), criteriaCaptor.capture());
        AuthorizationCriteria criteria = criteriaCaptor.getValue();
        assertThat(criteria).isEqualTo(
                AuthorizationCriteria.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                        .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build());
    }

    @Test
    void evaluateRules_no_rules_applied() {
        AppUser user = AppUser.builder().userId("user").build();
        String resourceId = "1";
        Set<AuthorizationRuleScopePermission> authorizationRules = Set.of(
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType1")
                        .handler("handler")
                        .permission(Permission.PERM_CA_USERS_EDIT).build()
        );
        RequestActionAuthorityInfoDTO requestActionInfoDTO = RequestActionAuthorityInfoDTO.builder()
                .id(Long.valueOf(resourceId))
                .type("requestActionType2")
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();

        when(requestActionAuthorityInfoProvider.getRequestActionAuthorityInfo(Long.valueOf(resourceId)))
                .thenReturn(requestActionInfoDTO);

        //invoke
        BusinessException be = assertThrows(BusinessException.class, () -> handler.evaluateRules(authorizationRules, user, resourceId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);

        verify(requestActionAuthorityInfoProvider, times(1)).getRequestActionAuthorityInfo(Long.valueOf(resourceId));
        verify(authorizationRulesQueryService, times(0)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());
        verifyNoInteractions(appAuthorizationService);
    }

    @Test
    void evaluateRules_no_request_action_found() {
        AppUser user = AppUser.builder().userId("user").build();
        String resourceId = "1";
        Set<AuthorizationRuleScopePermission> authorizationRules = Set.of(
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType")
                        .handler("handler")
                        .permission(Permission.PERM_CA_USERS_EDIT).build()
        );

        when(requestActionAuthorityInfoProvider.getRequestActionAuthorityInfo(Long.valueOf(resourceId)))
                .thenThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        //invoke
        BusinessException be = assertThrows(BusinessException.class, () -> handler.evaluateRules(authorizationRules, user, resourceId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(requestActionAuthorityInfoProvider, times(1)).getRequestActionAuthorityInfo(Long.valueOf(resourceId));
        verify(authorizationRulesQueryService, times(0)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());
        verifyNoInteractions(appAuthorizationService);
    }

    @Test
    void evaluateRules_unauthorized_request_type() {
        AppUser user = AppUser.builder().userId("user").roleType(RoleTypeConstants.REGULATOR).build();
        String resourceId = "1";
        Set<AuthorizationRuleScopePermission> authorizationRules = Set.of(
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType1")
                        .handler("handler")
                        .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build(),
                AuthorizationRuleScopePermission.builder()
                        .resourceSubType("requestActionType2")
                        .handler("handler")
                        .permission(Permission.PERM_CA_USERS_EDIT).build()
        );
        RequestActionAuthorityInfoDTO requestActionInfoDTO = RequestActionAuthorityInfoDTO.builder()
                .id(Long.valueOf(resourceId))
                .type("requestActionType1")
                .requestType("TEST_REQUEST_TYPE")
                .authorityInfo(ResourceAuthorityInfo.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, "1", 
                				ResourceType.CA, ENGLAND.name(),
                				ResourceType.VERIFICATION_BODY, "1"))
                        .build())
                .build();

        when(requestActionAuthorityInfoProvider.getRequestActionAuthorityInfo(Long.valueOf(resourceId)))
                .thenReturn(requestActionInfoDTO);
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType())).thenReturn(Set.of("TEST_REQUEST_TYPE_2"));

        final BusinessException be = assertThrows(BusinessException.class,
				() -> handler.evaluateRules(authorizationRules, user, resourceId));
		
		assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(requestActionAuthorityInfoProvider, times(1)).getRequestActionAuthorityInfo(Long.valueOf(resourceId));
        verify(authorizationRulesQueryService, times(1)).findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, user.getRoleType());

        ArgumentCaptor<AuthorizationCriteria> criteriaCaptor = ArgumentCaptor.forClass(AuthorizationCriteria.class);
        verify(appAuthorizationService, times(0)).authorize(Mockito.eq(user), criteriaCaptor.capture());
    }

}