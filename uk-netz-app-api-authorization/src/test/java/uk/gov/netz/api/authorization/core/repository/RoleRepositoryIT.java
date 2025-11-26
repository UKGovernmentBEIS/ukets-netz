package uk.gov.netz.api.authorization.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.transaction.TestTransaction;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.domain.RolePermission;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_TASK_ASSIGNMENT;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.OPERATOR;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.REGULATOR;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class RoleRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void findByType_no_roles() {
        createRole("Code 1", "code1", OPERATOR, PERM_ACCOUNT_USERS_EDIT);
        createRole("Code 2", "code2", OPERATOR,
                PERM_TASK_ASSIGNMENT);
        List<Role> roles = roleRepository.findByType(REGULATOR);
        assertThat(roles).isEmpty();
    }

    @Test
    void findByType() {
        Role role1 = createRole("Code 1", "code1", REGULATOR,
                PERM_ACCOUNT_USERS_EDIT);
        Role role2 = createRole("Code 2", "code2", REGULATOR,
                PERM_TASK_ASSIGNMENT);
        List<Role> roles = roleRepository.findByType(REGULATOR);
        assertThat(List.of(role1, role2)).hasSameElementsAs(roles);
    }

    @Test
    void testLazyInitialization_whenLazyOneToManyAccessedAfterSessionCloses_thenThrowException() {

        final Role role = new Role();
        role.setName("roleName");
        role.setCode("roleCode");
        role.setType(RoleTypeConstants.OPERATOR);
        role.setRolePermissions(new ArrayList<>());

        entityManager.persist(role);
        entityManager.flush();
        entityManager.clear();

        final List<Role> roles = roleRepository.findAll();

        TestTransaction.end();

        assertEquals(1, roles.size());
        assertThrows(LazyInitializationException.class, () -> roles.get(0).getRolePermissions().size());
    }

    private Role createRole(String name, String code, String roleType, String... permissions) {
        Role role = Role.builder()
            .name(name)
            .code(code)
            .type(roleType)
            .build();

        for (String permission : permissions) {
            role.addPermission(
                RolePermission.builder()
                    .permission(permission).build());
        }

        entityManager.persist(role);

        return role;
    }

}
