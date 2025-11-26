package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

class ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationServiceTest {

	private final ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService cut = new ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService();

    @Test
    void isAuthorized_no_permission_true() {
    	final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
    			.authorities(List.of(AppAuthority.builder().thirdPartyDataProviderId(1L).build()))
                .build();
    	AuthorizationCriteria criteria = AuthorizationCriteria.builder()
    			.requestResources(Map.of(
    					ResourceType.THIRD_PARTY_DATA_PROVIDER, "1"
    			)).build();
    	
    	assertThat(cut.isAuthorized(user, criteria)).isTrue();
    }
    
    @Test
    void isAuthorized_no_permission_false() {
    	final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
    			.authorities(List.of(AppAuthority.builder().thirdPartyDataProviderId(1L).build()))
                .build();
    	AuthorizationCriteria criteria = AuthorizationCriteria.builder()
    			.requestResources(Map.of(
    					ResourceType.THIRD_PARTY_DATA_PROVIDER, "2"
    			)).build();
    	
    	assertThat(cut.isAuthorized(user, criteria)).isFalse();
    }
    
    @Test
    void isAuthorized_with_permission_true() {
    	final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
    			.authorities(List.of(AppAuthority.builder().thirdPartyDataProviderId(1L)
    					.permissions(List.of("perm"))
    					.build()))
                .build();
    	AuthorizationCriteria criteria = AuthorizationCriteria.builder()
    			.permission("perm")
    			.requestResources(Map.of(
    					ResourceType.THIRD_PARTY_DATA_PROVIDER, "1"
    			)).build();
    	
    	assertThat(cut.isAuthorized(user, criteria)).isTrue();
    }
    
    @Test
    void isAuthorized_with_different_permission_false() {
    	final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
    			.authorities(List.of(AppAuthority.builder().thirdPartyDataProviderId(1L)
    					.permissions(List.of("perm"))
    					.build()))
                .build();
    	AuthorizationCriteria criteria = AuthorizationCriteria.builder()
    			.permission("perm2")
    			.requestResources(Map.of(
    					ResourceType.THIRD_PARTY_DATA_PROVIDER, "1"
    			)).build();
    	
    	assertThat(cut.isAuthorized(user, criteria)).isFalse();
    }
    
    @Test
    void isAuthorized_with_same_permission_in_different_provider_false() {
    	final AppUser user = AppUser.builder().roleType(RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER)
    			.authorities(List.of(AppAuthority.builder().thirdPartyDataProviderId(2L)
    					.permissions(List.of("perm"))
    					.build()))
                .build();
    	AuthorizationCriteria criteria = AuthorizationCriteria.builder()
    			.permission("perm")
    			.requestResources(Map.of(
    					ResourceType.THIRD_PARTY_DATA_PROVIDER, "1"
    			)).build();
    	
    	assertThat(cut.isAuthorized(user, criteria)).isFalse();
    }
}
