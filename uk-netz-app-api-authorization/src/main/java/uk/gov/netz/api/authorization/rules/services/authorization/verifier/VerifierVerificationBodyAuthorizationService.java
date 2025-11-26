package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Objects;

@Service
public class VerifierVerificationBodyAuthorizationService implements VerifierResourceTypeAuthorizationService {

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

    /**
     * Checks that VERIFIER has access to the provided verification body.
     * @param user the user to authorize
     * @param verificationBodyId the verification body to check permission on
     * @return if the user is authorized on verification body.
     */
    public boolean isAuthorized(AppUser user, Long verificationBodyId) {
        return user.getAuthorities()
            .stream()
            .filter(Objects::nonNull)
            .anyMatch(auth -> verificationBodyId.equals(auth.getVerificationBodyId()));
    }

    /**
     * Checks that a VERIFIER has the permissions to verification body.
     * @param user the user to authorize.
     * @param verificationBodyId the verification body to check permission on
     * @param permission to check
     * @return if the user has the permissions on the verification body
     */
    public boolean isAuthorized(AppUser user, Long verificationBodyId, String permission) {
        return user.getAuthorities()
            .stream()
            .filter(Objects::nonNull)
            .filter(auth -> verificationBodyId.equals(auth.getVerificationBodyId()))
            .flatMap(authority -> authority.getPermissions().stream())
            .toList().contains(permission);
    }
}
