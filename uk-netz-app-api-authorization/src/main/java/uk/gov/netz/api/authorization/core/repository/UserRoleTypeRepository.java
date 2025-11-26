package uk.gov.netz.api.authorization.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.netz.api.authorization.core.domain.UserRoleType;

/**
 * The UserRoleType Repository.
 */
@Repository
public interface UserRoleTypeRepository extends JpaRepository<UserRoleType, String> {
	
	@Transactional(readOnly = true)
    boolean existsByUserId(String userId);
	
}
