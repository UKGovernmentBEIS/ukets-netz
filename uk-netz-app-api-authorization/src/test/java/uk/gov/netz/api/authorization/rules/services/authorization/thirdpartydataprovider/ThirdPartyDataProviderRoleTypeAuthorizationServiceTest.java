package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

class ThirdPartyDataProviderRoleTypeAuthorizationServiceTest {

	private final ThirdPartyDataProviderOnResourceTypeAuthorizationService serviceMock = mock(ThirdPartyDataProviderOnResourceTypeAuthorizationService.class);
    private final List<ThirdPartyDataProviderOnResourceTypeAuthorizationService> resourceTypeAuthorizationServices = Collections.singletonList(serviceMock);
    private final ThirdPartyDataProviderRoleTypeAuthorizationService cut = new ThirdPartyDataProviderRoleTypeAuthorizationService(resourceTypeAuthorizationServices);

    @Test
    void isAuthorized_true() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(serviceMock.isApplicable(criteria)).thenReturn(true);
        when(serviceMock.isAuthorized(user, criteria)).thenReturn(true);

        assertTrue(cut.isAuthorized(user, criteria));

        verify(serviceMock, times(1)).isApplicable(criteria);
        verify(serviceMock, times(1)).isAuthorized(user, criteria);
    }
    
    @Test
    void isAuthorized_false() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(serviceMock.isApplicable(criteria)).thenReturn(true);
        when(serviceMock.isAuthorized(user, criteria)).thenReturn(false);

        assertFalse(cut.isAuthorized(user, criteria));

        verify(serviceMock, times(1)).isApplicable(criteria);
        verify(serviceMock, times(1)).isAuthorized(user, criteria);
    }
    
    @Test
    void isAuthorized_not_applicable() {
        AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER).build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
                .requestResources(Map.of(ResourceType.ACCOUNT, "1"))
                .build();

        when(serviceMock.isApplicable(criteria)).thenReturn(false);

        assertFalse(cut.isAuthorized(user, criteria));

        verify(serviceMock, times(1)).isApplicable(criteria);
        verifyNoMoreInteractions(serviceMock);
    }
    
    @Test
    void getRoleType() {
    	assertThat(cut.getRoleType()).isEqualTo(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER);
    }
    
}
