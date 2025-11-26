package uk.gov.netz.api.authorization.rules.services.authorization.operator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorCompetentAuthorityAuthorizationServiceTest {

    @InjectMocks
    private OperatorCompetentAuthorityAuthorizationService operatorCompetentAuthorityAuthorizationService;

    @Mock
    private AccountAuthorityInfoProvider accountAuthorityInfoProvider;

    @Test
    void isAuthorized_ca_with_criteria_true() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR)
                .authorities(List.of(AppAuthority.builder().accountId(1L).build(),
                        AppAuthority.builder().accountId(2L).build()))
                .build();
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .build();
        when(accountAuthorityInfoProvider.findCAByIdIn(Set.of(1L, 2L))).thenReturn(Set.of(CompetentAuthorityEnum.ENGLAND, CompetentAuthorityEnum.SCOTLAND));
        Assertions.assertTrue(operatorCompetentAuthorityAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_criteria_false() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR)
                .authorities(List.of(AppAuthority.builder().accountId(1L).build(),
                        AppAuthority.builder().accountId(2L).build()))
                .build();
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .build();
        when(accountAuthorityInfoProvider.findCAByIdIn(Set.of(1L, 2L))).thenReturn(Set.of(CompetentAuthorityEnum.SCOTLAND, CompetentAuthorityEnum.WALES));

        Assertions.assertFalse(operatorCompetentAuthorityAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_criteria_with_permission_true() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR)
                .authorities(List.of(AppAuthority.builder().accountId(1L).permissions(List.of("permission2")).build(),
                        AppAuthority.builder().accountId(2L).permissions(List.of("permission1")).build()))
                .build();
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(2L)).thenReturn(CompetentAuthorityEnum.ENGLAND);

        Assertions.assertTrue(operatorCompetentAuthorityAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_criteria_with_permission_false() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR)
                .authorities(List.of(AppAuthority.builder().accountId(1L).permissions(List.of("permission2")).build(),
                        AppAuthority.builder().accountId(2L).permissions(List.of("permission1")).build()))
                .build();
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(2L)).thenReturn(CompetentAuthorityEnum.SCOTLAND);

        Assertions.assertFalse(operatorCompetentAuthorityAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();
        assertTrue(operatorCompetentAuthorityAuthorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        assertFalse(operatorCompetentAuthorityAuthorizationService.isApplicable(authorizationCriteria));
    }
}
