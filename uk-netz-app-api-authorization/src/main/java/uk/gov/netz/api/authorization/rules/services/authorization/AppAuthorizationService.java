package uk.gov.netz.api.authorization.rules.services.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class AppAuthorizationService {

    private final RoleTypeAuthorizationServiceDelegator roleTypeAuthorizationServiceDelegator;

    /**
     * Authorizes user based on {@link AuthorizationCriteria} and {@link RoleTypeConstants}.
     *
     * @param user the authenticated user
     * @param authorizationCriteria the {@link AuthorizationCriteria} based on which criteria the authorization is performed on.
     * @throws BusinessException {@link ErrorCode} FORBIDDEN if authorization fails.
     */
    public void authorize(AppUser user, AuthorizationCriteria authorizationCriteria) {
        boolean isAuthorized = roleTypeAuthorizationServiceDelegator.isAuthorized(user, authorizationCriteria);

        if (!isAuthorized) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
