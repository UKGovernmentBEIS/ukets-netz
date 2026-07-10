package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryMapper;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MiReportUserDefinedMapperTest {

    private final MiReportUserDefinedMapper mapper = Mappers.getMapper(MiReportUserDefinedMapper.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "miReportUserDefinedCategoryMapper",
                Mappers.getMapper(MiReportUserDefinedCategoryMapper.class));
    }

    @Test
    void toMiReportUserDefinedEntity() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final Set<MiReportUserDefinedCategoryDTO> categoryDtos = Set.of(
                MiReportUserDefinedCategoryDTO.builder().id(1L).build(),
                MiReportUserDefinedCategoryDTO.builder().id(2L).build()
        );

        final Set<MiReportUserDefinedCategoryEntity> categories = Set.of(
                MiReportUserDefinedCategoryEntity.builder().id(1L).name("test1").enabled(true).build(),
                MiReportUserDefinedCategoryEntity.builder().id(2L).name("test2").enabled(true).build(),
                MiReportUserDefinedCategoryEntity.builder().id(3L).name("test3").enabled(true).build()
        );

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .categories(categoryDtos)
                .build();

        MiReportUserDefinedEntity entity = mapper.toMiReportUserDefinedEntity(miReportQueryDTO, categories, CompetentAuthorityEnum.ENGLAND, userId);

        assertThat(entity.getReportName()).isEqualTo(reportName);
        assertThat(entity.getDescription()).isEqualTo(description);
        assertThat(entity.getQueryDefinition()).isEqualTo(queryDefinition);
        assertThat(entity.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.ENGLAND);
        assertThat(entity.getCreatedBy()).isEqualTo(userId);
        assertThat(entity.getCategories()).isEqualTo(categories);
    }

    @Test
    void toMiReportUserDefinedDTO() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);
        final Set<MiReportUserDefinedCategoryDTO> categoryDtos = Set.of(
                MiReportUserDefinedCategoryDTO.builder().id(1L).name("test1").build(),
                MiReportUserDefinedCategoryDTO.builder().id(2L).name("test2").build()
        );
        final Set<MiReportUserDefinedCategoryEntity> categories = Set.of(
                MiReportUserDefinedCategoryEntity.builder().id(1L).name("test1").enabled(true).build(),
                MiReportUserDefinedCategoryEntity.builder().id(2L).name("test2").enabled(true).build()
        );

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .lastUpdatedOn(lastUpdatedOn)
                .categories(categories)
                .build();

        final MiReportUserDefinedDTO expectedDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .categories(categoryDtos)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        MiReportUserDefinedDTO actualDTO = mapper.toMiReportUserDefinedDTO(miReportQueryEntity);

        assertThat(actualDTO).isEqualTo(expectedDTO);
    }

    @Test
    void toMiReportUserDefinedInfoDTO() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);
        final Set<MiReportUserDefinedCategoryEntity> categories = Set.of(
                MiReportUserDefinedCategoryEntity.builder().id(1L).name("test1").enabled(true).build(),
                MiReportUserDefinedCategoryEntity.builder().id(2L).name("test2").enabled(true).build(),
                MiReportUserDefinedCategoryEntity.builder().id(3L).name("test3").enabled(true).build()
        );

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .lastUpdatedOn(lastUpdatedOn)
                .categories(categories)
                .build();

        MiReportUserDefinedInfoDTO actualDTO = mapper.toMiReportUserDefinedInfoDTO(miReportQueryEntity);

        assertThat(actualDTO.getId()).isEqualTo(queryId);
        assertThat(actualDTO.getReportName()).isEqualTo(reportName);
        assertThat(actualDTO.getDescription()).isEqualTo(description);
        assertThat(actualDTO.getCategories())
                .containsExactlyInAnyOrder(
                        MiReportUserDefinedCategoryDTO.builder().id(1L).name("test1").build(),
                        MiReportUserDefinedCategoryDTO.builder().id(2L).name("test2").build(),
                        MiReportUserDefinedCategoryDTO.builder().id(3L).name("test3").build()
                );
    }

    @Test
    void toMiReportUserDefinedInfoDTO_nullCategories() {
        final Long queryId = 1L;
        final String reportName = "test report name";
        final String description = "test description";

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .reportName(reportName)
                .description(description)
                .build();

        MiReportUserDefinedInfoDTO actualDTO = mapper.toMiReportUserDefinedInfoDTO(miReportQueryEntity);

        assertThat(actualDTO.getId()).isEqualTo(queryId);
        assertThat(actualDTO.getReportName()).isEqualTo(reportName);
        assertThat(actualDTO.getDescription()).isEqualTo(description);
        assertThat(actualDTO.getCategories()).isEmpty();
    }

    @Test
    void updateMiReportUserDefinedEntity() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);

        final String queryDefinitionNew = "select * from facility_audit order by created_on desc";
        final String reportNameNew = "New test report name";
        final String descriptionNew = "New test report description";


        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinitionNew)
                .reportName(reportNameNew)
                .description(descriptionNew)
                .build();

        mapper.updateMiReportUserDefinedEntity(miReportQueryEntity, miReportQueryDTO, null);

        assertThat(miReportQueryEntity.getReportName()).isEqualTo(reportNameNew);
        assertThat(miReportQueryEntity.getDescription()).isEqualTo(descriptionNew);
        assertThat(miReportQueryEntity.getQueryDefinition()).isEqualTo(queryDefinitionNew);
    }
}
