package uk.gov.netz.api.authorization.core.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.authorization.core.domain.UserRoleType;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.AuditConfiguration;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, AuditConfiguration.class})
class UserRoleTypeRepositoryIT extends AbstractContainerBaseTest {
	
    @Autowired
    private UserRoleTypeRepository userRoleTypeRepository;
    
    @Autowired
	private EntityManager entityManager;

    @Test
    void findById() {
    	final String userId = "user1";
    	createUserRoleType(userId, RoleTypeConstants.OPERATOR);
    	createUserRoleType("user2", RoleTypeConstants.OPERATOR);
    	flushAndClear();

    	Optional<UserRoleType> optionalUserRole = userRoleTypeRepository.findById(userId);

        assertThat(optionalUserRole).isPresent();
        assertThat(optionalUserRole.get()).isEqualTo(UserRoleType.builder().userId(userId).roleType(RoleTypeConstants.OPERATOR).build());
    }
    
    @Test
    void findById_empty() {
    	final String userId = "user1";

    	Optional<UserRoleType> optionalUserRole = userRoleTypeRepository.findById(userId);

        assertThat(optionalUserRole).isEmpty();
    }
    
    @Test
    void existsByUserId() {
    	final String userId = "user1";
    	createUserRoleType(userId, RoleTypeConstants.OPERATOR);

    	boolean result = userRoleTypeRepository.existsByUserId(userId);
        assertThat(result).isTrue();
        
        result = userRoleTypeRepository.existsByUserId("another");
        assertThat(result).isFalse();
    }
    
    private UserRoleType createUserRoleType(String userId, String roleType) {
		UserRoleType userRoleType = UserRoleType.builder().userId(userId).roleType(roleType).build();
		entityManager.persist(userRoleType);
		return userRoleType;
	}
	
	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

}