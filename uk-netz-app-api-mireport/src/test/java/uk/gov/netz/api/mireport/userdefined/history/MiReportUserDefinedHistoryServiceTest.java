package uk.gov.netz.api.mireport.userdefined.history;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedHistoryServiceTest {

    @InjectMocks
    private MiReportUserDefinedHistoryService service;

    @Mock
    private MiReportUserDefinedHistoryRepository repository;

    @Mock
    private MiReportUserDefinedHistoryMapper mapper;

    @Test
    void recordCreate_mapsWithCreateTypeAndNoReason_andSaves() {
        final String fullName = "John Doe";
        final AppUser appUser = AppUser.builder().firstName("John").lastName("Doe").build();
        final MiReportUserDefinedEntity report = MiReportUserDefinedEntity.builder().id(1L).build();
        final MiReportUserDefinedHistoryEntity mapped = new MiReportUserDefinedHistoryEntity();

        when(mapper.toMiReportUserDefinedHistoryEntity(
                report, MiReportUserDefinedChangeType.CREATE, fullName, null))
                .thenReturn(mapped);

        service.recordCreate(appUser, report);

        verify(mapper).toMiReportUserDefinedHistoryEntity(
                report, MiReportUserDefinedChangeType.CREATE, fullName, null);
        verify(repository).save(mapped);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void recordUpdate_mapsWithUpdateTypeAndReason_andSaves() {
        final String fullName = "John Doe";
        final String reason = "test";
        final AppUser appUser = AppUser.builder().firstName("John").lastName("Doe").build();
        final MiReportUserDefinedEntity report = MiReportUserDefinedEntity.builder().id(2L).build();
        final MiReportUserDefinedHistoryEntity mapped = new MiReportUserDefinedHistoryEntity();

        when(mapper.toMiReportUserDefinedHistoryEntity(
                report, MiReportUserDefinedChangeType.UPDATE, fullName, reason))
                .thenReturn(mapped);

        service.recordUpdate(appUser, report, reason);

        verify(mapper).toMiReportUserDefinedHistoryEntity(
                report, MiReportUserDefinedChangeType.UPDATE, fullName, reason);
        verify(repository).save(mapped);
        verifyNoMoreInteractions(mapper, repository);
    }

    @Test
    void findByMiReportUserDefinedId_returnsMappedResults() {
        final Long reportId = 5L;
        final MiReportUserDefinedHistoryEntity entity = new MiReportUserDefinedHistoryEntity();
        final MiReportUserDefinedHistoryDTO dto = new MiReportUserDefinedHistoryDTO();
        final Page<MiReportUserDefinedHistoryEntity> page =
                new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);

        when(repository.findByMiReportId(eq(reportId), any(Pageable.class))).thenReturn(page);
        when(mapper.toMiReportUserDefinedHistoryDTO(entity)).thenReturn(dto);

        final MiReportUserDefinedHistoryResults result = service.findByMiReportUserDefinedId(reportId, 0, 10);

        assertThat(result.getResults()).containsExactly(dto);
        assertThat(result.getTotal()).isEqualTo(1L);
        verify(repository).findByMiReportId(eq(reportId), any(Pageable.class));
        verify(mapper).toMiReportUserDefinedHistoryDTO(entity);
    }

    @Test
    void findByMiReportUserDefinedId_whenEmpty_returnsEmptyResults() {
        final Long reportId = 5L;
        final Page<MiReportUserDefinedHistoryEntity> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(repository.findByMiReportId(eq(reportId), any(Pageable.class))).thenReturn(emptyPage);

        final MiReportUserDefinedHistoryResults result = service.findByMiReportUserDefinedId(reportId, 0, 10);

        assertThat(result.getResults()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);
        verifyNoInteractions(mapper);
    }
}