package uk.gov.netz.api.authorization.rules.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRule;
import uk.gov.netz.api.authorization.rules.domain.AuthorizationRuleScopePermission;
import uk.gov.netz.api.authorization.rules.domain.AuthorizedService;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class AuthorizationRuleRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private AuthorizationRuleRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType_resourceType_null_resourceSubType_null() {
        //prepare data
        AuthorizedService service1 = buildService("service1");
        AuthorizedService service2 = buildService("service2");

        AuthorizationRule rule1 = buildRule(ResourceType.REQUEST_TASK, "taskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule2 = buildRule(ResourceType.CA, null, "handler2", null, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule3 = buildRule(ResourceType.ACCOUNT, null, "handler3", null, RoleTypeConstants.OPERATOR);
        AuthorizationRule rule4 = buildRule(ResourceType.REQUEST_ACTION, null, "handler3", Scope.EDIT_USER, RoleTypeConstants.REGULATOR);

        //add rule1, rule2 and rule3 to service 1
        service1.getRules().add(rule1);
        rule1.getServices().add(service1);

        service1.getRules().add(rule2);
        rule2.getServices().add(service1);

        service1.getRules().add(rule3);
        rule3.getServices().add(service1);


        //add rule1, rule3 and rule4 to service2
        service2.getRules().add(rule1);
        rule1.getServices().add(service2);

        service2.getRules().add(rule3);
        rule3.getServices().add(service2);

        service2.getRules().add(rule4);
        rule4.getServices().add(service2);

        entityManager.persist(rule1);
        entityManager.persist(rule2);
        entityManager.persist(rule3);
        entityManager.persist(rule4);
        entityManager.persist(service1);
        entityManager.persist(service2);

		ResourceScopePermission scope1 = buildPermissionScope(ResourceType.REQUEST_TASK, "taskType1",
				Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
				RoleTypeConstants.REGULATOR);
        entityManager.persist(scope1);
		ResourceScopePermission scope3 = buildPermissionScope(ResourceType.REQUEST_ACTION, null,
				Permission.PERM_ACCOUNT_USERS_EDIT, Scope.EDIT_USER, RoleTypeConstants.REGULATOR);
        entityManager.persist(scope3);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service1", RoleTypeConstants.REGULATOR, null, null);

        //assert
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType("taskType1")
                                .handler("handler1")
                                .permission(Permission.PERM_CA_USERS_EDIT)
                                .build(),
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.CA)
                                .resourceSubType(null)
                                .handler("handler2")
                                .permission(null)
                                .build()
                );
    }

    @Test
    void findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType_resourceType_null() {
        //prepare data
        AuthorizedService service1 = buildService("service1");
        AuthorizedService service2 = buildService("service2");

        AuthorizationRule rule1 = buildRule(ResourceType.REQUEST_TASK, "taskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule2 = buildRule(ResourceType.CA, null, "handler2", null, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule3 = buildRule(ResourceType.ACCOUNT, null, "handler3", null, RoleTypeConstants.OPERATOR);
        AuthorizationRule rule4 = buildRule(ResourceType.REQUEST_ACTION, null, "handler3", Scope.EDIT_USER, RoleTypeConstants.REGULATOR);

        //add rule1, rule2 and rule3 to service 1
        service1.getRules().add(rule1);
        rule1.getServices().add(service1);

        service1.getRules().add(rule2);
        rule2.getServices().add(service1);

        service1.getRules().add(rule3);
        rule3.getServices().add(service1);


        //add rule1, rule3 and rule4 to service2
        service2.getRules().add(rule1);
        rule1.getServices().add(service2);

        service2.getRules().add(rule3);
        rule3.getServices().add(service2);

        service2.getRules().add(rule4);
        rule4.getServices().add(service2);

        entityManager.persist(rule1);
        entityManager.persist(rule2);
        entityManager.persist(rule3);
        entityManager.persist(rule4);
        entityManager.persist(service1);
        entityManager.persist(service2);

        ResourceScopePermission scope1 = buildPermissionScope(ResourceType.REQUEST_TASK, "taskType1",
                Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
                RoleTypeConstants.REGULATOR);
        entityManager.persist(scope1);
        ResourceScopePermission scope3 = buildPermissionScope(ResourceType.REQUEST_ACTION, null,
                Permission.PERM_ACCOUNT_USERS_EDIT, Scope.EDIT_USER, RoleTypeConstants.REGULATOR);
        entityManager.persist(scope3);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service1", RoleTypeConstants.REGULATOR, null, "taskType1");

        //assert
        assertThat(result)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType("taskType1")
                                .handler("handler1")
                                .permission(Permission.PERM_CA_USERS_EDIT)
                                .build()
                );
    }

    @Test
    void findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType_resourceSubType_null() {
        //prepare data
        AuthorizedService service1 = buildService("service1");
        AuthorizedService service2 = buildService("service2");

        AuthorizationRule rule1 = buildRule(ResourceType.REQUEST_TASK, "taskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule2 = buildRule(ResourceType.REQUEST_TASK, null, "handler2", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        AuthorizationRule rule3 = buildRule(ResourceType.ACCOUNT, null, "handler3", null, RoleTypeConstants.OPERATOR);
        AuthorizationRule rule4 = buildRule(ResourceType.REQUEST_ACTION, null, "handler3", Scope.EDIT_USER, RoleTypeConstants.REGULATOR);

        //add rule1, rule2 and rule3 to service 1
        service1.getRules().add(rule1);
        rule1.getServices().add(service1);

        service1.getRules().add(rule2);
        rule2.getServices().add(service1);

        service1.getRules().add(rule3);
        rule3.getServices().add(service1);


        //add rule1, rule3 and rule4 to service2
        service2.getRules().add(rule1);
        rule1.getServices().add(service2);

        service2.getRules().add(rule3);
        rule3.getServices().add(service2);

        service2.getRules().add(rule4);
        rule4.getServices().add(service2);

        entityManager.persist(rule1);
        entityManager.persist(rule2);
        entityManager.persist(rule3);
        entityManager.persist(rule4);
        entityManager.persist(service1);
        entityManager.persist(service2);

        ResourceScopePermission scope1 = buildPermissionScope(ResourceType.REQUEST_TASK, "taskType1",
                Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
                RoleTypeConstants.REGULATOR);
        entityManager.persist(scope1);
        ResourceScopePermission scope3 = buildPermissionScope(ResourceType.REQUEST_ACTION, null,
                Permission.PERM_ACCOUNT_USERS_EDIT, Scope.EDIT_USER, RoleTypeConstants.REGULATOR);
        entityManager.persist(scope3);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service1", RoleTypeConstants.REGULATOR, ResourceType.REQUEST_TASK, null);

        //assert
        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType("taskType1")
                                .handler("handler1")
                                .permission(Permission.PERM_CA_USERS_EDIT)
                                .build(),
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType(null)
                                .handler("handler2")
                                .permission(null)
                                .build()
                );
    }
    
    @Test
    void findRulePermissionsByServiceAndRoleType_AuthorizationRuleScopePermission_with_different_role_type() {
        //prepare data
        AuthorizedService service1 = buildService("service1");

        AuthorizationRule rule1 = buildRule(ResourceType.REQUEST_TASK, "taskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);

        service1.getRules().add(rule1);
        rule1.getServices().add(service1);

        entityManager.persist(rule1);
        entityManager.persist(service1);

		ResourceScopePermission scope1 = buildPermissionScope(ResourceType.REQUEST_TASK, "taskType1",
				Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
				RoleTypeConstants.OPERATOR);
        entityManager.persist(scope1);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service1", RoleTypeConstants.REGULATOR, null, null);

        //assert
        assertThat(result).hasSize(1)
        .containsExactlyInAnyOrder(
                AuthorizationRuleScopePermission.builder()
                        .resourceType(ResourceType.REQUEST_TASK)
                        .resourceSubType("taskType1")
                        .handler("handler1")
                        .permission(null)
                        .build()
        );
    }
    
    @Test
    void findRulePermissionsByServiceAndRoleType_AuthorizationRuleScopePermission_with_same_role_type() {
        //prepare data
        AuthorizedService service1 = buildService("service1");

        AuthorizationRule rule1 = buildRule(ResourceType.REQUEST_TASK, "taskType1", "handler1", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);

        service1.getRules().add(rule1);
        rule1.getServices().add(service1);

        entityManager.persist(rule1);
        entityManager.persist(service1);

		ResourceScopePermission scope1 = buildPermissionScope(ResourceType.REQUEST_TASK, "taskType1",
				Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);
        entityManager.persist(scope1);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service1", RoleTypeConstants.REGULATOR, null, null);

        //assert
        assertThat(result)
        	.hasSize(1)
        	.containsExactly(
                    AuthorizationRuleScopePermission.builder()
                            .resourceType(ResourceType.REQUEST_TASK)
                            .resourceSubType("taskType1")
                            .handler("handler1")
                            .permission(Permission.PERM_CA_USERS_EDIT)
                            .build()
            );
    }

    @Test
    void findRulePermissionsByServiceAndRoleType_rule_subType_not_null_scope_permission_subType_null() {
        //prepare data
        AuthorizedService service = buildService("service");
        AuthorizationRule rule = buildRule(ResourceType.REQUEST_TASK, "taskType", "handler", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);

        service.getRules().add(rule);
        rule.getServices().add(service);

        entityManager.persist(rule);
        entityManager.persist(service);

		ResourceScopePermission scope = buildPermissionScope(ResourceType.REQUEST_TASK, null,
				Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
				RoleTypeConstants.REGULATOR);
        entityManager.persist(scope);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.REGULATOR, null, null);

        //assert
        assertThat(result)
                .hasSize(1)
                .containsExactly(
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType("taskType")
                                .handler("handler")
                                .permission(null)
                                .build()
                );
    }

    @Test
    void findRulePermissionsByServiceAndRoleType_rule_subType_null_scope_permission_subType_null() {
        //prepare data
        AuthorizedService service = buildService("service");
        AuthorizationRule rule = buildRule(ResourceType.REQUEST_TASK, null, "handler", Scope.REQUEST_TASK_EXECUTE, RoleTypeConstants.REGULATOR);

        service.getRules().add(rule);
        rule.getServices().add(service);

        entityManager.persist(rule);
        entityManager.persist(service);

		ResourceScopePermission scope = buildPermissionScope(ResourceType.REQUEST_TASK, null,
				Permission.PERM_CA_USERS_EDIT, Scope.REQUEST_TASK_EXECUTE,
				RoleTypeConstants.REGULATOR);
        entityManager.persist(scope);

        //invoke
        List<AuthorizationRuleScopePermission> result = repo.findRulePermissionsByServiceAndRoleTypeAndResourceTypeAndResourceSubType(
                "service", RoleTypeConstants.REGULATOR, null, null);

        //assert
        assertThat(result)
                .hasSize(1)
                .containsExactly(
                        AuthorizationRuleScopePermission.builder()
                                .resourceType(ResourceType.REQUEST_TASK)
                                .resourceSubType(null)
                                .handler("handler")
                                .permission(Permission.PERM_CA_USERS_EDIT)
                                .build()
                );
    }
        
    private AuthorizationRule buildRule(String resourceType, String resourceSubType, String handler, String scope, String roleType) {
        return AuthorizationRule.builder()
                .resourceType(resourceType)
                .resourceSubType(resourceSubType)
                .handler(handler)
                .scope(scope)
                .roleType(roleType)
                .build();
    }

    private AuthorizedService buildService(String name) {
        AuthorizedService service = new AuthorizedService();
        service.setName(name);
        return service;
    }

    private ResourceScopePermission buildPermissionScope(String resourceType, String resourceSubType, String permission, String scope, String roleType) {
        return ResourceScopePermission.builder()
                .resourceType(resourceType)
                .resourceSubType(resourceSubType)
                .permission(permission)
                .scope(scope)
                .roleType(roleType)
                .build();
    }
}
