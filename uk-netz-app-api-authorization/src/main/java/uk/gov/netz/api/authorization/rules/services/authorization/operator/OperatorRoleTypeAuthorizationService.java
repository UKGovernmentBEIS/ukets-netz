package uk.gov.netz.api.authorization.rules.services.authorization.operator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.authorization.rules.services.authorization.RoleTypeAuthorizationService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorRoleTypeAuthorizationService implements RoleTypeAuthorizationService {
    private final List<OperatorResourceTypeAuthorizationService> operatorResourceTypeAuthorizationServices;

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        return operatorResourceTypeAuthorizationServices.stream()
                .filter(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isApplicable(criteria))
                .findFirst()
                .map(resourceTypeAuthorizationService -> resourceTypeAuthorizationService.isAuthorized(user, criteria))
                .orElse(false);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.OPERATOR;
    }
}
