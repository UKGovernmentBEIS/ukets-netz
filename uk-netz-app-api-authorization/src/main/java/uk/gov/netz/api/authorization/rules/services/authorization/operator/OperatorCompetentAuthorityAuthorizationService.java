package uk.gov.netz.api.authorization.rules.services.authorization.operator;

import java.util.Objects;
import java.util.Set;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Service
@Order(200)
@RequiredArgsConstructor
public class OperatorCompetentAuthorityAuthorizationService implements OperatorResourceTypeAuthorizationService {
	
	private final AccountAuthorityInfoProvider accountAuthorityInfoProvider;
	
	@Override
    public boolean isApplicable(AuthorizationCriteria criteria) {
		return criteria.getCompetentAuthority() != null;
    }

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getCompetentAuthority());
        } else {
            return isAuthorized(user, criteria.getCompetentAuthority(), criteria.getPermission());
        }
    }
    
    private boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority) {
    	final Set<CompetentAuthorityEnum> userCAs = accountAuthorityInfoProvider.findCAByIdIn(user.getAccounts());
        return userCAs.contains(competentAuthority);
    }
    
    private boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority, String permission) {
        return user.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(authority -> authority.getPermissions().contains(permission) && 
                		competentAuthority == accountAuthorityInfoProvider.getAccountCa(authority.getAccountId()));
                
    }
}
