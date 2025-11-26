package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.NotificationTemplateAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Map;
import java.util.Set;

@Service("notificationTemplateAccessHandler")
@RequiredArgsConstructor
public class NotificationTemplateAccessRuleHandler implements AuthorizationResourceRuleHandler {

    private final NotificationTemplateAuthorityInfoProvider templateAuthorityInfoProvider;
    private final AppAuthorizationService appAuthorizationService;

    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user,
                              String resourceId) {

        CompetentAuthorityEnum competentAuthority = templateAuthorityInfoProvider.getNotificationTemplateCaById(Long.parseLong(resourceId));

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
            	.requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
