package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.VerificationBodyAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDataProviderOnVerificationBodyAuthorizationServiceTest {

    @InjectMocks
    private ThirdPartyDataProviderOnVerificationBodyAuthorizationService cut;

    @Mock
    private VerificationBodyAuthorityInfoProvider verificationBodyAuthorityInfoProvider;

    @Mock
    private ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService thirdPartyProviderUserOnThirdPartyProviderAuthorizationService;

    @Test
    void isAuthorized_no_criteria() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
            .build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder().requestResources(Map.of(
            ResourceType.VERIFICATION_BODY, "1"
        )).build();

        when(verificationBodyAuthorityInfoProvider.getThirdPartyDataProviderId(1L)).thenReturn(Optional.of(2L));
        when(thirdPartyProviderUserOnThirdPartyProviderAuthorizationService.isAuthorized(user, 2L)).thenReturn(true);

        boolean result = cut.isAuthorized(user, criteria);

        assertThat(result).isTrue();

        verify(verificationBodyAuthorityInfoProvider, times(1)).getThirdPartyDataProviderId(1L);
        verify(thirdPartyProviderUserOnThirdPartyProviderAuthorizationService, times(1)).isAuthorized(user, 2L);
    }

    @Test
    void isAuthorized_with_criteria() {
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
            .build();
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
            .permission("perm")
            .requestResources(Map.of(
                ResourceType.VERIFICATION_BODY, "1"
            )).build();

        when(verificationBodyAuthorityInfoProvider.getThirdPartyDataProviderId(1L)).thenReturn(Optional.of(2L));
        when(thirdPartyProviderUserOnThirdPartyProviderAuthorizationService.isAuthorized(user, 2L, "perm")).thenReturn(true);

        boolean result = cut.isAuthorized(user, criteria);

        assertThat(result).isTrue();

        verify(verificationBodyAuthorityInfoProvider, times(1)).getThirdPartyDataProviderId(1L);
        verify(thirdPartyProviderUserOnThirdPartyProviderAuthorizationService, times(1)).isAuthorized(user, 2L, "perm");
    }

    @Test
    void isApplicable() {
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
            .requestResources(Map.of(ResourceType.VERIFICATION_BODY, "1"))
            .build();
        boolean result = cut.isApplicable(criteria);

        assertThat(result).isTrue();

        verifyNoInteractions(verificationBodyAuthorityInfoProvider,  thirdPartyProviderUserOnThirdPartyProviderAuthorizationService);
    }

    @Test
    void isApplicable_false() {
        AuthorizationCriteria criteria = AuthorizationCriteria.builder()
            .build();
        boolean result = cut.isApplicable(criteria);

        assertThat(result).isFalse();
        verifyNoInteractions(verificationBodyAuthorityInfoProvider,  thirdPartyProviderUserOnThirdPartyProviderAuthorizationService);
    }
}
