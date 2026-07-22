package uk.gov.netz.api.user.core.service.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.AuthorityConstants;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.user.core.service.auth.UserAuthService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactAccountIdsServiceTest {

    private static final String USER_ID = "user-1";
    private static final String EMAIL = "contact@example.com";

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private ContactAccountIdsService contactAccountIdsService;

    @Test
    void nullEmail_returnsEmptySetWithoutLookup() {
        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(null)).isEmpty();

        verifyNoInteractions(userAuthService, authorityRepository);
    }

    @Test
    void blankEmail_returnsEmptySetWithoutLookup() {
        assertThat(contactAccountIdsService.resolveAccountIdsByEmail("   ")).isEmpty();

        verifyNoInteractions(userAuthService, authorityRepository);
    }

    @Test
    void unknownEmail_returnsEmptySet() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).isEmpty();

        verifyNoInteractions(authorityRepository);
    }

    @Test
    void knownEmail_noAuthorities_returnsEmptySet() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of());

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).isEmpty();
    }

    @Test
    void includesActiveAuthorityAccountId() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of(
                authority(AuthorityConstants.OPERATOR_ROLE_CODE, AuthorityStatus.ACTIVE, 1L)));

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).containsExactly(1L);
    }

    @Test
    void includesDisabledAuthorityAccountId() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of(
                authority(AuthorityConstants.OPERATOR_ROLE_CODE, AuthorityStatus.DISABLED, 2L)));

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).containsExactly(2L);
    }

    @Test
    void includesPendingAndAcceptedAuthorityAccountIds() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of(
                authority(AuthorityConstants.OPERATOR_ROLE_CODE, AuthorityStatus.PENDING, 3L),
                authority(AuthorityConstants.OPERATOR_ADMIN_ROLE_CODE, AuthorityStatus.ACCEPTED, 4L)));

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void includesAnyAuthorityCodeWithNonNullAccountId() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of(
                authority(AuthorityConstants.OPERATOR_ROLE_CODE, AuthorityStatus.ACTIVE, 1L),
                authority(AuthorityConstants.VERIFIER_ADMIN_ROLE_CODE, AuthorityStatus.ACTIVE, 99L),
                authority(AuthorityConstants.THIRD_PARTY_DATA_PROVIDER, AuthorityStatus.DISABLED, 100L)));

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).containsExactlyInAnyOrder(1L, 99L, 100L);
    }

    @Test
    void excludesNullAccountId() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.of(userInfo()));
        when(authorityRepository.findByUserId(USER_ID)).thenReturn(List.of(
                authority(AuthorityConstants.OPERATOR_ROLE_CODE, AuthorityStatus.ACTIVE, 1L),
                regulatorAuthority()));

        assertThat(contactAccountIdsService.resolveAccountIdsByEmail(EMAIL)).containsExactly(1L);
    }

    @Test
    void trimsEmailBeforeLookup() {
        when(userAuthService.getUserByEmail(EMAIL)).thenReturn(Optional.empty());

        contactAccountIdsService.resolveAccountIdsByEmail("  " + EMAIL + "  ");

        verify(userAuthService).getUserByEmail(eq(EMAIL));
    }

    private static UserInfoDTO userInfo() {
        return UserInfoDTO.builder().userId(USER_ID).email(EMAIL).build();
    }

    private static Authority authority(String code, AuthorityStatus status, Long accountId) {
        return Authority.builder()
                .userId(USER_ID)
                .code(code)
                .status(status)
                .accountId(accountId)
                .createdBy("test")
                .build();
    }

    private static Authority regulatorAuthority() {
        return Authority.builder()
                .userId(USER_ID)
                .code("regulator")
                .status(AuthorityStatus.ACTIVE)
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .createdBy("test")
                .build();
    }
}
