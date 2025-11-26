package uk.gov.netz.api.authorization.rules.services.authorization.operator;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AccountAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Objects;

/**
 * Service that checks if an OPERATOR user is authorized on an account
 */
@Service
@Order(100)
public class OperatorAccountAuthorizationService extends AccountAuthorizationService implements OperatorResourceTypeAuthorizationService {

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getAccountId());
        } else {
            return isAuthorized(user, criteria.getAccountId(), criteria.getPermission());
        }
    }

    /**
     * checks that OPERATOR has access to account
     * @param user the user to authorize.
     * @param accountId the account to check permission on.
     * @return if the OPERATOR is authorized on account.
     */
    public boolean isAuthorized(AppUser user, Long accountId) {
        return user.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(auth -> accountId.equals(auth.getAccountId()));
    }

    /**
     * checks that OPERATOR has the permissions to account
     * @param user the user to authorize.
     * @param accountId the account to check permission on.
     * @param permission to check
     * @return if the OPERATOR has the permissions on the account
     */
    public boolean isAuthorized(AppUser user, Long accountId, String permission) {
        return user.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .filter(auth -> accountId.equals(auth.getAccountId()))
                .flatMap(authority -> authority.getPermissions().stream())
                .toList().contains(permission);
    }
}
