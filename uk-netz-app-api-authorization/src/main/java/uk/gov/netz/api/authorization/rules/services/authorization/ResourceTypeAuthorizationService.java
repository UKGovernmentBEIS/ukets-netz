package uk.gov.netz.api.authorization.rules.services.authorization;

import uk.gov.netz.api.authorization.core.domain.AppUser;

public interface ResourceTypeAuthorizationService {
    boolean isAuthorized(AppUser user, AuthorizationCriteria criteria);
    boolean isApplicable(AuthorizationCriteria criteria);
}
