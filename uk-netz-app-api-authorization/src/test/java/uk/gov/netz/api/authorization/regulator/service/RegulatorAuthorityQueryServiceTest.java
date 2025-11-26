package uk.gov.netz.api.authorization.regulator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.core.domain.dto.AuthorityDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthoritiesDTO;
import uk.gov.netz.api.authorization.core.domain.dto.UserAuthorityDTO;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.core.service.AuthorityService;
import uk.gov.netz.api.authorization.regulator.domain.AuthorityManagePermissionDTO;
import uk.gov.netz.api.authorization.regulator.transform.RegulatorPermissionsAdapter;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.CompAuthAuthorizationResourceService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.authorization.core.domain.Permission.PERM_CA_USERS_EDIT;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionGroup.MANAGE_USERS_AND_CONTACTS;
import static uk.gov.netz.api.authorization.regulator.domain.RegulatorPermissionLevel.EXECUTE;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RegulatorAuthorityQueryServiceTest {

    @InjectMocks
    private RegulatorAuthorityQueryService regulatorAuthorityQueryService;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private AuthorityService<?> authorityService;

    @Mock
    private CompAuthAuthorizationResourceService compAuthAuthorizationResourceService;

    @Mock
    private RegulatorPermissionsAdapter regulatorPermissionsAdapter;

    @Test
    void getCaRegulators_no_users() {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, Collections.emptyList());
        AppUser authUser = buildRegulatorUser("reg1Id", appAuthority);

        //invoke
        UserAuthoritiesDTO regulatorUsersAuthoritiesInfoDTO = regulatorAuthorityQueryService.getCaAuthorities(authUser);

        assertEquals(0, regulatorUsersAuthoritiesInfoDTO.getAuthorities().size());

        verify(compAuthAuthorizationResourceService, times(1))
            .hasUserScopeToCompAuth(authUser, Scope.EDIT_USER);
        verify(authorityRepository, times(1))
            .findByCompetentAuthorityAndStatusNot(ca, AuthorityStatus.PENDING);
        verify(authorityRepository, never()).findByCompetentAuthority(any());
    }

    @Test
    void getCaRegulators_auth_user_has_edit_permission_on_ca() {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, List.of(Permission.PERM_CA_USERS_EDIT));
        AppUser authUser = buildRegulatorUser("reg1Id", appAuthority);
        List<Authority> expectedAuthorities =
            List.of(Authority.builder().userId("reg1Id").status(AuthorityStatus.ACTIVE).build(),
                Authority.builder().userId("reg2Id").status(AuthorityStatus.ACTIVE).build());
        UserAuthoritiesDTO expectedRegulatorUsersAuthoritiesInfoDTO = buildUserAuthoritiesDTO(
            List.of(buildUserAuthorityDTO("reg1Id", AuthorityStatus.ACTIVE), buildUserAuthorityDTO("reg2Id", AuthorityStatus.ACTIVE)), true);

        //mock
        when(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(authUser, Scope.EDIT_USER)).thenReturn(true);
        when(authorityRepository.findByCompetentAuthority(ca)).thenReturn(expectedAuthorities);

        //invoke
        UserAuthoritiesDTO actualRegulatorUsersAuthoritiesInfoDTO = regulatorAuthorityQueryService.getCaAuthorities(authUser);

        assertEquals(expectedRegulatorUsersAuthoritiesInfoDTO, actualRegulatorUsersAuthoritiesInfoDTO);

        verify(compAuthAuthorizationResourceService, times(1))
            .hasUserScopeToCompAuth(authUser, Scope.EDIT_USER);
        verify(authorityRepository, times(1)).findByCompetentAuthority(ca);
        verify(authorityRepository, never()).findByCompetentAuthorityAndStatusNot(any(), any());
    }

    @Test
    void getCaRegulators_auth_user_has_not_edit_permission_on_ca() {
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, Collections.emptyList());
        AppUser authUser = buildRegulatorUser("reg1Id", appAuthority);
        List<Authority> expectedAuthorities =
            List.of(Authority.builder().userId("reg1Id").status(AuthorityStatus.ACTIVE).build(),
                Authority.builder().userId("reg2Id").status(AuthorityStatus.ACTIVE).build());
        UserAuthoritiesDTO expectedRegulatorUsersAuthoritiesInfoDTO = buildUserAuthoritiesDTO(
            List.of(buildUserAuthorityDTO("reg1Id", null), buildUserAuthorityDTO("reg2Id", null)), false);

        //mock
        when(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(authUser, Scope.EDIT_USER)).thenReturn(false);
        when(authorityRepository.findByCompetentAuthorityAndStatusNot(ca, AuthorityStatus.PENDING)).thenReturn(expectedAuthorities);

        //invoke
        UserAuthoritiesDTO actualRegulatorUsersAuthoritiesInfoDTO =
            regulatorAuthorityQueryService.getCaAuthorities(authUser);

        assertEquals(expectedRegulatorUsersAuthoritiesInfoDTO, actualRegulatorUsersAuthoritiesInfoDTO);

        verify(compAuthAuthorizationResourceService, times(1))
            .hasUserScopeToCompAuth(authUser, Scope.EDIT_USER);
        verify(authorityRepository, times(1))
            .findByCompetentAuthorityAndStatusNot(ca, AuthorityStatus.PENDING);
        verify(authorityRepository, never()).findByCompetentAuthority(any());
    }

    @Test
    void getCurrentRegulatorUserPermissions() {
        final String authUserId = "authUserId";
        final CompetentAuthorityEnum ca = ENGLAND;
        List<String> permissions = List.of(PERM_CA_USERS_EDIT);
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, permissions);
        AppUser authUser = buildRegulatorUser(authUserId, appAuthority);

        // Mock
        when(authorityService.getAuthoritiesByUserId(authUserId)).thenReturn(
            List.of(AuthorityDTO.builder()
                .competentAuthority(ca)
                .authorityPermissions(permissions)
                .build()));
        when(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(authUser, Scope.EDIT_USER))
            .thenReturn(true);

        // Invoke
        AuthorityManagePermissionDTO actual = regulatorAuthorityQueryService.getCurrentRegulatorUserPermissions(authUser);

        // Assert
        assertThat(actual.isEditable()).isTrue();
        verify(authorityService, times(1)).getAuthoritiesByUserId(authUserId);
        verify(compAuthAuthorizationResourceService, times(1)).hasUserScopeToCompAuth(authUser, Scope.EDIT_USER);
    }

    @Test
    void getCurrentRegulatorUserPermissions_not_editable() {
        final String authUserId = "authUserId";
        final CompetentAuthorityEnum ca = ENGLAND;
        final List<String> permissions = Collections.emptyList();
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, permissions);
        AppUser authUser = buildRegulatorUser(authUserId, appAuthority);

        // Mock
        when(authorityService.getAuthoritiesByUserId(authUserId)).thenReturn(
            List.of(AuthorityDTO.builder()
                .competentAuthority(ca)
                .authorityPermissions(permissions)
                .build()));
        when(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(authUser, Scope.EDIT_USER))
            .thenReturn(false);

        // Invoke
        AuthorityManagePermissionDTO actual = regulatorAuthorityQueryService.getCurrentRegulatorUserPermissions(authUser);

        // Assert
        assertThat(actual.isEditable()).isFalse();
        verify(authorityService, times(1)).getAuthoritiesByUserId(authUserId);
        verify(compAuthAuthorizationResourceService, times(1)).hasUserScopeToCompAuth(authUser, Scope.EDIT_USER);
    }

    @Test
    void getRegulatorUserPermissionsByUserId() {
        final String userId = "userId";
        final CompetentAuthorityEnum ca = ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, Collections.emptyList());
        AppUser authUser = buildRegulatorUser("regUserId", appAuthority);

        //Mock
        when(authorityRepository.existsByUserIdAndCompetentAuthority(userId, ca))
            .thenReturn(true);
        when(authorityService.getAuthoritiesByUserId(userId)).thenReturn(List.of(
            AuthorityDTO.builder()
                .competentAuthority(ca)
                .authorityPermissions(List.of(PERM_CA_USERS_EDIT))
                .build()));

        // Invoke
        AuthorityManagePermissionDTO actual = regulatorAuthorityQueryService.getRegulatorUserPermissionsByUserId(authUser, userId);

        assertThat(actual.isEditable()).isTrue();
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(userId, ca);
        verify(authorityService, times(1)).getAuthoritiesByUserId(userId);
    }

    @Test
    void getRegulatorUserPermissionsByUserId_user_not_ca() {
        final String userId = "userId";
        final CompetentAuthorityEnum ca = ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca, Collections.emptyList());
        AppUser authUser = buildRegulatorUser("regUserId", appAuthority);

        when(authorityRepository.existsByUserIdAndCompetentAuthority(userId, ca))
            .thenReturn(false);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
            () -> regulatorAuthorityQueryService.getRegulatorUserPermissionsByUserId(authUser, userId));

        // Assert
        assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA, businessException.getErrorCode());
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(userId, ca);
        verify(authorityService, never()).getAuthoritiesByUserId(anyString());
    }

    private AppUser buildRegulatorUser(String userId, AppAuthority appAuthority) {
        return AppUser.builder()
            .userId(userId)
            .authorities(List.of(appAuthority))
            .roleType(RoleTypeConstants.REGULATOR)
            .build();
    }

    private AppAuthority createRegulatorAuthority(String code, CompetentAuthorityEnum competentAuthority,
                                                  List<String> permissions) {
        return AppAuthority.builder()
            .code(code)
            .competentAuthority(competentAuthority)
            .permissions(permissions)
            .build();
    }

    private UserAuthorityDTO buildUserAuthorityDTO(String id, AuthorityStatus authorityStatus) {
        return UserAuthorityDTO.builder()
            .userId(id)
            .authorityStatus(authorityStatus)
            .build();
    }

    private UserAuthoritiesDTO buildUserAuthoritiesDTO(List<UserAuthorityDTO> users, boolean editable) {
        return UserAuthoritiesDTO.builder()
            .authorities(users)
            .editable(editable)
            .build();
    }
}
