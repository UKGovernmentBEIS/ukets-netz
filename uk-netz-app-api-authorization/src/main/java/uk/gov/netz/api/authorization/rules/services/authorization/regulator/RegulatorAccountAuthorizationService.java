package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AccountAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Service
@Order(200)
@RequiredArgsConstructor
public class RegulatorAccountAuthorizationService extends AccountAuthorizationService implements RegulatorResourceTypeAuthorizationService {
    private final AccountAuthorityInfoProvider accountAuthorityInfoProvider;
    private final RegulatorCompetentAuthorityAuthorizationService regulatorCompetentAuthorityAuthorizationService;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getAccountId());
        } else {
            return isAuthorized(user, criteria.getAccountId(), criteria.getPermission());
        }
    }

    /**
     * checks that REGULATOR has access to account
     * @param user the user to authorize.
     * @param accountId the account to check permission on.
     * @return if the REGULATOR is authorized on account.
     */
    public boolean isAuthorized(AppUser user, Long accountId) {
        CompetentAuthorityEnum accountCompetentAuthority = accountAuthorityInfoProvider.getAccountCa(accountId);
        return regulatorCompetentAuthorityAuthorizationService.isAuthorized(user, accountCompetentAuthority);
    }

    /**
     * checks that REGULATOR has the permissions to account
     * @param user the user to authorize.
     * @param accountId the account to check permission on.
     * @param permission to check
     * @return if the REGULATOR has the permissions on the account
     */
    public boolean isAuthorized(AppUser user, Long accountId, String permission) {
        CompetentAuthorityEnum accountCompetentAuthority = accountAuthorityInfoProvider.getAccountCa(accountId);
        return regulatorCompetentAuthorityAuthorizationService.isAuthorized(user, accountCompetentAuthority, permission);
    }
}
