package uk.gov.netz.api.authorization.rules.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.repository.ResourceScopePermissionRepository;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceScopePermissionService {

    private final ResourceScopePermissionRepository resourceScopePermissionRepository;
    
    public Optional<ResourceScopePermission> findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(
            String resourceType, String resourceSubType, String roleType, String scope) {
        return resourceScopePermissionRepository.findByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(resourceType, resourceSubType, roleType, scope);
    }
    
    public Optional<ResourceScopePermission> findByResourceTypeAndRoleTypeAndScope(String resourceType, String roleType, String scope) {
        return resourceScopePermissionRepository.findByResourceTypeAndRoleTypeAndScope(resourceType, roleType, scope);
    }
    
    public boolean existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(String resourceType, String resourceSubType, String roleType, String scope) {
        return resourceScopePermissionRepository.existsByResourceTypeAndResourceSubTypeAndRoleTypeAndScope(resourceType, resourceSubType, roleType, scope);
    }

    public Set<ResourceScopePermission> findByResourceTypeAndRoleType(String resourceType, String roleType) {
        return resourceScopePermissionRepository.findByResourceTypeAndRoleType(resourceType, roleType);
    }
}
