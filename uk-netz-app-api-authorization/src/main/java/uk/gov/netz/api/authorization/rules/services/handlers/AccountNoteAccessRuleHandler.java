package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.AccountNoteAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Map;
import java.util.Set;


@Service("accountNoteAccessHandler")
@RequiredArgsConstructor
public class AccountNoteAccessRuleHandler implements AuthorizationResourceRuleHandler {
    
    private final AppAuthorizationService appAuthorizationService;
    private final AccountNoteAuthorityInfoProvider accountNoteAuthorityInfoProvider;
    
    @Override
    public void evaluateRules(final Set<AuthorizationRuleScopePermission> authorizationRules, 
                              final AppUser user,
                              final String resourceId) {

        final Long accountId = accountNoteAuthorityInfoProvider.getAccountIdById(Long.parseLong(resourceId));

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
            		.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                    .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }

}
