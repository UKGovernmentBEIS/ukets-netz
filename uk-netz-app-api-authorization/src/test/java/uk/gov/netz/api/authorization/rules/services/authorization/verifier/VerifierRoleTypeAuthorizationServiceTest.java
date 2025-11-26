package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifierRoleTypeAuthorizationServiceTest {
    private final VerifierResourceTypeAuthorizationService verifierResourceTypeAuthorizationService = mock(VerifierResourceTypeAuthorizationService.class);
    private final List<VerifierResourceTypeAuthorizationService> verifierResourceTypeAuthorizationServices = Collections.singletonList(verifierResourceTypeAuthorizationService);
    private final VerifierRoleTypeAuthorizationService verifierRoleTypeAuthorizationService = new VerifierRoleTypeAuthorizationService(verifierResourceTypeAuthorizationServices);

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(verifierResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(verifierResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(true);

        assertTrue(verifierRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(verifierResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(verifierResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(verifierResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(verifierResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(false);

        Assertions.assertFalse(verifierRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(verifierResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(verifierResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_no_applicable_resource_service() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(verifierResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(false);

        Assertions.assertFalse(verifierRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(verifierResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
    }

    @Test
    void getRoleType() {
        Assertions.assertEquals(RoleTypeConstants.VERIFIER, verifierRoleTypeAuthorizationService.getRoleType());
    }
}