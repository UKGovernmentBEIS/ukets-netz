package uk.gov.netz.api.security;

import uk.gov.netz.api.authorization.core.domain.AppUser;

public interface AppSecurityComponentProvider {

    AppUser getAuthenticatedUser();

    String getAccessToken();
}
