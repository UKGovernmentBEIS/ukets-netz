package uk.gov.netz.api.mireport.userdefined.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedCategoryServiceTest {

    @InjectMocks
    private MiReportUserDefinedCategoryService service;

    @Mock
    private MiReportUserDefinedCategoryRepository miReportUserDefinedCategoryRepository;

    @Test
    void findAllEnabled() {
        final MiReportUserDefinedCategoryEntity entity1 = MiReportUserDefinedCategoryEntity.builder()
                .id(1L)
                .name("Category 1")
                .enabled(true)
                .build();

        final MiReportUserDefinedCategoryEntity entity2 = MiReportUserDefinedCategoryEntity.builder()
                .id(2L)
                .name("Category 2")
                .enabled(true)
                .build();

        when(miReportUserDefinedCategoryRepository.findAllByEnabledTrueOrderByNameAsc()).thenReturn(List.of(entity1, entity2));

        List<MiReportUserDefinedCategoryDTO> actual = service.findAllEnabled();

        assertThat(actual).hasSize(2).containsExactlyInAnyOrder(
                MiReportUserDefinedCategoryDTO.builder().id(1L).name("Category 1").build(),
                MiReportUserDefinedCategoryDTO.builder().id(2L).name("Category 2").build()
        );
        verify(miReportUserDefinedCategoryRepository).findAllByEnabledTrueOrderByNameAsc();
    }

    @Test
    void findAllEnabled_empty() {
        when(miReportUserDefinedCategoryRepository.findAllByEnabledTrueOrderByNameAsc())
                .thenReturn(List.of());

        List<MiReportUserDefinedCategoryDTO> actual = service.findAllEnabled();

        assertThat(actual).isEmpty();
        verify(miReportUserDefinedCategoryRepository).findAllByEnabledTrueOrderByNameAsc();
    }

    @Test
    void getByIds() {
        final Set<Long> ids = Set.of(1L, 2L);

        final MiReportUserDefinedCategoryEntity entity1 = MiReportUserDefinedCategoryEntity.builder()
                .id(1L)
                .name("Category 1")
                .enabled(true)
                .build();

        final MiReportUserDefinedCategoryEntity entity2 = MiReportUserDefinedCategoryEntity.builder()
                .id(2L)
                .name("Category 2")
                .enabled(true)
                .build();

        when(miReportUserDefinedCategoryRepository.findAllById(ids))
                .thenReturn(List.of(entity1, entity2));

        Set<MiReportUserDefinedCategoryEntity> actual = service.getByIds(ids);

        assertThat(actual).hasSize(2).containsExactlyInAnyOrder(entity1, entity2);
        verify(miReportUserDefinedCategoryRepository).findAllById(ids);
    }

    @Test
    void getByIds_null() {

        Set<MiReportUserDefinedCategoryEntity> actual = service.getByIds(null);

        assertThat(actual).isEmpty();
        verify(miReportUserDefinedCategoryRepository, never()).findAllById(any());
    }

    @Test
    void getByIds_empty() {

        Set<MiReportUserDefinedCategoryEntity> actual = service.getByIds(Set.of());

        assertThat(actual).isEmpty();
        verify(miReportUserDefinedCategoryRepository, never()).findAllById(any());
    }

    @Test
    void getByIds_not_found() {
        final Set<Long> ids = Set.of(1L, 2L);

        final MiReportUserDefinedCategoryEntity entity1 = MiReportUserDefinedCategoryEntity.builder()
                .id(1L)
                .name("Category 1")
                .enabled(true)
                .build();

        when(miReportUserDefinedCategoryRepository.findAllById(ids))
                .thenReturn(List.of(entity1));

        final BusinessException be = assertThrows(BusinessException.class,
                () -> service.getByIds(ids));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, be.getErrorCode());
        verify(miReportUserDefinedCategoryRepository).findAllById(ids);
    }

}