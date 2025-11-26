package uk.gov.netz.api.authorization.rules.services;

import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Arrays;

/**
 * Service that authorizes user based only on role type.
 */
@Service
public class RoleAuthorizationService {

    /**
     * Evaluates whether the {@code appUser} has any of the {@code roleTypes}.
     *
     * @param appUser {@link AppUser}
     * @param roleTypes the role type array
     */
    public void evaluate(AppUser appUser, String[] roleTypes) {
        if (!Arrays.asList(roleTypes).contains(appUser.getRoleType()) || appUser.getAuthorities().isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
