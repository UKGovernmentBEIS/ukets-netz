package uk.gov.netz.api.authorization.rules.services.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaRequestCreateRuleHandlerTest {

	@InjectMocks
    private CaRequestCreateRuleHandler cut;
	
	@Mock
    private AppAuthorizationService appAuthorizationService;
    
    @Mock
    private AuthorizationRulesQueryService authorizationRulesQueryService;
    
    private final AppUser USER = AppUser.builder().roleType(RoleTypeConstants.REGULATOR)
    		.authorities(List.of(AppAuthority.builder().competentAuthority(CompetentAuthorityEnum.ENGLAND).build()))
    		.build();
    
    @Test
    void evaluateRules_empty_rules() {
        String resourceId = CompetentAuthorityEnum.ENGLAND.name();
        
        BusinessException be = assertThrows(BusinessException.class, () -> cut.evaluateRules(Set.of(), USER,
                resourceId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verifyNoInteractions(appAuthorizationService);
    }
    
    @Test
    void evaluateRules() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .resourceSubType("requestType")
            .build();
        AuthorizationRuleScopePermission authorizationRulePermissionScope2 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_CA_USERS_EDIT)
            .resourceSubType("requestType")
            .build();
        
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("requestType"));

        
        cut.evaluateRules(Set.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2), USER, CompetentAuthorityEnum.ENGLAND.name());

        verify(appAuthorizationService, times(1)).authorize(USER, AuthorizationCriteria.builder()
        		.permission(Permission.PERM_ACCOUNT_USERS_EDIT)
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
        		.build());
        verify(appAuthorizationService, times(1)).authorize(USER, AuthorizationCriteria.builder()
        		.permission(Permission.PERM_CA_USERS_EDIT)
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
        		.build());
        verifyNoMoreInteractions(appAuthorizationService);
    }
    
    @Test
    void evaluateRules_unauthorised_request_type() {
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 = 
                AuthorizationRuleScopePermission.builder()
            .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
            .resourceSubType("requestType")
            .build();
        
        when(authorizationRulesQueryService.findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST, USER.getRoleType())).thenReturn(Set.of("requestType2"));
        
        final BusinessException be = assertThrows(BusinessException.class,
				() -> cut.evaluateRules(Set.of(authorizationRulePermissionScope1), USER, CompetentAuthorityEnum.ENGLAND.name()));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(appAuthorizationService, times(0)).authorize(USER, AuthorizationCriteria.builder()
        		.permission(Permission.PERM_ACCOUNT_USERS_EDIT)
        		.requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
        		.build());
        verifyNoMoreInteractions(appAuthorizationService);
    }
}
