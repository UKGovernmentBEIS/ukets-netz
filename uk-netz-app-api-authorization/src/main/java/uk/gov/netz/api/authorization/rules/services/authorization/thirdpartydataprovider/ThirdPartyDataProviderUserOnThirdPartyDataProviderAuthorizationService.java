package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import java.util.Objects;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

@Service
@Order(200)
@RequiredArgsConstructor
public class ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService
		implements ThirdPartyDataProviderOnResourceTypeAuthorizationService {
	
	@Override
	public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
		if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getThirdPartyDataProviderId());
        } else {
            return isAuthorized(user, criteria.getThirdPartyDataProviderId(), criteria.getPermission());
        }
	}

	@Override
	public boolean isApplicable(AuthorizationCriteria criteria) {
		return criteria.getThirdPartyDataProviderId() != null;
	}
	
	public boolean isAuthorized(AppUser user, Long thirdPartyDataProviderId) {
        return user.getAuthorities()
            .stream()
            .filter(Objects::nonNull)
            .anyMatch(auth -> thirdPartyDataProviderId.equals(auth.getThirdPartyDataProviderId()));
    }

	public boolean isAuthorized(AppUser user, Long thirdPartyDataProviderId, String permission) {
        return user.getAuthorities()
            .stream()
            .filter(Objects::nonNull)
            .filter(auth -> thirdPartyDataProviderId.equals(auth.getThirdPartyDataProviderId()))
            .flatMap(authority -> authority.getPermissions().stream())
            .toList().contains(permission);
    }

}
