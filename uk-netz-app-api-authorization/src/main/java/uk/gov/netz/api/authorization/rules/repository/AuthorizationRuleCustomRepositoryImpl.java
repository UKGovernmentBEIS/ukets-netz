package uk.gov.netz.api.authorization.rules.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRule;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class AuthorizationRuleCustomRepositoryImpl implements AuthorizationRuleCustomRepository {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Map<String, Set<String>> findResourceSubTypesRoleTypes() {
        return em.createNamedQuery(AuthorizationRule.NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_ROLE_TYPES, Tuple.class)
                .getResultStream()
                .collect(
                    Collectors.groupingBy(
                            t -> (String)t.get("resourceSubType"),
                            Collectors.mapping(t -> (String)t.get("roleType"), Collectors.toSet())
                           ));
    }
    
    @Override
    public Optional<String> findRoleTypeByResourceTypeAndSubType(String resourceType, String resourceSubType) {
        List<String> roleTypes =     
                    em.createNamedQuery(AuthorizationRule.NAMED_QUERY_FIND_ROLE_TYPE_BY_RESOURCE_TYPE_AND_SUB_TYPE, String.class)
                        .setParameter("resourceType", resourceType)
                        .setParameter("resourceSubType", resourceSubType)
                        .getResultList();
        if (roleTypes.size() >  1) {
            throw new NonUniqueResultException("More than one role type found for resource type: %s and sub type: %s");
        }
        
        return roleTypes.isEmpty() ? Optional.empty() : Optional.ofNullable(roleTypes.get(0));
    }

    @Override
    public Set<String> findResourceSubTypesByResourceTypeAndRoleType(String resourceType, String roleType) {
        return new HashSet<>(em.createNamedQuery(AuthorizationRule.NAMED_QUERY_FIND_RESOURCE_SUB_TYPE_BY_RESOURCE_TYPE_AND_ROLE_TYPE, String.class)
                .setParameter("resourceType", resourceType)
                .setParameter("roleType", roleType)
                .getResultList());
    }
}
