package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.ResourceScopePermissionService;
import uk.gov.netz.api.authorization.rules.services.authorization.AppAuthorizationService;
import uk.gov.netz.api.authorization.rules.services.authorization.AuthorizationCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestTaskAuthorizationResourceService {
    private final List<RequestTaskRoleTypeAuthorizationQueryService> requestTaskRoleTypeAuthorizationQueryServices;
    private final ResourceScopePermissionService resourceScopePermissionService;
    private final AppAuthorizationService appAuthorizationService;

    public boolean hasUserExecuteScopeOnRequestTaskType(AppUser authUser, String requestTaskType, ResourceCriteria resourceCriteria) {
        String requiredPermission =
                resourceScopePermissionService.findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, requestTaskType, authUser.getRoleType(), Scope.REQUEST_TASK_EXECUTE)
                        .map(ResourceScopePermission::getPermission)
                        .orElse(null);

        AuthorizationCriteria authCriteria = getAuthorizationCriteriaByRoleType(authUser.getRoleType(), resourceCriteria, requiredPermission);
        try {
            appAuthorizationService.authorize(authUser, authCriteria);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    public boolean hasUserAssignScopeOnRequestTasks(AppUser authUser, ResourceCriteria resourceCriteria) {
        String requiredPermission =
                resourceScopePermissionService.findByResourceTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, authUser.getRoleType(), Scope.REQUEST_TASK_ASSIGN)
                        .map(ResourceScopePermission::getPermission)
                        .orElse(null);

        AuthorizationCriteria authCriteria = getAuthorizationCriteriaByRoleType(authUser.getRoleType(), resourceCriteria, requiredPermission);

        try {
            appAuthorizationService.authorize(authUser, authCriteria);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    public List<String> findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
            String requestTaskType, ResourceCriteria resourceCriteria, String roleType) {
        boolean requiresPermission =
                resourceScopePermissionService.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(ResourceType.REQUEST_TASK, requestTaskType, roleType, Scope.REQUEST_TASK_EXECUTE);

        return requestTaskRoleTypeAuthorizationQueryServices.stream()
                .filter(service -> service.getRoleType().equals(roleType))
                .findAny()
                .map(service -> service.findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(requestTaskType, resourceCriteria, requiresPermission))
                .orElseThrow(() -> new UnsupportedOperationException(String.format("Role type %s is not supported yet", roleType)));
    }

    public Set<String> findRequestTaskTypesByRoleType(String roleType) {
        return resourceScopePermissionService.findByResourceTypeAndRoleType(ResourceType.REQUEST_TASK, roleType).stream()
                .map(ResourceScopePermission::getResourceSubType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private AuthorizationCriteria getAuthorizationCriteriaByRoleType(String roleType, ResourceCriteria resourceCriteria, String requiredPermission) {
        return RoleTypeConstants.VERIFIER.equals(roleType) ?
                AuthorizationCriteria.builder()
                		.requestResources(Map.of(ResourceType.ACCOUNT, resourceCriteria.getAccountId().toString()))
                        .permission(requiredPermission).build() :
                AuthorizationCriteria.builder()
                        .requestResources(resourceCriteria.getRequestResources())
                        .permission(requiredPermission)
                        .build();
    }
}
