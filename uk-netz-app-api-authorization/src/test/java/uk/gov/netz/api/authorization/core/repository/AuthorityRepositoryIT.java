package uk.gov.netz.api.authorization.core.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityPermission;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.core.domain.Role;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityRoleDTO;
import uk.gov.netz.api.authorization.rules.domain.ResourceScopePermission;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.AuditConfiguration;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.ACCEPTED;
import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.ACTIVE;
import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.DISABLED;
import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.PENDING;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_ACCOUNT_USERS_EDIT;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import({ObjectMapper.class, AuditConfiguration.class})
class AuthorityRepositoryIT extends AbstractContainerBaseTest {

	@Autowired
	private AuthorityRepository repo;
	
	@Autowired
	private EntityManager entityManager;
	
    @Test
    void findPermissionsByUserId() {
    	createAuthority("regulator_user_1", "regulator", AuthorityStatus.ACTIVE, null, CompetentAuthorityEnum.ENGLAND, null, "regulator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build(),
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build()));
    	
		createAuthority("regulator_user_1", "regulator_admin", AuthorityStatus.ACTIVE, null, CompetentAuthorityEnum.ENGLAND, null, "regulator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build(),
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build()));
		
    	flushAndClear();
		
        final String userId = "regulator_user_1";
        List<String> permissions = repo.findAssignedPermissionsByUserId(userId);
        assertThat(permissions).hasSize(2).containsOnly(
            Permission.PERM_TASK_ASSIGNMENT,
                PERM_ACCOUNT_USERS_EDIT);
    }
    
    @Test
    void findByUserIdAndStatusWhenStatusActive() {
    	createAuthority("operator_user_1", "operator_admin", AuthorityStatus.ACTIVE, 1L, null, null, "operator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build(),
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build()));
    	
    	createAuthority("operator_user_1", "operator", AuthorityStatus.ACTIVE, 1L, null, null, "operator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build()));
    	
    	createAuthority("operator_user_1", "operator_assistant", AuthorityStatus.DISABLED, 1L, null, null, "operator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build()));
		
		flushAndClear();
    	
        final String userId = "operator_user_1";
        List<Authority> authorities = repo.findByUserIdAndStatus(userId, ACTIVE);

        assertThat(authorities).hasSize(2);
    }

    @Test
    void findByUserIdAndStatusAndAuthorityPermissionsAssignedTrueWhenStatusDisabled() {
    	createAuthority("operator_user_1", "operator_admin", AuthorityStatus.ACTIVE, 1L, null, null, "operator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_TASK_ASSIGNMENT).build()));
    	
    	createAuthority("operator_user_1", "operator", AuthorityStatus.DISABLED, 1L, null, null, "operator_user_1", List.of(
    			AuthorityPermission.builder().permission(Permission.PERM_ACCOUNT_USERS_EDIT).build(),
    			AuthorityPermission.builder().permission(Permission.PERM_CA_USERS_EDIT).build()));
    	
		flushAndClear();
		
        final String userId = "operator_user_1";
        List<Authority> authorities = repo.findByUserIdAndStatus(userId, DISABLED);

        assertThat(authorities).hasSize(1);
    }

    @Test
    void findByUserIdAndStatusAndAuthorityPermissionsAssignedTrueForInvalidUser() {
        List<Authority> authorities =
            repo.findByUserIdAndStatus("invalid_user", DISABLED);

        assertTrue(authorities.isEmpty());
    }
    
    @Test
    void findOperatorUserAuthorityRoleListByAccount() {
    	//prepare data
    	String roleOperatorAdminCode = "operator_admin";
    	Role roleOperatorAdmin = createRole(roleOperatorAdminCode, RoleTypeConstants.OPERATOR, "Operator admin");
    	
    	Long account1 = 1L;
    	Long account2 = 2L;
    	
    	createOperatorAuthority("user1", roleOperatorAdminCode, AuthorityStatus.ACTIVE, account1);
    	createOperatorAuthority("user2", roleOperatorAdminCode, AuthorityStatus.ACTIVE, account2);
    	createRegulatorAuthority("user3", "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.ENGLAND);
    	
    	flushAndClear();
    	
    	// invoke
    	List<AuthorityRoleDTO> authorityRoleList = repo.findOperatorUserAuthorityRoleListByAccount(account1);
    	
    	//assert
    	assertThat(authorityRoleList).hasSize(1);
    	assertThat(authorityRoleList.get(0).getUserId()).isEqualTo("user1");
    	assertThat(authorityRoleList.get(0).getRoleName()).isEqualTo(roleOperatorAdmin.getName());
    	assertThat(authorityRoleList.get(0).getRoleCode()).isEqualTo(roleOperatorAdmin.getCode());
    	assertThat(authorityRoleList.get(0).getAuthorityStatus()).isEqualTo(ACTIVE);
    	assertThat(authorityRoleList.get(0).getCreationDate()).isNotNull();
    }

	@Test
	void findOperatorUserAuthorityRoleListByAccountAndStatus() {
		//prepare data
		String roleOperatorAdminCode = "operator_admin";
		Role roleOperatorAdmin = createRole(roleOperatorAdminCode, RoleTypeConstants.OPERATOR, "Operator admin");

		Long account1 = 1L;
		Long account2 = 2L;

		createOperatorAuthority("user1", roleOperatorAdminCode, AuthorityStatus.ACCEPTED, account1);
		createOperatorAuthority("user2", roleOperatorAdminCode, AuthorityStatus.ACTIVE, account2);
		createRegulatorAuthority("user3", "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.ENGLAND);

		flushAndClear();

		// invoke
		List<AuthorityRoleDTO> authorityRoleList = repo
			.findOperatorUserAuthorityRoleListByAccountAndStatus(account1, Set.of(AuthorityStatus.ACCEPTED));

		//assert
		assertThat(authorityRoleList).hasSize(1);
		assertThat(authorityRoleList.get(0).getUserId()).isEqualTo("user1");
		assertThat(authorityRoleList.get(0).getRoleName()).isEqualTo(roleOperatorAdmin.getName());
		assertThat(authorityRoleList.get(0).getRoleCode()).isEqualTo(roleOperatorAdmin.getCode());
		assertThat(authorityRoleList.get(0).getAuthorityStatus()).isEqualTo(ACCEPTED);
		assertThat(authorityRoleList.get(0).getCreationDate()).isNotNull();
	}
    
    @Test
    void findByUserIdAndAccountId() {
    	//prepare data
    	final String user = "user";
    	String roleOperatorAdminCode = "operator_admin";
    	String roleOperatorCode = "operator";
    	
    	Long account1 = 1L;
    	Long account2 = 2L;
    	
    	Authority operatorAdminAuthority = 
    			createOperatorAuthority(user, roleOperatorAdminCode, AuthorityStatus.ACTIVE, account1);
    	createOperatorAuthority(user, roleOperatorCode, AuthorityStatus.ACTIVE, account2);
    	
    	flushAndClear();
    	
    	// invoke
    	Optional<Authority> resultOptional = repo.findByUserIdAndAccountId(user, account1);
    	
    	//assert
    	assertThat(operatorAdminAuthority).isEqualTo(resultOptional.get());
    }
    
    @Test
    void existsByUserIdAndCompetentAuthority() {
    	final String user = "user";
    	final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
    	//assert before
    	boolean result = repo.existsByUserIdAndCompetentAuthority(user, ca);
    	assertThat(result).isFalse();
    	
    	//create authority
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, ca);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndCompetentAuthority(user, ca);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndCompetentAuthorityNotForCAOrNull_different_ca() {
    	final String user = "user";
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, competentAuthority);
    	
    	boolean result = repo.existsByUserIdAndCompetentAuthorityNotForCAOrNull(user, competentAuthority);
    	assertThat(result).isFalse();
    	
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.NORTHERN_IRELAND);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndCompetentAuthorityNotForCAOrNull(user, competentAuthority);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndCompetentAuthorityNotForCAOrNull_null_ca() {
    	final String user = "user";
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, competentAuthority);
    	
    	boolean result = repo.existsByUserIdAndCompetentAuthorityNotForCAOrNull(user, competentAuthority);
    	assertThat(result).isFalse();
    	
    	createOperatorAuthority(user, "code1", AuthorityStatus.ACTIVE, 1L);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndCompetentAuthorityNotForCAOrNull(user, competentAuthority);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndVerificationBodyIdNotForVBOrNull_different_vb() {
    	final String user = "user";
    	Long vb = 1L;
    	Long vb2 = 2L;
    	createVerifierAuthority(user, "verifier_admin", ACTIVE, vb);
    	
    	boolean result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	assertThat(result).isFalse();
    	
    	createVerifierAuthority(user, "verifier_admin", ACTIVE, vb2);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndVerificationBodyIdNotForVBOrNull_null_vb_account() {
    	final String user = "user";
    	Long vb = 1L;
    	createVerifierAuthority(user, "verifier_admin", ACTIVE, vb);
    	
    	boolean result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	assertThat(result).isFalse();
    	
    	createOperatorAuthority(user, "code1", AuthorityStatus.ACTIVE, 1L);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndVerificationBodyIdNotForVBOrNull_null_vb_ca() {
    	final String user = "user";
    	Long vb = 1L;
    	createVerifierAuthority(user, "verifier_admin", ACTIVE, vb);
    	
    	boolean result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	assertThat(result).isFalse();
    	
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.NORTHERN_IRELAND);
    	
    	flushAndClear();
    	
    	result = repo.existsByUserIdAndVerificationBodyIdNotForVBOrNull(user, vb);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndAccountIdIsNull_ca() {
    	String userId = "user";
    	
    	boolean result = repo.existsByUserIdAndAccountIdIsNull(userId);
    	assertThat(result).isFalse();
    	
    	createRegulatorAuthority(userId, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.NORTHERN_IRELAND);
    	flushAndClear();
    	result = repo.existsByUserIdAndAccountIdIsNull(userId);
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserIdAndAccountIdIsNull_vb() {
    	String userId = "user";
    	
    	boolean result = repo.existsByUserIdAndAccountIdIsNull(userId);
    	assertThat(result).isFalse();
    	
    	createVerifierAuthority(userId, "verifier_admin", ACTIVE, 2L);
    	flushAndClear();
    	result = repo.existsByUserIdAndAccountIdIsNull(userId);
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserId_true() {
    	final String user = "user";

    	//create authority
    	createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.ENGLAND);
    	
    	flushAndClear();
    	
    	boolean result = repo.existsByUserId(user);
    	
    	//assert after
    	assertThat(result).isTrue();
    }
    
    @Test
    void existsByUserId_false() {
    	final String user = "user";

    	boolean result = repo.existsByUserId(user);
    	
    	//assert after
    	assertThat(result).isFalse();
    }
    
    @Test
    void deleteByUserIdAndCompetentAuthority() {
    	final String user = "user";
    	final String anotherUser = "anotheruser";
    	Authority auth1 = createRegulatorAuthority(user, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.ENGLAND);
    	Authority auth2 = createRegulatorAuthority(anotherUser, "regulator", AuthorityStatus.ACTIVE, CompetentAuthorityEnum.ENGLAND);
    	
    	repo.deleteByUserId(user);
    	
    	flushAndClear();
    	
    	//assert
    	assertTrue(repo.findById(auth1.getId()).isEmpty());
		assertTrue(repo.findById(auth2.getId()).isPresent());
    }
    
    private Role createRole(String code, String type, String name) {
    	Role role =
    			Role.builder()
    				.code(code)
    				.type(type)
    				.name(name)
    				.build();
    	entityManager.persist(role);
    	return role;
    }

	@Test
	void existsOtherAccountOperatorAdmin_false() {
		String userId = "oper1";
		Long accountId = 1L;
		createOperatorAuthority(userId, "operator_admin", ACTIVE, accountId);

		flushAndClear();

		assertThat(repo.existsOtherAccountOperatorAdmin(userId, accountId)).isFalse();
	}

	@Test
	void existsOtherAccountOperatorAdmin_true() {
		String userId1 = "oper1";
		String userId2 = "oper2";
		Long accountId = 1L;
		createOperatorAuthority(userId1, "operator_admin", ACTIVE, accountId);
		createOperatorAuthority(userId2, "operator_admin", ACTIVE, accountId);

		flushAndClear();

		assertThat(repo.existsOtherAccountOperatorAdmin(userId1, accountId)).isTrue();
	}

    @Test
    void existsOtherVerificationBodyAdmin_false() {
        String userId = "verifier1";
        Long verificationBodyId = 1L;
        createVerifierAuthority(userId, "verifier_admin", ACTIVE, verificationBodyId);

        flushAndClear();

        assertThat(repo.existsOtherVerificationBodyAdmin(userId, verificationBodyId)).isFalse();
    }

    @Test
    void existsOtherVerificationBodyAdmin_true() {
        String userId1 = "oper1";
        String userId2 = "oper2";
        Long verificationBodyId = 1L;
        createVerifierAuthority(userId1, "verifier_admin", ACTIVE, verificationBodyId);
        createVerifierAuthority(userId2, "verifier_admin", ACTIVE, verificationBodyId);

        flushAndClear();

        assertThat(repo.existsOtherVerificationBodyAdmin(userId1, verificationBodyId)).isTrue();
    }
	
	@Test
	void findVerifierUserAuthorityRoleListByVerificationBody() {
		Long vb = 1L;
		String roleVerifierAdminCode = "verifier_admin";
    	createRole(roleVerifierAdminCode, RoleTypeConstants.VERIFIER, "Verifier admin");
		
		createVerifierAuthority("user1", roleVerifierAdminCode, AuthorityStatus.ACTIVE, vb);
		createVerifierAuthority("user2", roleVerifierAdminCode, AuthorityStatus.ACTIVE, vb);
		createVerifierAuthority("user3", roleVerifierAdminCode, AuthorityStatus.ACTIVE, 2L);
		
		flushAndClear();
		
		//invoke
		List<AuthorityRoleDTO> result = repo.findVerifierUserAuthorityRoleListByVerificationBody(vb);
		
		//assert
		assertThat(result).hasSize(2);
		assertThat(result)
				.extracting(AuthorityRoleDTO::getUserId)
				.containsExactlyInAnyOrder("user1", "user2")
				;
	}
	
	@Test
	void findActiveOperatorUsersByAccountAndRoleCode() {
		//prepare data
		String roleCode = AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE;
		Long account1 = 1L;
		Long anotherAccount = 2L;
		
		createOperatorAuthority("user1", roleCode, AuthorityStatus.ACTIVE, account1);
		createOperatorAuthority("user2", roleCode, AuthorityStatus.PENDING, account1);
        createOperatorAuthority("user3", roleCode, AuthorityStatus.ACTIVE, account1);
		createOperatorAuthority("user3", "another_role", AuthorityStatus.ACTIVE, account1);
		createOperatorAuthority("user4", "roleCode", AuthorityStatus.ACTIVE, anotherAccount);
		
		//invoke
		List<String> usersFound = repo.findActiveOperatorUsersByAccountAndRoleCode(account1, roleCode);
		
		//assert
		assertThat(usersFound).containsExactlyInAnyOrder("user1", "user3");
	}

	@Test
	void findByAccountIdAndCodeIn() {
	    String operator = "operator";
	    String consultant = "consultant";
	    String agent = "agent";

	    
        createOperatorAuthority("user1", operator, AuthorityStatus.ACTIVE, 1L);
        createOperatorAuthority("user2", consultant, AuthorityStatus.DISABLED, 1L);
        createOperatorAuthority("user3", agent, AuthorityStatus.ACTIVE, 1L);
        createOperatorAuthority("user4", consultant, AuthorityStatus.ACTIVE, 2L);

        List<Authority> expectedAuthorities = repo.findByAccountIdAndCodeIn(1L, List.of(consultant, agent));

        assertThat(expectedAuthorities).hasSize(2);
        assertThat(expectedAuthorities)
            .extracting(Authority::getUserId)
            .containsExactlyInAnyOrder("user2", "user3");

    }

    @Test
    void findByAccountId() {
        String operator = "operator";
        String consultant = "consultant";
        String verifier = "verifier";


        createOperatorAuthority("user1", operator, AuthorityStatus.ACTIVE, 1L);
        createOperatorAuthority("user2", consultant, AuthorityStatus.DISABLED, 1L);
        createOperatorAuthority("user3", consultant, AuthorityStatus.ACTIVE, 2L);
        createOperatorAuthority("user4", verifier, AuthorityStatus.ACTIVE, 1L);

        List<Authority> expectedAuthorities = repo.findAllByUserIdInAndAccountId(Set.of("user1", "user2"), 1L);

        assertThat(expectedAuthorities).hasSize(2);
        assertThat(expectedAuthorities)
                .extracting(Authority::getUserId)
                .containsExactlyInAnyOrder("user1", "user2");

    }
	
	@Test
    void findRegulatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndCA() {
        createResourceScopePermission(
                ResourceType.REQUEST_TASK, 
                "requestTaskType",
                Scope.REQUEST_TASK_EXECUTE, 
                Permission.PERM_ACCOUNT_USERS_EDIT);
        
        String user1 = "user1";
        Authority authorityUser1England = 
                Authority.builder()
                    .userId(user1)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy(user1)
                    .build();
        authorityUser1England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());
        
        Authority authorityUser1Wales = 
                Authority.builder()
                    .userId(user1)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.WALES)
                    .createdBy(user1)
                    .build();
        authorityUser1Wales
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());

        String user2 = "user2";
        Authority authorityUser2England = 
                Authority.builder()
                    .userId(user2)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                    .createdBy(user2)
                    .build();
        authorityUser2England
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_CA_USERS_EDIT)
                                    .build());
        
        String user3 = "user3";
        Authority authorityUser3Wales = 
                Authority.builder()
                    .userId(user3)
                    .code("regularator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .competentAuthority(CompetentAuthorityEnum.WALES)
                    .createdBy(user3)
                    .build();
        authorityUser3Wales
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());

        entityManager.persist(authorityUser1England);
        entityManager.persist(authorityUser1Wales);
        entityManager.persist(authorityUser2England);
        entityManager.persist(authorityUser3Wales);
        
        
        String resourceType = ResourceType.REQUEST_TASK;
        String resourceSubType = "requestTaskType";
        String scope = Scope.REQUEST_TASK_EXECUTE;
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        List<String> usersFound = repo.findRegulatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndCA(resourceType, resourceSubType, scope, competentAuthority);
        assertThat(usersFound).containsExactly(user1);
    }
	
	@Test
    void findVerifierUsersWithScopeOnResourceTypeAndResourceSubTypeAndVerificationBodyId() {
        createResourceScopePermission(
                ResourceType.REQUEST_TASK,
                "requestTaskType",
                Scope.REQUEST_TASK_EXECUTE, 
                Permission.PERM_CA_USERS_EDIT);
        
        String user1 = "user1";
        Long verificationBodyId1 = 1L;
        Authority authorityUser1Vb1 = 
                Authority.builder()
                    .userId(user1)
                    .code("verifier_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .verificationBodyId(verificationBodyId1)
                    .createdBy(user1)
                    .build();
        authorityUser1Vb1
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_CA_USERS_EDIT)
                                    .build());
        
        String user2 = "user2";
        Long verificationBodyId2 = 2L;
        Authority authorityUser2Vb2 = 
                Authority.builder()
                    .userId(user2)
                    .code("verifier_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .verificationBodyId(verificationBodyId2)
                    .createdBy(user2)
                    .build();
        authorityUser2Vb2
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_CA_USERS_EDIT)
                                    .build());
        
        entityManager.persist(authorityUser1Vb1);
        entityManager.persist(authorityUser2Vb2);
        
        
        String resourceType = ResourceType.REQUEST_TASK; 
        String resourceSubType = "requestTaskType";
        String scope = Scope.REQUEST_TASK_EXECUTE; 
        List<String> usersFound = repo.findVerifierUsersWithScopeOnResourceTypeAndResourceSubTypeAndVerificationBodyId(resourceType, resourceSubType, scope, verificationBodyId1);
        assertThat(usersFound).containsExactly(user1);
    }
	
	@Test
    void findOperatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndAccountId() {
        //prepare data
	    createResourceScopePermission(
                ResourceType.REQUEST_TASK,
                "requestTaskType",
                Scope.REQUEST_TASK_EXECUTE, 
                Permission.PERM_ACCOUNT_USERS_EDIT);
	    
	    Long account1 = 1L;
	    Long account2 = 2L;
	    
        String user1 = "user1";
        Authority authorityUser1Account1 = 
                Authority.builder()
                    .userId(user1)
                    .code("operator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account1)
                    .createdBy(user1)
                    .build();
        authorityUser1Account1
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());
        
        String user2 = "user2";
        Authority authorityUser2Account1 = 
                Authority.builder()
                    .userId(user2)
                    .code("operator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account2)
                    .createdBy(user2)
                    .build();
        authorityUser2Account1
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_TASK_ASSIGNMENT)
                                    .build());
        
        String user3 = "user3";
        Authority authorityUser3Account2 = 
                Authority.builder()
                    .userId(user2)
                    .code("operator_admin")
                    .status(AuthorityStatus.ACTIVE)
                    .accountId(account2)
                    .createdBy(user3)
                    .build();
        authorityUser3Account2
            .addPermission(
                    AuthorityPermission.builder()
                                    .permission(Permission.PERM_ACCOUNT_USERS_EDIT)
                                    .build());

        entityManager.persist(authorityUser1Account1);
        entityManager.persist(authorityUser2Account1);
        entityManager.persist(authorityUser3Account2);
        
        flushAndClear();
        
        //execute
        String resourceType = ResourceType.REQUEST_TASK; 
        String resourceSubType = "requestTaskType";
        String scope = Scope.REQUEST_TASK_EXECUTE; 
        List<String> usersFound = repo.findOperatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndAccountId(resourceType, resourceSubType, scope, account1);

        //assert
        assertThat(usersFound).containsExactly(user1);
    }

    @Test
    void findByUuidAndStatus() {
        final String uuid = "uuid";
        final String userId = "userId";

        Authority expectedAuthority = Authority.builder().userId(userId).uuid(uuid).status(PENDING).createdBy(userId).build();
        Authority anotherAuthority = Authority.builder().userId(userId).uuid("anotherUuid").status(ACTIVE).createdBy(userId).build();

        entityManager.persist(expectedAuthority);
        entityManager.persist(anotherAuthority);
        flushAndClear();

        Optional<Authority> optionalActualAuthority = repo.findByUuidAndStatus(uuid, PENDING);

        assertThat(optionalActualAuthority).isNotEmpty();
        assertEquals(expectedAuthority, optionalActualAuthority.get());
    }

    @Test
    void findByUuidAndStatus_returns_empty_optional() {
        final String uuid = "uuid";
        final String userId = "userId";

        Authority authority = Authority.builder().userId(userId).uuid("anotherUuid").status(ACTIVE).createdBy(userId).build();

        entityManager.persist(authority);
        flushAndClear();

        Optional<Authority> optionalActualAuthority = repo.findByUuidAndStatus(uuid, PENDING);

        assertThat(optionalActualAuthority).isEmpty();
    }

    @Test
    void findByCompetentAuthority() {
        Authority expectedRegulatorAuthority =
            createRegulatorAuthority("reg1", "regulator", ACTIVE, CompetentAuthorityEnum.ENGLAND);
        createRegulatorAuthority("reg2", "regulator", ACTIVE, CompetentAuthorityEnum.WALES);
        flushAndClear();

        // invoke
        List<Authority> actualAuthorities = repo.findByCompetentAuthority(CompetentAuthorityEnum.ENGLAND);

        //assert
        assertThat(actualAuthorities).hasSize(1);
        assertEquals(expectedRegulatorAuthority, actualAuthorities.get(0));
    }

    @Test
    void findByCompetentAuthorityAndStatusNot() {
        Authority expectedRegulatorAuthority = createRegulatorAuthority("reg1", "regulator", ACTIVE, CompetentAuthorityEnum.ENGLAND);
        createRegulatorAuthority("reg2", "regulator", PENDING, CompetentAuthorityEnum.ENGLAND);
        createRegulatorAuthority("reg3", "regulator", ACTIVE, CompetentAuthorityEnum.WALES);
        flushAndClear();

        // invoke
        List<Authority> actualAuthorities = repo.findByCompetentAuthorityAndStatusNot(CompetentAuthorityEnum.ENGLAND, PENDING);

        //assert
        assertThat(actualAuthorities).hasSize(1);
        assertEquals(expectedRegulatorAuthority, actualAuthorities.get(0));
    }

    @Test
    void findNonPendingOperatorUserAuthorityRoleListByAccount() {
        String roleOperatorAdminCode = "operator_admin";
        Role roleOperatorAdmin = createRole(roleOperatorAdminCode, RoleTypeConstants.OPERATOR, "Operator admin");

        createOperatorAuthority("user1", roleOperatorAdminCode, ACTIVE, 1L);
        createOperatorAuthority("user2", roleOperatorAdminCode, PENDING, 2L);
        createOperatorAuthority("user3", roleOperatorAdminCode, ACTIVE, 3L);

        flushAndClear();

        // invoke
        List<AuthorityRoleDTO> actualAuthorityRoleList =
            repo.findNonPendingOperatorUserAuthorityRoleListByAccount(1L);

        //assert
        assertThat(actualAuthorityRoleList).hasSize(1);
        assertThat(actualAuthorityRoleList.get(0).getUserId()).isEqualTo("user1");
        assertThat(actualAuthorityRoleList.get(0).getRoleName()).isEqualTo(roleOperatorAdmin.getName());
        assertThat(actualAuthorityRoleList.get(0).getRoleCode()).isEqualTo(roleOperatorAdmin.getCode());
        assertThat(actualAuthorityRoleList.get(0).getAuthorityStatus()).isEqualTo(ACTIVE);
        assertThat(actualAuthorityRoleList.get(0).getCreationDate()).isNotNull();
    }

    @Test
    void findNonPendingVerifierUserAuthorityRoleListByVerificationBody() {
        String roleVerifierAdminCode = "verifier_admin";
        Role verifierAdminRole = createRole(roleVerifierAdminCode, RoleTypeConstants.VERIFIER, "Verifier admin");

        createVerifierAuthority("user1", roleVerifierAdminCode, ACTIVE, 1L);
        createVerifierAuthority("user2", roleVerifierAdminCode, PENDING, 1L);
        createVerifierAuthority("user3", roleVerifierAdminCode,ACTIVE, 2L);

        flushAndClear();

        //invoke
        List<AuthorityRoleDTO> actualAuthorityRoleList =
            repo.findNonPendingVerifierUserAuthorityRoleListByVerificationBody(1L);

        //assert
        assertThat(actualAuthorityRoleList).hasSize(1);
        assertThat(actualAuthorityRoleList.get(0).getUserId()).isEqualTo("user1");
        assertThat(actualAuthorityRoleList.get(0).getRoleName()).isEqualTo(verifierAdminRole.getName());
        assertThat(actualAuthorityRoleList.get(0).getRoleCode()).isEqualTo(verifierAdminRole.getCode());
        assertThat(actualAuthorityRoleList.get(0).getAuthorityStatus()).isEqualTo(ACTIVE);
        assertThat(actualAuthorityRoleList.get(0).getCreationDate()).isNotNull();
    }

    @Test
    void findByAccountIds() {
        String roleOperatorAdminCode = "operator_admin";
        createRole(roleOperatorAdminCode, RoleTypeConstants.OPERATOR, "Operator admin");

        createOperatorAuthority("user1", roleOperatorAdminCode, ACTIVE, 1L);
        createOperatorAuthority("user2", roleOperatorAdminCode, PENDING, 2L);
        createOperatorAuthority("user3", roleOperatorAdminCode, ACTIVE, 3L);

        flushAndClear();

        // invoke
        List<Authority> authorities =
            repo.findByAccountIdIn(List.of(1L, 2L, 3L));

        //assert
        assertThat(authorities).hasSize(3);
        assertThat(authorities.get(0).getUserId()).isEqualTo("user1");
        assertThat(authorities.get(0).getAuthority()).isEqualTo(AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE);
        assertThat(authorities.get(0).getCreationDate()).isNotNull();
        assertThat(authorities.get(0).getStatus()).isEqualTo(ACTIVE);
        assertThat(authorities.get(0).getAccountId()).isNotNull();
        assertThat(authorities.get(0).getCompetentAuthority()).isNull();
    }
    
	private ResourceScopePermission createResourceScopePermission(
            String resourceType, String resourceSubType, String scope, String permission) {
        ResourceScopePermission resource = ResourceScopePermission.builder()
            .resourceType(resourceType)    
            .resourceSubType(resourceSubType)
            .scope(scope)
            .permission(permission)
            .build();
        entityManager.persist(resource);
        return resource;
    }
	
    private Authority createOperatorAuthority(String userId, String code, AuthorityStatus authorityStatus, Long accountId) {
		return createAuthority(userId, code, authorityStatus, accountId, null, null, userId, Collections.emptyList());
	}
    
    private Authority createRegulatorAuthority(String userId, String code, AuthorityStatus authorityStatus, CompetentAuthorityEnum ca) {
		return createAuthority(userId, code, authorityStatus, null, ca, null, userId, Collections.emptyList());
	}
    
    private Authority createVerifierAuthority(String userId, String code, AuthorityStatus authorityStatus, Long verificationBodyId) {
		return createAuthority(userId, code, authorityStatus, null, null, verificationBodyId, userId, Collections.emptyList());
	}
	
	private Authority createAuthority(String userId, String code, AuthorityStatus authorityStatus, Long accountId,
			CompetentAuthorityEnum ca, Long verificationBodyId, String createdBy, List<AuthorityPermission> permissions) {
		Authority authority = Authority.builder()
				.userId(userId)
				.code(code)
				.status(authorityStatus)
				.accountId(accountId)
				.competentAuthority(ca)
				.verificationBodyId(verificationBodyId)
				.createdBy(createdBy).build();
		
		permissions.forEach(authority::addPermission);

		entityManager.persist(authority);
		return authority;
	}
	
	private void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}
}
