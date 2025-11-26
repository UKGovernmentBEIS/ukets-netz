package uk.gov.netz.api.authorization.regulator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityAssignmentService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.common.utils.UuidGenerator;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityService {

    private final AuthorityRepository authorityRepository;
    private final AuthorityAssignmentService authorityAssignmentService;
    
    public boolean existsNonPendingAuthorityForCA(String userId, CompetentAuthorityEnum competentAuthority) {
    	Optional<Authority> authorityOpt = authorityRepository.findByUserIdAndCompetentAuthority(userId, competentAuthority);
		return authorityOpt.isPresent() && 
				authorityOpt.get().getStatus() != AuthorityStatus.PENDING;
    }
    
	public boolean existsAuthorityNotForCA(String userId, CompetentAuthorityEnum competentAuthority) {
		return authorityRepository.existsByUserIdAndCompetentAuthorityNotForCAOrNull(userId, competentAuthority);
	}
    
    @Transactional
    public String createRegulatorAuthorityPermissions(
            AppUser authModificationUser,
            String userId,
            CompetentAuthorityEnum competentAuthority,
            List<String> permissions) {
        Optional<Authority> authorityOpt = authorityRepository.findByUserIdAndCompetentAuthority(userId, competentAuthority);

        Authority authority;
        if (authorityOpt.isEmpty()) {
            //create new pending authority
            authority = createPendingAuthority(competentAuthority, userId, authModificationUser.getUserId(), permissions);
        } else {
            //update existing authority
            authority = authorityOpt.get();
            if (authority.getStatus() != AuthorityStatus.PENDING) {
                throw new BusinessException(ErrorCode.AUTHORITY_USER_UPDATE_NON_PENDING_AUTHORITY_NOT_ALLOWED);
            }
            authority = authorityAssignmentService.updatePendingAuthorityWithNewPermissions(
                    authority, permissions, authModificationUser.getUserId());
        }
        return authority.getUuid();
    }
    
    @Transactional
    public Authority acceptAuthority(Long authorityId) {
        return authorityAssignmentService.updateAuthorityStatus(authorityId, AuthorityStatus.ACCEPTED);
    }
    
    public boolean existsByUserIdAndCompetentAuthority(String userId, CompetentAuthorityEnum competentAuthority) {
        return authorityRepository.existsByUserIdAndCompetentAuthority(userId, competentAuthority);
    }

    private Authority createPendingAuthority(CompetentAuthorityEnum competentAuthority, String user, String authModificationUser,
                                             List<String> permissions) {
        Authority authority = Authority.builder()
            .userId(user)
            .competentAuthority(competentAuthority)
            .status(AuthorityStatus.PENDING)
            .createdBy(authModificationUser)
            .uuid(UuidGenerator.generate())
            .build();

        return authorityAssignmentService.createAuthorityWithPermissions(authority, permissions);
    }
}
