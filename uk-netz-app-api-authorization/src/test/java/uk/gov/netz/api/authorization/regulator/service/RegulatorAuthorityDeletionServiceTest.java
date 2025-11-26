package uk.gov.netz.api.authorization.regulator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Authority;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegulatorAuthorityDeletionServiceTest {

    @InjectMocks
    private RegulatorAuthorityDeletionService regulatorAuthorityDeletionService;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void deleteRegulatorUser_whenPendingOrAccepted_thenPurge() {
        
        final String user = "user";
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca);
        AppUser authUser = buildRegulatorUser("reg1Id", List.of(appAuthority));

        when(authorityRepository.existsByUserIdAndCompetentAuthority(user, ca)).thenReturn(true);
        when(authorityRepository.findByUserId("user")).thenReturn(List.of(Authority.builder().status(AuthorityStatus.ACCEPTED).build()));

        //invoke
        regulatorAuthorityDeletionService.deleteRegulatorUser(user, authUser);

        //verify
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(user, ca);
        verify(authorityRepository, times(1)).deleteByUserId(user);
        verify(eventPublisher, times(1)).publishEvent(
            RegulatorAuthorityDeletionEvent.builder().userId(user).build());
    }
    
    @Test
    void deleteRegulatorUser_whenNotPendingOrAccepted_thenDoNotPurge() {
        final String user = "user";
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca);
        AppUser authUser = buildRegulatorUser("reg1Id", List.of(appAuthority));

        when(authorityRepository.existsByUserIdAndCompetentAuthority(user, ca)).thenReturn(true);
        when(authorityRepository.findByUserId("user")).thenReturn(List.of(Authority.builder().status(AuthorityStatus.ACTIVE).build()));

        //invoke
        regulatorAuthorityDeletionService.deleteRegulatorUser(user, authUser);

        //verify
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(user, ca);
        verify(authorityRepository, times(1)).deleteByUserId(user);
        verify(eventPublisher, times(1)).publishEvent(
            RegulatorAuthorityDeletionEvent.builder().userId(user).build());
    }

    @Test
    void deleteRegulatorUser_deleted_user_not_in_ca() {
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final String user = "user";
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca);
        AppUser authUser = buildRegulatorUser("reg1Id", List.of(appAuthority));

        when(authorityRepository.existsByUserIdAndCompetentAuthority(user, ca)).thenReturn(false);

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> regulatorAuthorityDeletionService.deleteRegulatorUser(user, authUser));

        assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA, businessException.getErrorCode());

        //verify
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(user, ca);
        verify(authorityRepository, never()).deleteByUserId(Mockito.anyString());
        verify(eventPublisher, never()).publishEvent(Mockito.any());
    }

    @Test
    void deleteRegulatorUser_deleted_user_no_authorities_to_ca() {
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final String user = "user";
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca);
        AppUser authUser = buildRegulatorUser("reg1Id", List.of(appAuthority));

        when(authorityRepository.existsByUserIdAndCompetentAuthority(user, ca)).thenReturn(false);

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> regulatorAuthorityDeletionService.deleteRegulatorUser(user, authUser));

        assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_CA, businessException.getErrorCode());

        //verify
        verify(authorityRepository, times(1)).existsByUserIdAndCompetentAuthority(user, ca);
        verify(authorityRepository, never()).deleteByUserId(Mockito.anyString());
        verify(eventPublisher, never()).publishEvent(Mockito.any());
    }

    @Test
    void deleteCurrentRegulatorUser() {
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        AppAuthority appAuthority = createRegulatorAuthority("code1", ca);
        AppUser authUser = buildRegulatorUser("reg1Id", List.of(appAuthority));

        when(authorityRepository.findByUserId("reg1Id")).thenReturn(List.of(Authority.builder().status(AuthorityStatus.ACTIVE).build()));
        
        //invoke
        regulatorAuthorityDeletionService.deleteCurrentRegulatorUser(authUser);

        //verify
        verify(authorityRepository, times(1)).deleteByUserId("reg1Id");
        verify(eventPublisher, times(1)).publishEvent((
            RegulatorAuthorityDeletionEvent.builder().userId("reg1Id").build()));
    }

    private AppUser buildRegulatorUser(String userId, List<AppAuthority> appAuthorities) {

        return AppUser.builder()
            .userId(userId)
            .authorities(appAuthorities)
            .roleType(RoleTypeConstants.REGULATOR)
            .build();
    }

    private AppAuthority createRegulatorAuthority(String code, CompetentAuthorityEnum competentAuthority,
                                                  String...permissions) {
        return AppAuthority.builder()
            .code(code)
            .competentAuthority(competentAuthority)
            .permissions(List.of(permissions))
            .build();
    }

}