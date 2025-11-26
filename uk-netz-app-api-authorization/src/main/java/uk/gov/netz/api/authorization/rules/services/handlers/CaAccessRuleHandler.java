package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Map;
import java.util.Set;

@Service("caAccessHandler")
@RequiredArgsConstructor
public class CaAccessRuleHandler implements AuthorizationRuleHandler {

    private final AppAuthorizationService appAuthorizationService;

    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user) {

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                    .permission(rule.getPermission())
                    .requestResources(Map.of(ResourceType.CA, user.getCompetentAuthority().name()))
                    .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
