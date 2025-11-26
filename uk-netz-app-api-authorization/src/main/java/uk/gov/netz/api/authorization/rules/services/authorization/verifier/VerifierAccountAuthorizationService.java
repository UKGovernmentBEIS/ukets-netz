package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AccountAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Optional;

/**
 * Service that checks if a VERIFIER user is authorized on an account.
 */
@Service
@Order(200)
@RequiredArgsConstructor
public class VerifierAccountAuthorizationService extends AccountAuthorizationService implements VerifierResourceTypeAuthorizationService {

    private final AccountAuthorityInfoProvider accountAuthorityInfoProvider;
    private final VerifierVerificationBodyAuthorizationService verifierVerificationBodyAuthorizationService;
    private final VerifierAccountAccessService verifierAccountAccessService;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getAccountId());
        } else {
            return isAuthorized(user, criteria.getAccountId(), criteria.getPermission());
        }
    }

    /**
     * Checks that VERIFIER has access to account.
     * @param user the user to authorize
     * @param accountId the account to check permission on
     * @return if the VERIFIER is authorized on account.
     */
    public boolean isAuthorized(AppUser user, Long accountId) {
        final boolean authorized = this.checkAuthorizedAccount(user, accountId);
        if (!authorized) {
            return false;
        }

        Optional<Long> accountVerificationBodyOptional = accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId);
        return accountVerificationBodyOptional
            .map(accountVerificationBody -> verifierVerificationBodyAuthorizationService.isAuthorized(user, accountVerificationBody))
            .orElse(false);
    }

    /**
     * Checks that VERIFIER has the permissions to account.
     * @param user the user to authorize
     * @param accountId the account to check permission on
     * @param permission to check
     * @return if the VERIFIER has the permissions on the account
     */
    public boolean isAuthorized(AppUser user, Long accountId, String permission) {
        final boolean authorized = this.checkAuthorizedAccount(user, accountId);
        if (!authorized) {
            return false;
        }

        Optional<Long> accountVerificationBodyOptional = accountAuthorityInfoProvider.getAccountVerificationBodyId(accountId);
        return accountVerificationBodyOptional
            .map(accountVerificationBody -> verifierVerificationBodyAuthorizationService.isAuthorized(user, accountVerificationBody, permission))
            .orElse(false);
    }

    private boolean checkAuthorizedAccount(final AppUser user, final Long accountId) {
        return verifierAccountAccessService.findAuthorizedAccountIds(user).contains(accountId);
    }
}
