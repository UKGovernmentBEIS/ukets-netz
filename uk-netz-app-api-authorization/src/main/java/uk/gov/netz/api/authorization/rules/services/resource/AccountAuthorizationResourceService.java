package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.ResourceScopePermissionService;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class AccountAuthorizationResourceService {
    
    private final ResourceScopePermissionService resourceScopePermissionService;
    private final AppAuthorizationService appAuthorizationService;

    public boolean hasUserScopeToAccount(AppUser authUser, Long accountId, String scope) {
        String requiredPermission =
                resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.ACCOUNT, authUser.getRoleType(), scope)
                .map(ResourceScopePermission::getPermission)
                .orElse(null);

        if(requiredPermission == null) {
            return false;
        }

        AuthorizationCriteria authCriteria = 
                AuthorizationCriteria.builder()
                	.requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
                    .permission(requiredPermission)
                    .build();
        try {
            appAuthorizationService.authorize(authUser, authCriteria);
        } catch (BusinessException e) {
            return false;
        }
        
        return true;
    }
}
