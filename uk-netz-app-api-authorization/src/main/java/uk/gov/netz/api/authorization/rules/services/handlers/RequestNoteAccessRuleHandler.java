package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.RequestNoteAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;

import java.util.Set;


@Service("requestNoteAccessHandler")
@RequiredArgsConstructor
public class RequestNoteAccessRuleHandler implements AuthorizationResourceRuleHandler {

    private final AppAuthorizationService appAuthorizationService;
    private final RequestNoteAuthorityInfoProvider requestNoteAuthorityInfoProvider;

    @Override
    public void evaluateRules(final Set<AuthorizationRuleScopePermission> authorizationRules,
                              final AppUser user,
                              final String resourceId) {

        final RequestAuthorityInfoDTO requestInfo =
            requestNoteAuthorityInfoProvider.getRequestNoteInfo(Long.parseLong(resourceId));

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
            	.requestResources(requestInfo.getAuthorityInfo().getRequestResources())
                .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
