package uk.gov.netz.api.authorization.rules.services.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.NotificationTemplateAuthorityInfoProvider;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationTemplateAccessRuleHandlerTest {

    @InjectMocks
    private NotificationTemplateAccessRuleHandler handler;

    @Mock
    private NotificationTemplateAuthorityInfoProvider templateAuthorityInfoProvider;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    @Test
    void evaluateRules() {
        Long notificationTemplateId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.WALES;
        AppUser appUser = AppUser.builder().
            roleType(RoleTypeConstants.REGULATOR)
            .build();
        AuthorizationRuleScopePermission authorizationRule1 = AuthorizationRuleScopePermission.builder().build();
        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        	.requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
            .build();

        when(templateAuthorityInfoProvider.getNotificationTemplateCaById(notificationTemplateId)).thenReturn(competentAuthority);

        handler.evaluateRules(rules, appUser, String.valueOf(notificationTemplateId));

        verify(templateAuthorityInfoProvider, times(1)).getNotificationTemplateCaById(notificationTemplateId);
        verify(appAuthorizationService, times(1)).authorize(appUser, authorizationCriteria);
    }

    @Test
    void evaluateRules_resource_forbidden() {
        Long notificationTemplateId = 1L;
        CompetentAuthorityEnum resourceCompetentAuthority = CompetentAuthorityEnum.ENGLAND;
        AppUser appUser = AppUser.builder().
            roleType(RoleTypeConstants.OPERATOR)
            .build();
        AuthorizationRuleScopePermission authorizationRule1 = AuthorizationRuleScopePermission.builder().build();
        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        	.requestResources(Map.of(ResourceType.CA, resourceCompetentAuthority.name()))
            .build();

        when(templateAuthorityInfoProvider.getNotificationTemplateCaById(notificationTemplateId)).thenReturn(resourceCompetentAuthority);
        doThrow(new BusinessException(ErrorCode.FORBIDDEN)).when(appAuthorizationService).authorize(appUser, authorizationCriteria);

        BusinessException be = assertThrows(BusinessException.class, () ->
            handler.evaluateRules(rules, appUser, String.valueOf(notificationTemplateId)));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(templateAuthorityInfoProvider, times(1)).getNotificationTemplateCaById(notificationTemplateId);
        verify(appAuthorizationService, times(1)).authorize(appUser, authorizationCriteria);
    }
}