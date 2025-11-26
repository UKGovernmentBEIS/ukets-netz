package uk.gov.netz.api.authorization.rules.services.authorization.operator;

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

class OperatorRoleTypeAuthorizationServiceTest {
    private final OperatorResourceTypeAuthorizationService operatorResourceTypeAuthorizationService = mock(OperatorResourceTypeAuthorizationService.class);
    private final List<OperatorResourceTypeAuthorizationService> operatorResourceTypeAuthorizationServices = Collections.singletonList(operatorResourceTypeAuthorizationService);
    private final OperatorRoleTypeAuthorizationService operatorRoleTypeAuthorizationService = new OperatorRoleTypeAuthorizationService(operatorResourceTypeAuthorizationServices);

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(operatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(operatorResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(true);

        assertTrue(operatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(operatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(operatorResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(operatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(true);
        when(operatorResourceTypeAuthorizationService.isAuthorized(user, criteria)).thenReturn(false);

        Assertions.assertFalse(operatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(operatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
        verify(operatorResourceTypeAuthorizationService, times(1)).isAuthorized(user, criteria);
    }

    @Test
    void isAuthorized_no_applicable_resource_service() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(operatorResourceTypeAuthorizationService.isApplicable(criteria)).thenReturn(false);

        Assertions.assertFalse(operatorRoleTypeAuthorizationService.isAuthorized(user, criteria));

        verify(operatorResourceTypeAuthorizationService, times(1)).isApplicable(criteria);
    }

    @Test
    void getRoleType() {
        Assertions.assertEquals(RoleTypeConstants.OPERATOR, operatorRoleTypeAuthorizationService.getRoleType());
    }
}