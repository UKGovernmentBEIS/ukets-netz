package uk.gov.netz.api.authorization.rules.services.authorization.regulator;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Objects;

@Service
@Order(100)
@RequiredArgsConstructor
public class RegulatorCompetentAuthorityAuthorizationService implements RegulatorResourceTypeAuthorizationService {

    @Override
    public boolean isAuthorized(AppUser user, AuthorizationCriteria criteria) {
        if (criteria.getPermission() == null) {
            return isAuthorized(user, criteria.getCompetentAuthority());
        } else {
            return isAuthorized(user, criteria.getCompetentAuthority(), criteria.getPermission());
        }
    }

    @Override
    public boolean isApplicable(AuthorizationCriteria criteria) {
        return criteria.getCompetentAuthority() != null;
    }

    /**
     * checks that a REGULATOR has access to competentAuthority
     * @param user the user to authorize.
     * @param competentAuthority the {@link CompetentAuthorityEnum} to check permission on.
     * @return if the user is authorized on competentAuthority.
     */
    public boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority) {
        return user.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .map(auth -> competentAuthority == auth.getCompetentAuthority())
                .findAny()
                .orElse(false);
    }

    /**
     * checks that a REGULATOR has the permissions to competentAuthority
     * @param user the user to authorize.
     * @param competentAuthority the {@link CompetentAuthorityEnum} to check permission on.
     * @param permission to check
     * @return if the user has the permissions on the competentAuthority
     */
    public boolean isAuthorized(AppUser user, CompetentAuthorityEnum competentAuthority, String permission) {
        return user.getAuthorities()
                .stream()
                .filter(Objects::nonNull)
                .filter(authority -> competentAuthority == authority.getCompetentAuthority())
                .flatMap(authority -> authority.getPermissions().stream())
                .toList().contains(permission);
    }
}
