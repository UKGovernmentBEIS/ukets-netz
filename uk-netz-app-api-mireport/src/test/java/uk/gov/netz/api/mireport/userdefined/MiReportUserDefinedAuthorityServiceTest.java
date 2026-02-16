package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedAuthorityServiceTest {

    @InjectMocks
    private MiReportUserDefinedAuthorityService service;

    @Mock
    private MiReportUserDefinedRepository repository;

    @Test
    void getMiReportCaById() {
        final Long miReportId = 1L;
        when(repository.findById(miReportId))
                .thenReturn(Optional.of(MiReportUserDefinedEntity.builder().competentAuthority(CompetentAuthorityEnum.ENGLAND).build()));

        final CompetentAuthorityEnum actual = service.getMiReportCaById(miReportId);

        assertThat(actual).isEqualTo(CompetentAuthorityEnum.ENGLAND);
        verify(repository).findById(miReportId);
    }

    @Test
    void getMiReportCaById_not_found() {
        final Long miReportId = 1L;
        when(repository.findById(miReportId))
                .thenReturn(Optional.empty());

        final BusinessException be = assertThrows(BusinessException.class,
                () -> service.getMiReportCaById(miReportId));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, be.getErrorCode());
        verify(repository).findById(miReportId);
    }
}
