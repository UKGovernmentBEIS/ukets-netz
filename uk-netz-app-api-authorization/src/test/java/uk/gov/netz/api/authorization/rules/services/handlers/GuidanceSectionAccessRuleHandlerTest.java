package uk.gov.netz.api.authorization.rules.services.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.GuidanceSectionAuthorityInfoProvider;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuidanceSectionAccessRuleHandlerTest {

    @InjectMocks
    private GuidanceSectionAccessRuleHandler handler;

    @Mock
    private GuidanceSectionAuthorityInfoProvider guidanceSectionAuthorityInfoProvider;

    @Mock
    private AppAuthorizationService appAuthorizationService;

    @Test
    void evaluateRules() {
        Long sectionId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.WALES;
        AppUser appUser = AppUser.builder().
                roleType(RoleTypeConstants.REGULATOR)
                .build();
        AuthorizationRuleScopePermission authorizationRule1 = AuthorizationRuleScopePermission.builder().build();
        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .build();

        when(guidanceSectionAuthorityInfoProvider.getGuidanceSectionCaById(sectionId)).thenReturn(competentAuthority);

        handler.evaluateRules(rules, appUser, String.valueOf(sectionId));

        verify(guidanceSectionAuthorityInfoProvider).getGuidanceSectionCaById(sectionId);
        verify(appAuthorizationService).authorize(appUser, authorizationCriteria);
        verifyNoMoreInteractions(guidanceSectionAuthorityInfoProvider, appAuthorizationService);
    }

    @Test
    void evaluateRules_resource_forbidden() {
        Long sectionId = 1L;
        CompetentAuthorityEnum resourceCompetentAuthority = CompetentAuthorityEnum.ENGLAND;
        AppUser appUser = AppUser.builder().
                roleType(RoleTypeConstants.OPERATOR)
                .build();
        AuthorizationRuleScopePermission authorizationRule1 = AuthorizationRuleScopePermission.builder().build();
        Set<AuthorizationRuleScopePermission> rules = Set.of(authorizationRule1);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, resourceCompetentAuthority.name()))
                .build();

        when(guidanceSectionAuthorityInfoProvider.getGuidanceSectionCaById(sectionId)).thenReturn(resourceCompetentAuthority);
        doThrow(new BusinessException(ErrorCode.FORBIDDEN)).when(appAuthorizationService).authorize(appUser, authorizationCriteria);

        BusinessException be = assertThrows(BusinessException.class, () ->
                handler.evaluateRules(rules, appUser, String.valueOf(sectionId)));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
        verify(guidanceSectionAuthorityInfoProvider).getGuidanceSectionCaById(sectionId);
        verify(appAuthorizationService).authorize(appUser, authorizationCriteria);
        verifyNoMoreInteractions(guidanceSectionAuthorityInfoProvider, appAuthorizationService);
    }
}
