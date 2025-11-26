package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.GuidanceSectionAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Map;
import java.util.Set;


@Service("guidanceSectionAccessHandler")
@RequiredArgsConstructor
@ConditionalOnBean(GuidanceSectionAuthorityInfoProvider.class)
public class GuidanceSectionAccessRuleHandler implements AuthorizationResourceRuleHandler {

    private final AppAuthorizationService appAuthorizationService;
    private final GuidanceSectionAuthorityInfoProvider guidanceSectionAuthorityInfoProvider;

    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user, String resourceId) {

        if (authorizationRules.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        CompetentAuthorityEnum competentAuthority =
                guidanceSectionAuthorityInfoProvider.getGuidanceSectionCaById(Long.parseLong(resourceId));

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
                    .requestResources(Map.of(ResourceType.CA, competentAuthority.name()))
                    .permission(rule.getPermission())
                    .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
