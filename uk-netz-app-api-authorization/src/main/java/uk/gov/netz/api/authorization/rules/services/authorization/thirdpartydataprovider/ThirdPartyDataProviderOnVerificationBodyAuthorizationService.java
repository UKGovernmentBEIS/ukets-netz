package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.VerificationBodyAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

@Service
@Order(200)
@RequiredArgsConstructor
public class ThirdPartyDataProviderOnVerificationBodyAuthorizationService
	implements ThirdPartyDataProviderOnResourceTypeAuthorizationService {
	
	private final VerificationBodyAuthorityInfoProvider verificationBodyAuthorityInfoProvider;
	private final ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService thirdPartyProviderUserOnThirdPartyProviderAuthorizationService;

	@Override
	public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
		if (criteria.getPermission() == null) {
			return isAuthorized(user, criteria.getVerificationBodyId());
		} else {
			return isAuthorized(user, criteria.getVerificationBodyId(), criteria.getPermission());
		}
	}

	@Override
	public boolean isApplicable(AuthorizationCriteria criteria) {
		return ObjectUtils.isNotEmpty(criteria.getVerificationBodyId());
	}

	public boolean isAuthorized(AppUser user, Long verificationBodyId) {
		return verificationBodyAuthorityInfoProvider.getThirdPartyDataProviderId(verificationBodyId)
				.map(verificationBodyThirdPartyDataProviderId -> thirdPartyProviderUserOnThirdPartyProviderAuthorizationService
						.isAuthorized(user, verificationBodyThirdPartyDataProviderId))
				.orElse(false);
	}
	
	public boolean isAuthorized(AppUser user, Long verificationBodyId, String permission) {
		return verificationBodyAuthorityInfoProvider.getThirdPartyDataProviderId(verificationBodyId)
				.map(verificationBodyThirdPartyDataProviderId -> thirdPartyProviderUserOnThirdPartyProviderAuthorizationService
						.isAuthorized(user, verificationBodyThirdPartyDataProviderId, permission))
				.orElse(false);
	}

}
