package uk.gov.netz.api.authorization.operator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityRoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthoritiesDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthorityDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.transform.AuthorityMapper;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.AccountAuthorizationResourceService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorAuthorityQueryServiceTest {

    @InjectMocks
    private OperatorAuthorityQueryService service;
    
    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private AccountAuthorizationResourceService accountAuthorizationResourceService;
	@Mock
	private  AuthorityMapper authorityMapper;
    
    @Test
    void getAccountAuthorities_has_edit_user_scope_on_account() {
        AppUser authUser = new AppUser();
        Long accountId = 1L;
        String user = "user";

        List<AuthorityRoleDTO> authorityRoles = List.of(AuthorityRoleDTO.builder().userId(user).build());
        UserAuthorityDTO accountOperatorAuthority =
            UserAuthorityDTO.builder().userId(user).build();


        when(accountAuthorizationResourceService.hasUserScopeToAccount(authUser, accountId, Scope.EDIT_USER)).thenReturn(true);
        when(authorityRepository.findOperatorUserAuthorityRoleListByAccount(accountId)).thenReturn(authorityRoles);

        UserAuthoritiesDTO result = service.getAccountAuthorities(authUser, accountId);

        assertTrue(result.isEditable());
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().get(0)).isEqualTo(accountOperatorAuthority);

        verify(accountAuthorizationResourceService, times(1))
                .hasUserScopeToAccount(authUser, accountId, Scope.EDIT_USER);
        verify(authorityRepository, times(1))
                .findOperatorUserAuthorityRoleListByAccount(accountId);
        verifyNoMoreInteractions(authorityRepository);
    }

    @Test
    void getAccountAuthorities_has_not_edit_user_scope_on_account() {
        AppUser authUser = new AppUser();
        Long accountId = 1L;
        String user = "user";

        List<AuthorityRoleDTO> authorityRoles = List.of(AuthorityRoleDTO.builder().userId(user).build());
        UserAuthorityDTO accountOperatorAuthority =
            UserAuthorityDTO.builder().userId(user).build();


        when(accountAuthorizationResourceService.hasUserScopeToAccount(authUser, accountId, Scope.EDIT_USER)).thenReturn(false);
        when(authorityRepository.findNonPendingOperatorUserAuthorityRoleListByAccount(accountId)).thenReturn(authorityRoles);

        UserAuthoritiesDTO result = service.getAccountAuthorities(authUser, accountId);

        assertFalse(result.isEditable());
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().get(0)).isEqualTo(accountOperatorAuthority);

        verify(accountAuthorizationResourceService, times(1))
            .hasUserScopeToAccount(authUser, accountId, Scope.EDIT_USER);
        verify(authorityRepository, times(1))
            .findNonPendingOperatorUserAuthorityRoleListByAccount(accountId);
        verifyNoMoreInteractions(authorityRepository);
    }
    
    @Test
	void existsNonPendingAuthorityForAccount_Exists() {
		String userId = "userId";
		Long accountId = 1L;
		Authority authority = Authority.builder()
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();
		
		when(authorityRepository.findByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authority));
		
		boolean result = service.existsNonPendingAuthorityForAccount(userId, accountId);
		
		assertThat(result).isTrue();
		
		verify(authorityRepository, times(1)).findByUserIdAndAccountId(userId, accountId);
	}
	
	@Test
	void existsNonPendingAuthorityForAccount_not_Exists() {
		String userId = "userId";
		Long accountId = 1L;
		Authority authority = Authority.builder()
				.accountId(accountId)
				.status(AuthorityStatus.PENDING)
				.build();
		
		when(authorityRepository.findByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.of(authority));
		
		boolean result = service.existsNonPendingAuthorityForAccount(userId, accountId);
		
		assertThat(result).isFalse();
		
		verify(authorityRepository, times(1)).findByUserIdAndAccountId(userId, accountId);
	}
	
	@Test
	void existsNonPendingAuthorityForAccount_not_Exists_no_authority() {
		String userId = "userId";
		Long accountId = 1L;
		
		when(authorityRepository.findByUserIdAndAccountId(userId, accountId)).thenReturn(Optional.empty());
		
		boolean result = service.existsNonPendingAuthorityForAccount(userId, accountId);
		
		assertThat(result).isFalse();
		
		verify(authorityRepository, times(1)).findByUserIdAndAccountId(userId, accountId);
	}
	
	@Test
	void existsAuthorityNotForAccount() {
		String userId = "user";
		
		service.existsAuthorityNotForAccount(userId);
		
		verify(authorityRepository, times(1)).existsByUserIdAndAccountIdIsNull(userId);
	}
	
	@Test
    void findOperatorUserAuthorityRoleListByAccount() {
        Long accountId = 1L;

    	List<AuthorityRoleDTO> authorityRoleList = List.of(
		    	AuthorityRoleDTO.builder()
		    		.userId("user1")
		    		.roleName("operator_admin")
		    		.authorityStatus(AuthorityStatus.ACTIVE)
		    		.build());
    	
    	when(authorityRepository.findOperatorUserAuthorityRoleListByAccount(accountId))
    			.thenReturn(authorityRoleList);
    	
    	//invoke
    	List<AuthorityRoleDTO> authorityRoleListFound = service.findOperatorUserAuthorityRoleListByAccount(accountId);
    	
    	//verify
    	verify(authorityRepository, times(1)).findOperatorUserAuthorityRoleListByAccount(accountId);
    	
    	//assert
    	assertThat(authorityRoleListFound.size()).isEqualTo(authorityRoleList.size());
    	assertThat(authorityRoleListFound.get(0)).isEqualTo(authorityRoleList.get(0));
    }
	
	@Test
    void findActiveOperatorUsersByAccount() {
    	Long accountId = 1L;
    	List<String> admins = List.of("admin1");
    	
    	when(authorityRepository.findActiveOperatorUsersByAccountAndRoleCode(accountId, AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE))
    		.thenReturn(admins);
    	
    	List<String> result = service.findActiveOperatorAdminUsersByAccount(accountId);
    	
    	assertThat(result).isEqualTo(admins);
    	verify(authorityRepository, times(1)).findActiveOperatorUsersByAccountAndRoleCode(accountId, AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE);
    }

	@Test
	void findAuthorityByUserIdAndAccountId() {
		Long accountId = 1L;
		String userId = "userId";

		final Authority authority = Authority.builder()
				.id(1L)
				.userId(userId)
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();

		final AuthorityDTO authorityDTO = AuthorityDTO.builder()
				.accountId(accountId)
				.status(AuthorityStatus.ACTIVE)
				.build();

		when(authorityRepository.findByUserIdAndAccountId(userId, accountId))
				.thenReturn(Optional.of(authority));
		when(authorityMapper.toAuthorityDTO(authority)).thenReturn(authorityDTO);

		Optional<AuthorityDTO> result = service.findAuthorityByUserIdAndAccountId(userId, accountId);

		assertThat(result).isPresent();
		assertThat(result.get().getAccountId()).isEqualTo(accountId);
		assertThat(result.get().getStatus()).isEqualTo(AuthorityStatus.ACTIVE);
		verify(authorityRepository, times(1)).findByUserIdAndAccountId(userId, accountId);
	}
}
