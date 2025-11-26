package uk.gov.netz.api.authorization.rules.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRule;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class AuthorizationRuleCustomRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private AuthorizationRuleCustomRepositoryImpl repo;
    
    @Autowired
    private EntityManager entityManager;
    
    @Test
    void findResourceSubTypesRoleTypes() {
        createRule(ResourceType.REQUEST_TASK, "requestTaskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        createRule(ResourceType.REQUEST_TASK, "requestTaskType2", "handler2", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.OPERATOR);
        createRule(ResourceType.REQUEST_ACTION, "sub", "handler3", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.OPERATOR);
        createRule(ResourceType.REQUEST_ACTION, "sub", "handler3", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        
        Map<String, Set<String>> result = repo.findResourceSubTypesRoleTypes();
        
        assertThat(result)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                    "requestTaskType1", Set.of(RoleTypeConstants.REGULATOR),
                    "requestTaskType2", Set.of(RoleTypeConstants.OPERATOR),
                    "sub", Set.of(RoleTypeConstants.OPERATOR, RoleTypeConstants.REGULATOR)
                    ));
    }
    
    @Test
    void findRoleTypeByResourceTypeAndSubType() {
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        createRule(resourceType, resourceSubType, "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        
        createRule(resourceType, resourceSubType, "handler2", Scope.REQUEST_TASK_VIEW, RoleTypeConstants.REGULATOR);
        
        Optional<String> ruleOpt = repo.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType);
        
        assertThat(ruleOpt)
                .isNotEmpty()
                .contains(RoleTypeConstants.REGULATOR);
    }
    
    @Test
    void findRoleTypeByResourceTypeAndSubType_not_found() {
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        
        createRule(resourceType, resourceSubType, "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        
        Optional<String> ruleOpt = repo.findRoleTypeByResourceTypeAndSubType(resourceType, "invalid");
        
        assertThat(ruleOpt).isEmpty();
    }
    
    @Test
    void findRoleTypeByResourceTypeAndSubType_multiple_role_types_found() {
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        
        createRule(resourceType, resourceSubType, "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        createRule(resourceType, resourceSubType, "handler2", Scope.REQUEST_TASK_VIEW, RoleTypeConstants.OPERATOR);
        
        assertThrows(NonUniqueResultException.class, () ->
                repo.findRoleTypeByResourceTypeAndSubType(resourceType, resourceSubType));
    }
    
    private void createRule(String resourceType, String resourceSubType, String handler, String scope, String roleType) {
        AuthorizationRule rule = AuthorizationRule.builder()
                .resourceType(resourceType)
                .resourceSubType(resourceSubType)
                .handler(handler)
                .scope(scope)
                .roleType(roleType)
                .build();
        entityManager.persist(rule);
    }
}
