package uk.gov.netz.api.authorization.rules.services.authorization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleTypeAuthorizationServiceDelegatorTest {
    private final RoleTypeAuthorizationService roleTypeAuthorizationService = mock(RoleTypeAuthorizationService.class);
    private final List<RoleTypeAuthorizationService> roleTypeAuthorizationServices = Collections.singletonList(roleTypeAuthorizationService);
    private final RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator = new RoleTypeAuthorizationServiceDelegator(roleTypeAuthorizationServices);

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(roleTypeAuthorizationService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(roleTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(true);

        assertTrue(roleTypeAuthorizationServiceDelegator.isAuthorized(user, criteria));

        verify(roleTypeAuthorizationService, times(1)).getRoleType();
        verify(roleTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(roleTypeAuthorizationService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(roleTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(false);

        Assertions.assertFalse(roleTypeAuthorizationServiceDelegator.isAuthorized(user, criteria));

        verify(roleTypeAuthorizationService, times(1)).getRoleType();
        verify(roleTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_no_resource_service_found() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(roleTypeAuthorizationService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);

        Assertions.assertFalse(roleTypeAuthorizationServiceDelegator.isAuthorized(user, criteria));

        verify(roleTypeAuthorizationService, times(1)).getRoleType();
    }
}