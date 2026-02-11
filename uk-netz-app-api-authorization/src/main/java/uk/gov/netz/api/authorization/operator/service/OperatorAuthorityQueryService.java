package uk.gov.netz.api.authorization.operator.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityInfoDTO;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityRoleDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthoritiesDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthorityDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.transform.AuthorityMapper;
import uk.gov.netz.api.authorization.core.transform.UserAuthorityMapper;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.AccountAuthorizationResourceService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OperatorAuthorityQueryService {

    private final AuthorityRepository authorityRepository;
    private final AccountAuthorizationResourceService accountAuthorizationResourceService;
    private final UserAuthorityMapper userAuthorityMapper = Mappers.getMapper(UserAuthorityMapper.class);
    private final AuthorityMapper authorityMapper;

    public UserAuthoritiesDTO getAccountAuthorities(AppUser authUser, Long accountId) {

        boolean hasAuthUserEditUserScopeOnAccount =
            accountAuthorizationResourceService.hasUserScopeToAccount(authUser, accountId, Scope.EDIT_USER);

        List<AuthorityRoleDTO> operatorUserAuthorities = hasAuthUserEditUserScopeOnAccount ?
            findOperatorUserAuthoritiesListByAccount(accountId) :
            findNonPendingOperatorUserAuthoritiesListByAccount(accountId);

        List<UserAuthorityDTO> accountAuthorities = operatorUserAuthorities.stream()
            .map(authorityRole -> userAuthorityMapper.toUserAuthority(authorityRole, true))
            .collect(Collectors.toList());

        return UserAuthoritiesDTO.builder()
                .authorities(accountAuthorities)
                .editable(hasAuthUserEditUserScopeOnAccount)
                .build();
    }

    public List<AuthorityRoleDTO> findOperatorUserAuthorityRoleListByAccountAndStatus(Long accountId, Set<AuthorityStatus> statuses) {
        return authorityRepository.findOperatorUserAuthorityRoleListByAccountAndStatus(accountId, statuses);
    }

    /**
     * Find operator user authorities by account.
     * @param accountId the account id
     * @return the list of operator user authority info along with role info
     */
    public List<AuthorityRoleDTO> findOperatorUserAuthoritiesListByAccount(Long accountId) {
        return authorityRepository.findOperatorUserAuthorityRoleListByAccount(accountId);
    }

    public List<AuthorityInfoDTO> findByAccountIds(List<Long> accountIds) {
        return authorityRepository.findByAccountIdIn(accountIds).stream().map(authorityMapper::toAuthorityInfoDTO).collect(Collectors.toList());
    }

    private List<AuthorityRoleDTO> findNonPendingOperatorUserAuthoritiesListByAccount(Long accountId) {
        return authorityRepository.findNonPendingOperatorUserAuthorityRoleListByAccount(accountId);
    }
    
    public boolean existsNonPendingAuthorityForAccount(String userId, Long accountId) {
    	Optional<Authority> authorityOpt = authorityRepository.findByUserIdAndAccountId(userId, accountId);
		return authorityOpt.isPresent() && 
				authorityOpt.get().getStatus() != AuthorityStatus.PENDING;
    }
    
    public boolean existsAuthorityNotForAccount(String userId) {
		return authorityRepository.existsByUserIdAndAccountIdIsNull(userId);
	}
    
    public List<String> findActiveOperatorAdminUsersByAccount(Long accountId){
        return authorityRepository.findActiveOperatorUsersByAccountAndRoleCode(accountId, AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE);
    }

    public Optional<AuthorityDTO> findAuthorityByUserIdAndAccountId(String userId, Long accountId) {
        return authorityRepository.findByUserIdAndAccountId(userId, accountId)
                .map(authorityMapper::toAuthorityDTO);
    }
}
