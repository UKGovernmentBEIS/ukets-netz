package uk.gov.netz.api.authorization.rules.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class AuthorizationRulesService {

    private final AuthorizationRuleRepository authorizationRuleRepository;
    private final Map<String, AuthorizationResourceRuleHandler> authorizationResourceRuleHandlers;
    private final Map<String, AuthorizationRuleHandler> authorizationRuleHandlers;

    public void evaluateRules(AppUser user, String service, String resourceId, String resourceType, String resourceSubType) {
        Map<String, Set<AuthorizationRuleScopePermission>> rules = getAuthorizationServiceRules(user, service, resourceType, resourceSubType);
        rules.forEach((key, value) -> authorizationResourceRuleHandlers.get(key).evaluateRules(value, user, resourceId));
    }

    public void evaluateRules(AppUser user, String service) {
        Map<String, Set<AuthorizationRuleScopePermission>> rules = getAuthorizationServiceRules(user, service, null, null);
        rules.forEach((key, value) -> authorizationRuleHandlers.get(key).evaluateRules(value, user));
    }

    private Map<String, Set<AuthorizationRuleScopePermission>> getAuthorizationServiceRules(AppUser user, String service, String resourceType, String resourceSubType) {
        final List<AuthorizationRuleScopePermission> rules = authorizationRuleRepository.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                service,
                user.getRoleType(),
                resourceType,
                resourceSubType);

        if (rules.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return rules
                .stream()
                .collect(Collectors.groupingBy(AuthorizationRuleScopePermission::getHandler, Collectors.toSet()));
    }

}
