package uk.gov.netz.api.authorization.rules.services.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleTypeAuthorizationServiceDelegator {

    private final List<RoleTypeAuthorizationService> roleTypeAuthorizationServices;

    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        return getRoleTypeService(user)
            .map(resourceAuthorizationService -> resourceAuthorizationService.isAuthorized(user, criteria))
            .orElse(false);
    }

    private Optional<RoleTypeAuthorizationService> getRoleTypeService(AppUser user) {
        return roleTypeAuthorizationServices.stream()
            .filter(roleTypeAuthorizationService -> roleTypeAuthorizationService.getRoleType().equals(user.getRoleType()))
            .findAny();
    }
}
