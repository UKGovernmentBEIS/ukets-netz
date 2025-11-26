package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegulatorRoleTypeAuthorizationService implements RoleTypeAuthorizationService {
    private final List<RegulatorResourceTypeAuthorizationService> regulatorResourceTypeAuthorizationServices;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        return regulatorResourceTypeAuthorizationServices.stream()
                .filter(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isApplicable(criteria))
                .findFirst()
                .map(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isAuthorized(user, criteria))
                .orElse(false);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.REGULATOR;
    }
}
