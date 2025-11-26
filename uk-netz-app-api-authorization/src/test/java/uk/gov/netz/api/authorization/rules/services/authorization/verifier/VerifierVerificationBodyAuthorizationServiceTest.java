package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerifierVerificationBodyAuthorizationServiceTest {
    private VerifierVerificationBodyAuthorizationService authorizationService = new VerifierVerificationBodyAuthorizationService();
    private final AppAuthority appAuthority = AppAuthority.builder()
            .verificationBodyId(1L)
            .permissions(List.of("permission1", "permission2"))
            .build();
    private final AppUser verifierUser = AppUser.builder().authorities(List.of(appAuthority)).roleType(RoleTypeConstants.VERIFIER).build();

    @Test
    void isAuthorized_account_with_criteria_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, "1"))
                .build();
        Assertions.assertTrue(authorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, "1"))
                .permission("permission1")
                .build();
        Assertions.assertTrue(authorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, "2"))
                .build();
        Assertions.assertFalse(authorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_with_criteria_with_permission_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, "1"))
                .permission("permission3")
                .build();
        Assertions.assertFalse(authorizationService.isAuthorized(verifierUser, authorizationCriteria));
    }

    @Test
    void isAuthorized_account_true() {
        assertTrue(authorizationService.isAuthorized(verifierUser, 1L));
    }

    @Test
    void isAuthorized_account_false() {
        assertFalse(authorizationService.isAuthorized(verifierUser, 2L));
    }

    @Test
    void isAuthorized_account_with_permissions_true() {
        assertTrue(authorizationService.isAuthorized(verifierUser, 1L, "permission1"));
    }

    @Test
    void isAuthorized_account_with_permissions_false() {
        assertFalse(authorizationService.isAuthorized(verifierUser, 1L, "permission3"));
    }

    @Test
    void isApplicable_true() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
        		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, "1"))
                .build();
        assertTrue(authorizationService.isApplicable(authorizationCriteria));
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                .build();
        assertFalse(authorizationService.isApplicable(authorizationCriteria));
    }
}