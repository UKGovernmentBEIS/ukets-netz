package uk.gov.netz.api.authorization.core.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityInfoDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.transform.AuthorityMapper;

@RequiredArgsConstructor
public abstract class AuthorityAbstractService<T extends AuthorityDTO> implements AuthorityService<T> {
	
	protected final AuthorityRepository authorityRepository;
	protected final AuthorityMapper authorityMapper;

	@Override
    public List<AuthorityDTO> getAuthoritiesByUserId(String userId) {
        return authorityRepository.findByUserId(userId).stream()
            .map(authorityMapper::toAuthorityDTO)
            .collect(Collectors.toList());
    }

	@Override
    public List<String> findAssignedPermissionsByUserId(String userId) {
        return authorityRepository.findAssignedPermissionsByUserId(userId);
    }

	@Override
    public boolean existsByUserId(String userId) {
        return authorityRepository.existsByUserId(userId);
    }

	@Override
    public boolean existsByUserIdAndAccountId(String userId, Long accountId) {
        return authorityRepository
            .findByUserIdAndAccountId(userId, accountId)
            .isPresent();
    }

	@Override
    public Optional<AuthorityInfoDTO> findAuthorityByUuidAndStatusPending(String uuid) {
        return authorityRepository.findByUuidAndStatus(uuid, AuthorityStatus.PENDING)
            .map(authorityMapper::toAuthorityInfoDTO);
    }

	@Override
    public Map<String, AuthorityStatus> findStatusByUsersAndAccountId(List<String> userIds, Long accountId) {
        return authorityRepository.findStatusByUsersAndAccountId(userIds, accountId);
    }
    
	@Override
    public Map<String, AuthorityStatus> findStatusByUsers(List<String> userIds) {
        return authorityRepository.findStatusByUsers(userIds);
    }
    
	@Override
    public Optional<AuthorityInfoDTO> findAuthorityByUserIdAndAccountId(String userId, Long accountId) {
        return authorityRepository.findByUserIdAndAccountId(userId, accountId)
            .map(authorityMapper::toAuthorityInfoDTO);
    }
    
}
