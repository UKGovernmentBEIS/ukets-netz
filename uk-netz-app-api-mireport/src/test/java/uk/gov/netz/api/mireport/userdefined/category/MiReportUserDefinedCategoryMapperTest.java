package uk.gov.netz.api.mireport.userdefined.category;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class MiReportUserDefinedCategoryMapperTest {

    private final MiReportUserDefinedCategoryMapper mapper =
            Mappers.getMapper(MiReportUserDefinedCategoryMapper.class);

    @Test
    void toMiReportUserDefinedCategoryDTO() {
        final MiReportUserDefinedCategoryEntity entity = MiReportUserDefinedCategoryEntity.builder()
                .id(1L)
                .name("test category")
                .enabled(true)
                .build();

        final MiReportUserDefinedCategoryDTO expectedDTO = MiReportUserDefinedCategoryDTO.builder()
                .id(1L)
                .name("test category")
                .build();

        MiReportUserDefinedCategoryDTO actualDTO = mapper.toMiReportUserDefinedCategoryDTO(entity);

        assertThat(actualDTO).isEqualTo(expectedDTO);
    }

    @Test
    void toMiReportUserDefinedCategoryDTO_null() {
        MiReportUserDefinedCategoryDTO actualDTO = mapper.toMiReportUserDefinedCategoryDTO(null);

        assertThat(actualDTO).isNull();
    }
}