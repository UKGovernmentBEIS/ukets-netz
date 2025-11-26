package uk.gov.netz.api.authorization.rules.services.authorization;

import uk.gov.netz.api.authorization.core.domain.AppUser;

public interface RoleTypeAuthorizationService {
    boolean isAuthorized(AppUser user, AuthorizationCriteria criteria);
    String getRoleType();
}
