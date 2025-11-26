package uk.gov.netz.api.authorization.regulator.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityDeletionService {

    private final AuthorityRepository authorityRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void deleteCurrentRegulatorUser(AppUser authUser) {
        deleteRegulator(authUser.getUserId());
    }

    @Transactional
    public void deleteRegulatorUser(String userId, AppUser authUser) {
        CompetentAuthorityEnum ca = authUser.getCompetentAuthority();
        validateUserToBeDeleted(userId, ca);
        deleteRegulator(userId);
    }

    private void deleteRegulator(String userId) {

        final List<Authority> authorities = authorityRepository.findByUserId(userId);
        if (ObjectUtils.isEmpty(authorities)) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }
        final RegulatorAuthorityDeletionEvent event = RegulatorAuthorityDeletionEvent.builder()
                .userId(userId)
                .build();

        authorityRepository.deleteByUserId(userId);
        eventPublisher.publishEvent(event);
    }

    private void validateUserToBeDeleted(String userId, CompetentAuthorityEnum competentAuthority) {
        if (!authorityRepository.existsByUserIdAndCompetentAuthority(userId, competentAuthority)) {
            throw new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA);
        }
    }
}
