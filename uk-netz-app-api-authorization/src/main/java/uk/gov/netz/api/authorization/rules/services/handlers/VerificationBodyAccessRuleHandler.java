package uk.gov.netz.api.authorization.rules.services.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRuleHandler;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service("verificationBodyAccessHandler")
@RequiredArgsConstructor
public class VerificationBodyAccessRuleHandler implements AuthorizationRuleHandler {

    private final AppAuthorizationService appAuthorizationService;

    @Override
    public void evaluateRules(Set<AuthorizationRuleScopePermission> authorizationRules, AppUser user) {
        final Long userVerificationBodyId = Optional.ofNullable(user.getVerificationBodyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN));

        authorizationRules.forEach(rule -> {
            AuthorizationCriteria authorizationCriteria = AuthorizationCriteria.builder()
            		.requestResources(Map.of(ResourceType.VERIFICATION_BODY, userVerificationBodyId.toString()))
                    .permission(rule.getPermission())
                    .build();
            appAuthorizationService.authorize(user, authorizationCriteria);
        });
    }
}
