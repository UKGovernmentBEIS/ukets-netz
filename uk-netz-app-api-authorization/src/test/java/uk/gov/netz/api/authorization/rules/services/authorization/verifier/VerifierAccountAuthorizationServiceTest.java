package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifierAccountAuthorizationServiceTest {
    @InjectMocks
    private VerifierAccountAuthorizationService verifierAccountAuthorizationService;

    @Mock
    private AccountAuthorityInfoProvider accountAuthorityInfoProvider;

    @Mock
    private VerifierVerificationBodyAuthorizationService verifierVerificationBodyAuthorizationService;

    @Mock
    private VerifierAccountAccessService verifierAccountAccessService;

    private final AppUser verifierUser = AppUser.builder().userId("userId").roleType(RoleTypeConstants.VERIFIER).build();

    @Test
    void isAuthorized_account_with_criteria_true() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .build();
        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody))
                .thenReturn(true);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertTrue(verifierAccountAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_true() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody, "permission1"))
                .thenReturn(true);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertTrue(verifierAccountAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_false() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .build();
        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody))
                .thenReturn(false);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_false() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                .permission("permission1")
                .build();
        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody, "permission1"))
                .thenReturn(false);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_true() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody))
                .thenReturn(true);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertTrue(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId));
    }

    @Test
    void isAuthorized_account_false() {
        Long accountId = 1L;
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId));
    }

    @Test
    void isAuthorized_account_no_verification_body() {
        Long accountId = 1L;

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.empty());
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId));
        verifyNoInteractions(verifierVerificationBodyAuthorizationService);
    }

    @Test
    void isAuthorized_account_with_permissions_true() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        String permission = "permission1";

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody, permission))
                .thenReturn(true);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertTrue(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId, permission));
    }

    @Test
    void isAuthorized_account_with_permissions_false() {
        Long accountId = 1L;
        Long accountVerificationBody = 1L;
        String permission = "permission1";

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody, permission))
                .thenReturn(false);
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId, permission));
    }

    @Test
    void isAuthorized_account_no_verification_body_with_permissions() {
        Long accountId = 1L;
        String permission = "permission1";

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.empty());
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L, 2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId, permission));
        verifyNoInteractions(verifierVerificationBodyAuthorizationService);
    }

    @Test
    void isAuthorized_account_when_not_admin_and_task_exists() {

        Long accountId = 1L;
        Long accountVerificationBody = 2L;
        String permission = "permission1";

        when(accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId)).thenReturn(Optional.of(accountVerificationBody));
        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(1L));
        when(verifierVerificationBodyAuthorizationService.isAuthorized(verifierUser, accountVerificationBody, permission))
                .thenReturn(true);

        assertTrue(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId, permission));
    }

    @Test
    void isAuthorized_account_when_not_admin_and_task_not_exists() {

        Long accountId = 1L;
        String permission = "permission1";

        when(verifierAccountAccessService.findAuthorizedAccountIds(verifierUser)).thenReturn(Set.of(2L));

        assertFalse(verifierAccountAuthorizationService.isAuthorized(verifierUser, accountId, permission));
        verifyNoInteractions(verifierVerificationBodyAuthorizationService);
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();
        assertTrue(verifierAccountAuthorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        assertFalse(verifierAccountAuthorizationService.isApplicable(authorizationCriteria));
    }
}