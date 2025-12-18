package uk.gov.netz.api.authorization.rules.services.handlers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.services.AuthorizationResourceRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.dto.RequestAuthorityInfoDTO;
import uk.gov.netz.api.authorization.rules.services.authorityinfo.providers.RequestAuthorityInfoProvider;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;

@Service("requestAccessHandler")
@RequiredArgsConstructor
public class RequestAccessRuleHandler implements AuthorizationResourceRuleHandler {
    private final AppAuthorizationService appAuthorizationService;
    private final RequestAuthorityInfoProvider requestAuthorityInfoProvider;

    /**
     * @param user the authenticated user
     * @param authorizationRules the list of
     * @param resourceId the resourceId for which the rules apply.
     * @throws BusinessException {@link ErrorCode} FORBIDDEN if authorization fails.
     *
     * Authorizes access on {@link uk.gov.netz.api.account.domain.Account} or {@link CompetentAuthorityEnum}
     * of {@link uk.gov.netz.api.workflow.request.core.domain.Request} with id the {@code resourceId}
     * and permission of the rule
     */
    @Override
    public void evaluateRules(@Valid Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user, String resourceId) {
        RequestAuthorityInfoDTO requestInfoDTO = requestAuthorityInfoProvider.getRequestInfo(resourceId);
        
        List<AuthorizationRuleScopePermission> appliedRules = 
                authorizationRules.stream()
                .filter(rule -> requestInfoDTO.getType().equals(rule.getResourceSubType()))
                .toList();
        
        if (appliedRules.isEmpty()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        appliedRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
            		.requestResources(requestInfoDTO.getAuthorityInfo().getRequestResources())
                    .permission(rule.getPermission())
                    .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
