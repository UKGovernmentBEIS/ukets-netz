package uk.gov.netz.api.authorization.rules.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.rules.repository.AuthorizationRuleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorizationRulesQueryService {

    private final AuthorizationRuleRepository authorizationRuleRepository;
    
    public Optional<String> findRoleTypeByResourceTypeAndSubType(String resourceType, String resourceSubType) {
        return authorizationRuleRepository
                .findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
    }
    
    public Map<String, Set<String>> findResourceSubTypesRoleTypes() {
        return authorizationRuleRepository.findResourceSubTypesRoleTypes();
    }
    
    public Set<String> findResourceSubTypesByResourceTypeAndRoleType(String resourceType, String roleType) {
        return authorizationRuleRepository.findResourceSubTypesByResourceTypeAndRoleType(resourceType, roleType);
    }
}
