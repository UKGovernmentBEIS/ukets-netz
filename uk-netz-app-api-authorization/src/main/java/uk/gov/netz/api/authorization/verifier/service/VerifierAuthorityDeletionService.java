package uk.gov.netz.api.authorization.verifier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;

import java.util.List;

import static uk.gov.netz.api.common.exception.ErrorCode.AUTHORITY_USER_IS_NOT_VERIFIER;
import static uk.gov.netz.api.common.exception.ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_VERIFICATION_BODY;

@Service
@RequiredArgsConstructor
public class VerifierAuthorityDeletionService {

    private final AuthorityRepository authorityRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final VerifierAdminExistenceValidator verifierAdminExistenceValidator;

    @Transactional
    public void deleteVerifierAuthority(String userId, AppUser authUser) {
        switch (authUser.getRoleType()) {
            case RoleTypeConstants.REGULATOR:
                deleteVerifierAuthority(userId);
                break;
            case RoleTypeConstants.VERIFIER:
                deleteVerifierAuthority(userId, authUser.getVerificationBodyId());
                break;
            default:
                throw new UnsupportedOperationException(
                    String.format("User with role type %s can not delete verifier user", authUser.getRoleType()));
        }
    }

    @Transactional
    public void deleteVerifierAuthority(String userId, Long verificationBodyId) {
        Authority authority = authorityRepository.findByUserIdAndVerificationBodyId(userId, verificationBodyId)
            .orElseThrow(() -> new BusinessException(AUTHORITY_USER_NOT_RELATED_TO_VERIFICATION_BODY));

        deleteAuthority(authority);
    }

    @Transactional
    public void deleteVerifierAuthorities(Long verificationBodyId) {
        List<Authority> authorities = authorityRepository.findAllByVerificationBodyId(verificationBodyId);
        authorityRepository.deleteAll(authorities);

        authorities.forEach(this::publishVerifierUserDeletedEvent);
    }

    private void deleteVerifierAuthority(String userId) {
        Authority authority = authorityRepository.findByUserIdAndVerificationBodyIdNotNull(userId)
                .orElseThrow(() -> new BusinessException(AUTHORITY_USER_IS_NOT_VERIFIER));

        deleteAuthority(authority);
    }

    private void deleteAuthority(Authority authority) {
        verifierAdminExistenceValidator.validateDeletion(authority);
        authorityRepository.delete(authority);
        publishVerifierUserDeletedEvent(authority);
    }

    private void publishVerifierUserDeletedEvent(Authority authority) {
        eventPublisher.publishEvent(VerifierAuthorityDeletionEvent.builder()
                .userId(authority.getUserId())
                .build());
    }
}
