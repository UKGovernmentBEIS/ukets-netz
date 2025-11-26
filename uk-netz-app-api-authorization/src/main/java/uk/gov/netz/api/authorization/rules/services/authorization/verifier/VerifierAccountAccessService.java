package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import uk.gov.netz.api.authorization.core.domain.AppUser;

import java.util.Set;

public interface VerifierAccountAccessService {
    Set<Long> findAuthorizedAccountIds(AppUser user);
}
