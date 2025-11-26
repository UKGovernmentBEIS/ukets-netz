package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

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
class VerifierCompetentAuthorityAuthorizationServiceTest {

    @InjectMocks
    private VerifierCompetentAuthorityAuthorizationService verifierCompetentAuthorityAuthorizationService;

    @Mock
    private VerifierAccountAccessService verifierAccountAccessService;

    @Mock
    private AccountAuthorityInfoProvider accountAuthorityInfoProvider;


    @Test
    void isAuthorized_ca_with_criteria_true() {
        final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER).build();
        final Set<Long> accountIds = Set.of(1L, 2L);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();

        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(accountIds);
        when(accountAuthorityInfoProvider.findCAByIdIn(accountIds)).thenReturn(Set.of(CompetentAuthorityEnum.ENGLAND, CompetentAuthorityEnum.WALES));

        assertTrue(verifierCompetentAuthorityAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_criteria_false() {
        final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER).build();
        final Set<Long> accountIds = Set.of(1L, 2L);
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();

        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(accountIds);
        when(accountAuthorityInfoProvider.findCAByIdIn(accountIds)).thenReturn(Set.of(CompetentAuthorityEnum.SCOTLAND, CompetentAuthorityEnum.WALES));

        assertFalse(verifierCompetentAuthorityAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_permissions_true() {
        Long verificationBodyId = 1L;
        String permission = "permission1";
        final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(verificationBodyId).permissions(List.of(permission)).build(),
                        AppAuthority.builder().verificationBodyId(2L).permissions(List.of("permission2")).build()))
                .build();
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .permission(permission)
                .build();

        when(accountAuthorityInfoProvider.findAccountIdsByVerificationBodyId(verificationBodyId)).thenReturn(Set.of(2L, 3L));
        when(accountAuthorityInfoProvider.findCAByIdIn(Set.of(2L, 3L))).thenReturn(Set.of(CompetentAuthorityEnum.ENGLAND, CompetentAuthorityEnum.WALES));

        assertTrue(verifierCompetentAuthorityAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_permissions_false() {
        Long verificationBodyId = 1L;
        String permission = "permission1";
        final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(verificationBodyId).permissions(List.of(permission)).build(),
                        AppAuthority.builder().verificationBodyId(2L).permissions(List.of("permission2")).build()))
                .build();
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .permission("permission2")
                .build();

        when(accountAuthorityInfoProvider.findAccountIdsByVerificationBodyId(2L)).thenReturn(Set.of(2L, 3L));
        when(accountAuthorityInfoProvider.findCAByIdIn(Set.of(2L, 3L))).thenReturn(Set.of(CompetentAuthorityEnum.SCOTLAND, CompetentAuthorityEnum.WALES));

        assertFalse(verifierCompetentAuthorityAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_ca_with_permissions_no_matching_permission_false() {
        Long verificationBodyId = 1L;
        String permission = "permission1";
        final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(verificationBodyId).permissions(List.of(permission)).build(),
                        AppAuthority.builder().verificationBodyId(2L).permissions(List.of("permission2")).build()))
                .build();
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .permission("permission3")
                .build();

        assertFalse(verifierCompetentAuthorityAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.CA, CompetentAuthorityEnum.ENGLAND.name()))
                .build();
        assertTrue(verifierCompetentAuthorityAuthorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        assertFalse(verifierCompetentAuthorityAuthorizationService.isApplicable(authorizationCriteria));
    }
}
