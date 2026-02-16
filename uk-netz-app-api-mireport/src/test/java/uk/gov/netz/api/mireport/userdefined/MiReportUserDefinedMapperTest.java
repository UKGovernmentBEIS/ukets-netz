package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MiReportUserDefinedMapperTest {

    private final MiReportUserDefinedMapper mapper = Mappers.getMapper(MiReportUserDefinedMapper.class);

    @Test
    void toMiReportUserDefinedEntity() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        MiReportUserDefinedEntity entity = mapper.toMiReportUserDefinedEntity(miReportQueryDTO, CompetentAuthorityEnum.ENGLAND, userId);

        assertThat(entity.getReportName()).isEqualTo(reportName);
        assertThat(entity.getDescription()).isEqualTo(description);
        assertThat(entity.getQueryDefinition()).isEqualTo(queryDefinition);
        assertThat(entity.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.ENGLAND);
        assertThat(entity.getCreatedBy()).isEqualTo(userId);
    }

    @Test
    void toMiReportUserDefinedDTO() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        final MiReportUserDefinedDTO expectedDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        MiReportUserDefinedDTO actualDTO = mapper.toMiReportUserDefinedDTO(miReportQueryEntity);

        assertThat(actualDTO).isEqualTo(expectedDTO);
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

        mapper.updateMiReportUserDefinedEntity(miReportQueryEntity, miReportQueryDTO);

        assertThat(miReportQueryEntity.getReportName()).isEqualTo(reportNameNew);
        assertThat(miReportQueryEntity.getDescription()).isEqualTo(descriptionNew);
        assertThat(miReportQueryEntity.getQueryDefinition()).isEqualTo(queryDefinitionNew);
    }
}
