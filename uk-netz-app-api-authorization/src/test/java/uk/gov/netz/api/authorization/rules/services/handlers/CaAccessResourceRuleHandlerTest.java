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
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CaAccessResourceRuleHandlerTest {

	@InjectMocks
    private CaAccessResourceRuleHandler cut;
	
	@Mock
    private AppAuthorizationService appAuthorizationService;
    
    private final AppUser USER = AppUser.builder().roleType(RoleTypeConstants.REGULATOR)
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
    	String resourceId = CompetentAuthorityEnum.ENGLAND.name();
    	
        AuthorizationRuleScopePermission authorizationRulePermissionScope1 = 
				AuthorizationRuleScopePermission.builder().handler("handler1")
            .build();
		AuthorizationRuleScopePermission authorizationRulePermissionScope2 = AuthorizationRuleScopePermission.builder()
				.handler("handler1").permission(Permission.PERM_CA_USERS_EDIT).build();

        cut.evaluateRules(Set.of(authorizationRulePermissionScope1, authorizationRulePermissionScope2),
            USER, resourceId);

        verify(appAuthorizationService, times(1)).authorize(USER, AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.CA, resourceId))
        		.build());
        verify(appAuthorizationService, times(1)).authorize(USER, AuthorizationCriteria.builder()
        		.permission(Permission.PERM_CA_USERS_EDIT)
        		.requestResources(Map.of(ResourceType.CA, resourceId))
        		.build());
        verifyNoMoreInteractions(appAuthorizationService);
    }
}
