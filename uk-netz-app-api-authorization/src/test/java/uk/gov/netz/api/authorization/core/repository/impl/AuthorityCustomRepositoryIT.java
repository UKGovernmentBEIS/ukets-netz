package uk.gov.netz.api.authorization.core.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityPermission;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.AuditConfiguration;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, AuditConfiguration.class})
class AuthorityCustomRepositoryIT extends AbstractContainerBaseTest {

	@Autowired
	private AuthorityCustomRepositoryImpl repo;
	
	@Autowired
	private EntityManager entityManager;
	
	private static final String USER1 = "user1";
	private static final String USER2 = "user2";
    private static final String USER3 = "user3";
	
	@Test
    void findResourceSubTypesRegulatorUserHasScope() {
        //prepare data
        Authority authorityUser1England = 
                Authority.builder()
                    .userId(USER1)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy(USER1)
                    .build();
        authorityUser1England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_TASK_ASSIGNMENT)
                                    .build());
        authorityUser1England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_CA_USERS_EDIT)
                                    .build());
        
        Authority authorityUser1Wales = 
                Authority.builder()
                    .userId(USER1)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.WALES)
                    .createdBy(USER1)
                    .build();
        authorityUser1Wales
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_TASK_ASSIGNMENT)
                                    .build());
        
        Authority authorityUser2England = 
                Authority.builder()
                    .userId(USER2)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy(USER2)
                    .build();
        authorityUser2England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_TASK_ASSIGNMENT)
                                    .build());
        authorityUser2England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());

        entityManager.persist(authorityUser1England);
        entityManager.persist(authorityUser2England);
        entityManager.persist(authorityUser1Wales);
        
        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_VIEW, Permission.PERM_TASK_ASSIGNMENT);
        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_EXECUTE, Permission.PERM_ACCOUNT_USERS_EDIT);

        flushAndClear();
        
        //execute
        Map<CompetentAuthorityEnum, Set<String>> result = repo.findResourceSubTypesRegulatorUserHasScope(USER1, ResourceType.REQUEST_TASK, Scope.REQUEST_TASK_VIEW);
        
        //assert
        assertThat(result)
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                    authorityUser1England.getCompetentAuthority(), Set.of("requestTaskType"),
                    authorityUser1Wales.getCompetentAuthority(), Set.of("requestTaskType")));
    }

    @Test
    void findResourceSubTypesVerifierUserHasScope() {
        // Verifier to test
        Authority authorityUser1 = Authority.builder()
                .userId(USER1)
                .code("verifier_admin")
                .status(AuthorityStatus.ACTIVE)
                .verificationBodyId(1L)
                .createdBy(USER1)
                .build();
        authorityUser1.addPermission(AuthorityPermission.builder()
                .permission(Permission.PERM_TASK_ASSIGNMENT).build());
        authorityUser1.addPermission(AuthorityPermission.builder()
                .permission(Permission.PERM_CA_USERS_EDIT).build());

        // Verifier on same vb
        Authority authorityUser2 = Authority.builder()
                .userId(USER2)
                .code("verifier")
                .status(AuthorityStatus.ACTIVE)
                .createdBy(USER2)
                .build();
        authorityUser2.addPermission(AuthorityPermission.builder()
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build());

        // Verifier on different vb
        Authority authorityUser3 = Authority.builder()
                .userId(USER3)
                .code("verifier_admin")
                .status(AuthorityStatus.ACTIVE)
                .createdBy(USER3)
                .build();
        authorityUser3.addPermission(AuthorityPermission.builder()
                .permission(Permission.PERM_TASK_ASSIGNMENT).build());
        authorityUser3.addPermission(AuthorityPermission.builder()
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT).build());

        entityManager.persist(authorityUser1);
        entityManager.persist(authorityUser2);
        entityManager.persist(authorityUser3);

        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_VIEW, Permission.PERM_TASK_ASSIGNMENT);
        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_EXECUTE, Permission.PERM_ACCOUNT_USERS_EDIT);
        flushAndClear();

        //execute
        Map<Long, Set<String>> result = repo.findResourceSubTypesVerifierUserHasScope(USER1, ResourceType.REQUEST_TASK, Scope.REQUEST_TASK_VIEW);

        //assert
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                1L, Set.of("requestTaskType")));
    }
	
	@Test
    void findOperatorUsersByAccountId() {
        //prepare data
        Long account1 = 1L;
        Long account2 = 2L;
        Authority authorityUser1Account1 = 
                Authority.builder()
                    .userId(USER1)
                    .code("operator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account1)
                    .createdBy(USER1)
                    .build();
        Authority authorityUser2Account2 = 
                Authority.builder()
                    .userId(USER2)
                    .code("operator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account2)
                    .createdBy(USER2)
                    .build();

        entityManager.persist(authorityUser1Account1);
        entityManager.persist(authorityUser2Account2);
        
        flushAndClear();
        
        //execute
        List<String> result = repo.findOperatorUsersByAccountId(account1);
        
        //assert
        assertThat(result)
            .containsExactly(USER1);
    }
	
	@Test
    void findRegulatorUsersByCompetentAuthority() {
        //prepare data
        Authority authorityUser1England = 
                Authority.builder()
                    .userId(USER1)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy(USER1)
                    .build();
        
        Authority authorityUser1Scotland = 
                Authority.builder()
                    .userId(USER2)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                    .createdBy(USER2)
                    .build();

        entityManager.persist(authorityUser1England);
        entityManager.persist(authorityUser1Scotland);
        
        flushAndClear();
        
        //execute
        List<String> result = repo.findRegulatorUsersByCompetentAuthority(CompetentAuthorityEnum.ENGLAND);
        
        //assert
        assertThat(result)
            .containsExactly(USER1);
    }

    @Test
    void findVerifierUsersByVerificationBodyId() {
        //prepare data
        Authority authorityUser1Vb1 = 
                Authority.builder()
                    .userId(USER1)
                    .code("verifier_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .verificationBodyId(1L)
                    .createdBy(USER1)
                    .build();
        
        Authority authorityUser2Vb2 = 
                Authority.builder()
                    .userId(USER2)
                    .code("verifier_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .verificationBodyId(2L)
                    .createdBy(USER2)
                    .build();

        entityManager.persist(authorityUser1Vb1);
        entityManager.persist(authorityUser2Vb2);
        
        flushAndClear();
        
        //execute
        List<String> result = repo.findVerifierUsersByVerificationBodyId(1L);
        
        //assert
        assertThat(result)
            .containsExactly(USER1);
    }

    @Test
    void findStatusByUsers() {

        Authority authorityUser1 =
            Authority.builder()
                .userId("user1")
                .accountId(1L)
                .code("code1")
                .status(AuthorityStatus.ACTIVE)
                .createdBy("ff")
                .build();
        entityManager.persist(authorityUser1);

        Authority authorityUser2 =
            Authority.builder()
                .userId("user2")
                .code("code2")
                .status(AuthorityStatus.DISABLED)
                .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                .createdBy("hh")
                .build();
        entityManager.persist(authorityUser2);

        Authority authorityUser3 =
            Authority.builder()
                .userId("user3")
                .code("code3")
                .status(AuthorityStatus.ACTIVE)
                .verificationBodyId(3L)
                .createdBy("hh")
                .build();
        entityManager.persist(authorityUser3);

        flushAndClear();

        Map<String, AuthorityStatus> result = repo.findStatusByUsers(List.of("user1", "user2", "user3"));

        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
            "user1", AuthorityStatus.ACTIVE,
            "user2", AuthorityStatus.DISABLED,
            "user3", AuthorityStatus.ACTIVE
        ));
    }
	
	@Test
	void findStatusByUsersAndAccountId() {
	    Long account = 1L;
	    Long anotherAccount = 2L;
	    
	    Authority authorityUser1 =
                Authority.builder()
                    .userId("user1")
                    .code("code1")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy("ff")
                    .build();
	    entityManager.persist(authorityUser1);
        
        Authority authorityUser2 = 
                Authority.builder()
                    .userId("user2")
                    .code("code2")
                    .status(AuthorityStatus.DISABLED)
                    .accountId(account)
                    .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                    .createdBy("hh")
                    .build();
        entityManager.persist(authorityUser2);
        
        Authority authorityUser3_1 = 
                Authority.builder()
                    .userId("user3")
                    .code("code3")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account)
                    .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                    .createdBy("hh")
                    .build();
        entityManager.persist(authorityUser3_1);
        
        Authority authorityUser3_2 = 
                Authority.builder()
                    .userId("user3")
                    .code("code3")
                    .status(AuthorityStatus.DISABLED)
                    .accountId(anotherAccount)
                    .competentAuthority(CompetentAuthorityEnum.SCOTLAND)
                    .createdBy("hh")
                    .build();
        entityManager.persist(authorityUser3_2);
        
        flushAndClear();
        
        Map<String, AuthorityStatus> result = repo.findStatusByUsersAndAccountId(List.of("user1", "user2", "user3"), account);
        
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of(
                "user1", AuthorityStatus.ACTIVE,
                "user2", AuthorityStatus.DISABLED,
                "user3", AuthorityStatus.ACTIVE
                ));
	}

	@Test
	void findResourceSubTypesOperatorUserHasScope() {
	    String user1 = "user1";
	    String user2 = "user2";
	    Long account1 = 1L;
	    Long account2 = 2L;
	    Long account3 = 3L;

	    Authority authorityUser1Account1 = Authority.builder()
            .accountId(account1)
            .userId(user1)
            .status(AuthorityStatus.ACTIVE)
            .createdBy(user1)
            .creationDate(LocalDateTime.now())
            .build();
	    authorityUser1Account1.addPermission(
	        AuthorityPermission.builder()
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build()
        );
        authorityUser1Account1.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_TASK_ASSIGNMENT)
                .build()
        );

        entityManager.persist(authorityUser1Account1);

        Authority authorityUser1Account2 = Authority.builder()
            .accountId(account2)
            .userId(user1)
            .status(AuthorityStatus.ACTIVE)
            .createdBy(user1)
            .creationDate(LocalDateTime.now())
            .build();
        authorityUser1Account2.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_CA_USERS_EDIT)
                .build()
        );
        authorityUser1Account2.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build()
        );

        entityManager.persist(authorityUser1Account2);

        Authority authorityUser2Account2 = Authority.builder()
            .accountId(account2)
            .userId(user2)
            .status(AuthorityStatus.ACTIVE)
            .createdBy(user1)
            .creationDate(LocalDateTime.now())
            .build();
        authorityUser2Account2.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_TASK_ASSIGNMENT)
                .build()
        );

        entityManager.persist(authorityUser2Account2);

        Authority authorityUser1Account3 = Authority.builder()
            .accountId(account3)
            .userId(user1)
            .status(AuthorityStatus.PENDING)
            .createdBy(user1)
            .creationDate(LocalDateTime.now())
            .build();
        authorityUser1Account3.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                .build()
        );
        authorityUser1Account3.addPermission(
            AuthorityPermission.builder()
                .permission(Permission.PERM_TASK_ASSIGNMENT)
                .build()
        );

        entityManager.persist(authorityUser1Account3);
        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_VIEW, Permission.PERM_TASK_ASSIGNMENT);
        createResourceScopePermission(ResourceType.REQUEST_TASK, "requestTaskType", Scope.REQUEST_TASK_EXECUTE, Permission.PERM_ACCOUNT_USERS_EDIT);

        flushAndClear();

        Map<Long, Set<String>> result =
            repo.findResourceSubTypesOperatorUserHasScopeByAccounts(USER1, Set.of(account1, account2, account3),
                ResourceType.REQUEST_TASK, Scope.REQUEST_TASK_VIEW);

        assertThat(result)
            .containsExactlyInAnyOrderEntriesOf(
                Map.of(
                    account1, Set.of("requestTaskType")
                )
            );
    }
	
    private void createResourceScopePermission(String resourceType, String resourceSubType, String scope, String permission) {
        ResourceScopePermission resource = ResourceScopePermission.builder()
            .resourceType(resourceType)    
            .resourceSubType(resourceSubType)
            .scope(scope)
            .permission(permission)
            .build();
        entityManager.persist(resource);
    }
	
	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
