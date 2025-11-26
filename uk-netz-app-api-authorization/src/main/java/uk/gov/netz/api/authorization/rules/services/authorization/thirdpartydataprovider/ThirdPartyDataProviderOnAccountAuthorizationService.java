package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AccountAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

@Service
@Order(200)
@RequiredArgsConstructor
public class ThirdPartyDataProviderOnAccountAuthorizationService extends AccountAuthorizationService
		implements ThirdPartyDataProviderOnResourceTypeAuthorizationService {
	
	private final AccountAuthorityInfoProvider accountAuthorityInfoProvider;
	private final ThirdPartyDataProviderUserOnThirdPartyDataProviderAuthorizationService thirdPartyProviderUserOnThirdPartyProviderAuthorizationService;

	@Override
	public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
		if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getAccountId());
        } else {
        	return isAuthorized(user, criteria.getAccountId(), criteria.getPermission());
        }
	}
	
	public boolean isAuthorized(AppUser user, Long accountId) {
		return accountAuthorityInfoProvider.getThirdPartyDataProviderId(accountId)
				.map(accountThirdPartyDataProviderId -> thirdPartyProviderUserOnThirdPartyProviderAuthorizationService
						.isAuthorized(user, accountThirdPartyDataProviderId))
				.orElse(false);
	}
	
	public boolean isAuthorized(AppUser user, Long accountId, String permission) {
		return accountAuthorityInfoProvider.getThirdPartyDataProviderId(accountId)
				.map(accountThirdPartyDataProvider -> thirdPartyProviderUserOnThirdPartyProviderAuthorizationService
						.isAuthorized(user, accountThirdPartyDataProvider, permission))
				.orElse(false);
	}

}
