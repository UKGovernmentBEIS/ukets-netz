package uk.gov.netz.api.authorization.core.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.Role;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Transactional(readOnly = true)
    @EntityGraph(value = "role-permissions-graph", type = EntityGraph.EntityGraphType.FETCH)
    List<Role> findByType(String roleType);
    
    @Transactional(readOnly = true)
    @EntityGraph(value = "role-permissions-graph", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Role> findByCode(String code);

}
