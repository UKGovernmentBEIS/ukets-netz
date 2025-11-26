package uk.gov.netz.api.authorization.rules.repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface AuthorizationRuleCustomRepository {

    @Transactional(readOnly = true)
    Map<String, Set<String>> findResourceSubTypesRoleTypes();
    
    @Transactional(readOnly = true)
    Optional<String> findRoleTypeByResourceTypeAndSubType(String resourceType, String resourceSubType);
    
    @Transactional(readOnly = true)
    Set<String> findResourceSubTypesByResourceTypeAndRoleType(String resourceType, String roleType);
}
