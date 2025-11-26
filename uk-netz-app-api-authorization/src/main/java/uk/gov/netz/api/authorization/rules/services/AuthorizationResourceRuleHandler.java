package uk.gov.netz.api.authorization.rules.services;

import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Set;

public interface AuthorizationResourceRuleHandler {

    /**
     * Evaluates the {@code authorizationRules}.
     * @param authorizationRules the list of {@link AuthorizationRuleScopePermission}
     * @param user the authenticated user
     * @param resourceId the resourceId for which the rules apply.
     * @throws BusinessException {@link ErrorCode} FORBIDDEN if authorization fails.
     */
    void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user, String resourceId);
}
