package uk.gov.netz.api.authorization.regulator.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthoritiesDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthorityDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityService;
import uk.gov.netz.api.authorization.core.transform.UserAuthorityMapper;
import uk.gov.netz.api.authorization.regulator.domain.AuthorityManagePermissionDTO;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorPermissionsAdapter;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.CompAuthAuthorizationResourceService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegulatorAuthorityQueryService {

    private final AuthorityRepository authorityRepository;
    private final AuthorityService<?> authorityService;
    private final CompAuthAuthorizationResourceService compAuthAuthorizationResourceService;
    private final RegulatorPermissionsAdapter regulatorPermissionsAdapter;

    private final UserAuthorityMapper userAuthorityMapper = Mappers.getMapper(UserAuthorityMapper.class);

    /**
     * Returns the current regulator user permissions. Authenticated user should belong to same CA.
     *
     * @param appUser {@link AppUser}
     * @return {@link AuthorityManagePermissionDTO}
     */
    public AuthorityManagePermissionDTO getCurrentRegulatorUserPermissions(AppUser appUser) {
        List<String> permissions = authorityService.getAuthoritiesByUserId(appUser.getUserId()).stream()
            .map(AuthorityDTO::getAuthorityPermissions)
            .flatMap(List::stream)
            .collect(Collectors.toList());


        return AuthorityManagePermissionDTO.builder()
            .permissions(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(permissions))
            .editable(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(appUser, Scope.EDIT_USER))
            .build();
    }

    /**
     * Returns the regulator user permissions. User being accessed should belong to CA.
     *
     * @param authUser {@link AppUser}
     * @param userId Keycloak user id
     * @return {@link AuthorityManagePermissionDTO}
     */
    public AuthorityManagePermissionDTO getRegulatorUserPermissionsByUserId(AppUser authUser, String userId) {
        CompetentAuthorityEnum ca = authUser.getCompetentAuthority();

        // Validate
        if (!authorityRepository.existsByUserIdAndCompetentAuthority(userId, ca)) {
            throw new BusinessException(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA);
        }

        List<String> permissions = authorityService.getAuthoritiesByUserId(userId).stream()
            .map(AuthorityDTO::getAuthorityPermissions)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        return AuthorityManagePermissionDTO.builder()
            .permissions(regulatorPermissionsAdapter.getPermissionGroupLevelsFromPermissions(permissions))
            .editable(true).build();
    }

    /**
     * Returns information about regulator users and their authorities.
     * Regulator users that are fetched belong to the same competent authority with the {@code appUser}
     * @param appUser the authenticated {@link AppUser}
     * @return {@link UserAuthoritiesDTO}
     */
    public UserAuthoritiesDTO getCaAuthorities(AppUser appUser) {
        boolean hasEditUserScopeOnCa = compAuthAuthorizationResourceService.hasUserScopeToCompAuth(appUser, Scope.EDIT_USER);

        List<Authority> regulatorUserAuthorities = hasEditUserScopeOnCa ?
            findRegulatorUserAuthoritiesByCa(appUser.getCompetentAuthority()) :
            findNonPendingRegulatorUserAuthoritiesByCa(appUser.getCompetentAuthority());

        List<UserAuthorityDTO> caUserAuthorities = regulatorUserAuthorities.stream()
                .map(authority -> userAuthorityMapper.toUserAuthority(authority, hasEditUserScopeOnCa))
                .collect(Collectors.toList());

        return UserAuthoritiesDTO.builder()
                .authorities(caUserAuthorities)
                .editable(hasEditUserScopeOnCa)
                .build();
    }

    private List<Authority> findRegulatorUserAuthoritiesByCa(CompetentAuthorityEnum competentAuthority) {
        return authorityRepository.findByCompetentAuthority(competentAuthority);
    }

    private List<Authority> findNonPendingRegulatorUserAuthoritiesByCa(CompetentAuthorityEnum competentAuthority) {
        return authorityRepository.findByCompetentAuthorityAndStatusNot(competentAuthority, AuthorityStatus.PENDING);
    }
}
