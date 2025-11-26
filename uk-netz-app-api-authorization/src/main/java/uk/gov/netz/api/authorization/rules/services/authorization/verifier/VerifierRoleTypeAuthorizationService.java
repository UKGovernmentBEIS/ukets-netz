package uk.gov.netz.api.authorization.rules.services.authorization.verifier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VerifierRoleTypeAuthorizationService implements RoleTypeAuthorizationService {
    private final List<VerifierResourceTypeAuthorizationService> verifierResourceTypeAuthorizationServices;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        return verifierResourceTypeAuthorizationServices.stream()
                .filter(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isApplicable(criteria))
                .findFirst()
                .map(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isAuthorized(user, criteria))
                .orElse(false);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.VERIFIER;
    }
}
