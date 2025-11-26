package uk.gov.netz.api.authorization.rules.services.authorization.thirdpartydataprovider;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

@Service
@RequiredArgsConstructor
public class ThirdPartyDataProviderRoleTypeAuthorizationService implements RoleTypeAuthorizationService {

	private final List<ThirdPartyDataProviderOnResourceTypeAuthorizationService> resourceTypeAuthorizationServices;

	@Override
	public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
		return resourceTypeAuthorizationServices.stream()
				.filter(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isApplicable(criteria))
				.findFirst()
				.map(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isAuthorized(user, criteria))
				.orElse(false);
	}

	@Override
	public String getRoleType() {
		return RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER;
	}

}
