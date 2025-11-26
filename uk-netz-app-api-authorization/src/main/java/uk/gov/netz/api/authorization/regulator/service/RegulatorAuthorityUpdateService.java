package uk.gov.netz.api.authorization.regulator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityAssignmentService;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel;
import uk.gov.netz.api.authorization.regulator.domain.RegulatorUserUpdateStatusDTO;
import uk.gov.netz.api.authorization.regulator.event.RegulatorUserStatusDisabledEvent;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorPermissionsAdapter;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.CompAuthAuthorizationResourceService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityUpdateService {

    private final AuthorityRepository authorityRepository;
    private final AuthorityAssignmentService authorityAssignmentService;
    private final CompAuthAuthorizationResourceService compAuthAuthorizationResourceService;
    private final RegulatorStatusModificationAllowanceValidator regulatorStatusModificationAllowanceValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final RegulatorPermissionsAdapter regulatorPermissionsAdapter;

    /**
     * Updates the status of regulators.
     * @param regulatorUsers the list of regulators whose status will be updated
     * @param authUser the regulator who performed the update action
     * @return the list of users that have been activated (ACCEPTED -> ACTIVE)
     * @throws BusinessException if any of the regulators does not exist in the database or does not belong
     * to the same competent authority as the actor
     */
    @Transactional
    public List<String> updateRegulatorUsersStatus(
            final List<RegulatorUserUpdateStatusDTO> regulatorUsers, final AppUser authUser) {

        CompetentAuthorityEnum competentAuthority = authUser.getCompetentAuthority();
        regulatorStatusModificationAllowanceValidator.validateUpdate(regulatorUsers, competentAuthority);

        final List<String> activatedRegulators = new ArrayList<>();

        //update authorities
        regulatorUsers.forEach(
                ru -> {
                    Authority authority =
                            authorityRepository.findByUserIdAndCompetentAuthority(ru.getUserId(), competentAuthority)
                                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA));
                    final AuthorityStatus previousStatus = authority.getStatus();
                    authority.setStatus(ru.getAuthorityStatus());
                    if (previousStatus == AuthorityStatus.ACCEPTED && authority.getStatus() == AuthorityStatus.ACTIVE) {
                        activatedRegulators.add(ru.getUserId());
                    }
                }
        );

        //publish event for status disabled users
        regulatorUsers.stream()
                .filter(ru -> AuthorityStatus.DISABLED == ru.getAuthorityStatus())
                .forEach(ru -> eventPublisher.publishEvent(new RegulatorUserStatusDisabledEvent(ru.getUserId())));

        return activatedRegulators;
    }

    @Transactional
    public void updateRegulatorUserPermissions(AppUser appUser, String userId, Map<String, RegulatorPermissionLevel> authorityPermissions) {
        // Validate
        if (!authorityRepository.existsByUserIdAndCompetentAuthority(userId, appUser.getCompetentAuthority())) {
            throw new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA);
        }

        CompetentAuthorityEnum ca = appUser.getCompetentAuthority();
        if (compAuthAuthorizationResourceService.hasUserScopeToCompAuth(appUser, Scope.EDIT_USER)) {
            Authority authority = authorityRepository.findByUserIdAndCompetentAuthority(userId, ca)
                    .orElseThrow(() -> new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA));

            authorityAssignmentService.updateAuthorityWithNewPermissions(authority, regulatorPermissionsAdapter
                    .getPermissionsFromPermissionGroupLevels(authorityPermissions));
        }
    }
}
