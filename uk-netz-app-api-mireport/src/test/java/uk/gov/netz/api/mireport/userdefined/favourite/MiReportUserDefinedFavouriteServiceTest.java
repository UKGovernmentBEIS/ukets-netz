package uk.gov.netz.api.mireport.userdefined.favourite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedFavouriteServiceTest {

    private static final String USER_ID = "user-1";
    private static final Long REPORT_ID = 10L;

    @InjectMocks
    private MiReportUserDefinedFavouriteService service;

    @Mock
    private MiReportUserDefinedFavouriteRepository miReportUserDefinedFavouriteRepository;

    @Mock
    private MiReportUserDefinedRepository miReportUserDefinedRepository;

    private AppUser appUser() {
        return AppUser.builder().userId(USER_ID).build();
    }

    @Test
    void addFavourite_savesWhenReportExistsAndNotAlreadyFavourite() {
        when(miReportUserDefinedRepository.existsById(REPORT_ID)).thenReturn(true);
        when(miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(USER_ID, REPORT_ID))
                .thenReturn(false);

        service.addFavourite(appUser(), REPORT_ID);

        ArgumentCaptor<MiReportUserDefinedFavouriteEntity> captor =
                ArgumentCaptor.forClass(MiReportUserDefinedFavouriteEntity.class);
        verify(miReportUserDefinedFavouriteRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getMiReportId()).isEqualTo(REPORT_ID);
    }

    @Test
    void addFavourite_idempotent_doesNotSaveWhenAlreadyFavourite() {
        when(miReportUserDefinedRepository.existsById(REPORT_ID)).thenReturn(true);
        when(miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(USER_ID, REPORT_ID))
                .thenReturn(true);

        service.addFavourite(appUser(), REPORT_ID);

        verify(miReportUserDefinedFavouriteRepository, never()).save(any());
    }

    @Test
    void addFavourite_throwsWhenReportDoesNotExist() {
        when(miReportUserDefinedRepository.existsById(REPORT_ID)).thenReturn(false);

        final BusinessException be = assertThrows(BusinessException.class,
                () -> service.addFavourite(appUser(), REPORT_ID));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, be.getErrorCode());
        verifyNoInteractions(miReportUserDefinedFavouriteRepository);
    }

    @Test
    void removeFavourite_delegatesToRepository() {
        service.removeFavourite(appUser(), REPORT_ID);

        verify(miReportUserDefinedFavouriteRepository).deleteByUserIdAndMiReportId(USER_ID, REPORT_ID);
    }

    @Test
    void isFavourite_returnsTrueWhenExists() {
        when(miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(USER_ID, REPORT_ID))
                .thenReturn(true);

        assertThat(service.isFavourite(appUser(), REPORT_ID)).isTrue();
    }

    @Test
    void isFavourite_returnsFalseWhenNotExists() {
        when(miReportUserDefinedFavouriteRepository.existsByUserIdAndMiReportId(USER_ID, REPORT_ID))
                .thenReturn(false);

        assertThat(service.isFavourite(appUser(), REPORT_ID)).isFalse();
    }
}
