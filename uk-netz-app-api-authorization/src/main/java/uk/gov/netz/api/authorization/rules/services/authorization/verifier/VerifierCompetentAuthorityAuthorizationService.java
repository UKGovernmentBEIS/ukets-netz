package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Objects;
import java.util.Set;

@Service
@Order(300)
@RequiredArgsConstructor
public class VerifierCompetentAuthorityAuthorizationService implements VerifierResourceTypeAuthorizationService {
	
	private final VerifierAccountAccessService verifierAccountAccessService;
	private final AccountAuthorityInfoProvider accountAuthorityInfoProvider;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getCompetentAuthority());
        } else {
            return isAuthorized(user, criteria.getCompetentAuthority(), criteria.getPermission());
        }
    }

    @Override
    public boolean isApplicable(AuthorizationCriteria criteria) {
        return criteria.getCompetentAuthority() != null;
    }

    private boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority) {
    	final Set<Long> authorizedAccountIds = verifierAccountAccessService.findAuthorizedAccountIds(user);
    	final Set<CompetentAuthorityEnum> authorizedCAs = accountAuthorityInfoProvider.findCAByIdIn(authorizedAccountIds);
    	return authorizedCAs.contains(competentAuthority);
    }

    private boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority, String permission) {
		return user.getAuthorities().stream().filter(Objects::nonNull)
				.anyMatch(authority -> authority.getPermissions().contains(permission) && accountAuthorityInfoProvider
						.findCAByIdIn(accountAuthorityInfoProvider
								.findAccountIdsByVerificationBodyId(authority.getVerificationBodyId()))
						.contains(competentAuthority));
    }
}
