package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import static org.mockito.Mockito.when;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RegulatorAccountAuthorizationServiceTest {

    @InjectMocks
    private RegulatorAccountAuthorizationService regulatorAccountAuthorizationService;

    @Mock
    private AccountAuthorityInfoProvider accountAuthorityInfoProvider;

    @Mock
    private RegulatorCompetentAuthorityAuthorizationService regulatorCompAuthAuthorizationService;

    private final AppUser user = AppUser.builder().roleType(RoleTypeConstants.REGULATOR).build();

    @Test
    void isAuthorized_account_with_criteria_true() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority)).thenReturn(true);

        Assertions.assertTrue(regulatorAccountAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_true() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority, "permission1")).thenReturn(true);

        Assertions.assertTrue(regulatorAccountAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_false() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(CompetentAuthorityEnum.ENGLAND);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority)).thenReturn(false);

        Assertions.assertFalse(regulatorAccountAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_false() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(CompetentAuthorityEnum.ENGLAND);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority, "permission1")).thenReturn(false);

        Assertions.assertFalse(regulatorAccountAuthorizationService.isAuthorized(user, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_true() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority)).thenReturn(true);

        Assertions.assertTrue(regulatorAccountAuthorizationService.isAuthorized(user, accountId));
    }

    @Test
    void isAuthorized_account_false() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority)).thenReturn(false);

        Assertions.assertFalse(regulatorAccountAuthorizationService.isAuthorized(user, accountId));
    }

    @Test
    void isAuthorized_account_with_permissions_true() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String permission = "permission1";
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority, permission)).thenReturn(true);

        Assertions.assertTrue(regulatorAccountAuthorizationService.isAuthorized(user, accountId, permission));
    }

    @Test
    void isAuthorized_account_with_permissions_false() {
        Long accountId = 1L;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        String permission = "permission1";
        when(accountAuthorityInfoProvider.getAccountCa(accountId)).thenReturn(competentAuthority);
        when(regulatorCompAuthAuthorizationService.isAuthorized(user, competentAuthority, permission)).thenReturn(false);

        Assertions.assertFalse(regulatorAccountAuthorizationService.isAuthorized(user, accountId, permission));
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();
        Assertions.assertTrue(regulatorAccountAuthorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        Assertions.assertFalse(regulatorAccountAuthorizationService.isApplicable(authorizationCriteria));
    }
}