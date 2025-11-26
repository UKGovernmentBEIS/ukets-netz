package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegulatorRoleTypeAuthorizationServiceTest {
    private final RegulatorResourceTypeAuthorizationService regulatorResourceTypeAuthorizationService = mock(RegulatorResourceTypeAuthorizationService.class);
    private final List<RegulatorResourceTypeAuthorizationService> regulatorResourceTypeAuthorizationServices = Collections.singletonList(regulatorResourceTypeAuthorizationService);
    private final RegulatorRoleTypeAuthorizationService regulatorRoleTypeAuthorizationService = new RegulatorRoleTypeAuthorizationService(regulatorResourceTypeAuthorizationServices);

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.REGULATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(regulatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(regulatorResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(true);

        Assertions.assertTrue(regulatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(regulatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(regulatorResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.REGULATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(regulatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(regulatorResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(false);

        Assertions.assertFalse(regulatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(regulatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(regulatorResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_no_applicable_resource_service() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.REGULATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria
                .builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(regulatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(false);

        Assertions.assertFalse(regulatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(regulatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
    }

    @Test
    void getRoleType() {
        Assertions.assertEquals(RoleTypeConstants.REGULATOR, regulatorRoleTypeAuthorizationService.getRoleType());
    }
}