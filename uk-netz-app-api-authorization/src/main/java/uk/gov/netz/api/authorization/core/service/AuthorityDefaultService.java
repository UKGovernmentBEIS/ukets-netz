package uk.gov.netz.api.authorization.core.service;

import static uk.gov.netz.api.authorization.core.domain.AuthorityStatus.ACTIVE;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.transform.AuthorityMapper;

@Service
public class AuthorityDefaultService extends AuthorityAbstractService<AuthorityDTO> {

	public AuthorityDefaultService(AuthorityRepository authorityRepository, AuthorityMapper authorityMapper) {
		super(authorityRepository, authorityMapper);
	}

    @Override
    public List<AuthorityDTO> getActiveAuthoritiesWithAssignedPermissions(String userId) {
        return authorityRepository.findByUserIdAndStatus(userId, ACTIVE).stream()
            .map(authorityMapper::toAuthorityDTO)
            .collect(Collectors.toList());
    }
    
}
