package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.custom.CustomMiReportQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedServiceTest {

    @InjectMocks
    private MiReportUserDefinedService service;

    @Mock
    private MiReportUserDefinedRepository miReportUserDefinedRepository;

    @Mock
    private MiReportUserDefinedGeneratorDelegator miReportUserDefinedGeneratorDelegator;

    @Test
    void findAllByCA() {
        final Long queryId = 1L;
        final String reportName = "test report name";
        final String description = "test description";

        final Long queryId2 = 2L;
        final String reportName2 = "test report name 2";
        final String description2 = "test description 2";

        final int pageNumber = 1;
        final int pageSize = 10;
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "reportName"));

        final MiReportUserDefinedInfoDTO miReportQueryInfoDTO1 = MiReportUserDefinedInfoDTO.builder()
                .id(queryId)
                .reportName(reportName)
                .description(description)
                .build();

        final MiReportUserDefinedInfoDTO miReportQueryInfoDTO2 = MiReportUserDefinedInfoDTO.builder()
                .id(queryId2)
                .reportName(reportName2)
                .description(description2)
                .build();

        final List<MiReportUserDefinedInfoDTO> queries = List.of(miReportQueryInfoDTO1, miReportQueryInfoDTO2);
        final Page<MiReportUserDefinedInfoDTO> page = new PageImpl<>(queries);

        MiReportUserDefinedResults expectedResults = MiReportUserDefinedResults.builder()
                .queries(queries)
                .total(2L)
                .build();

        when(miReportUserDefinedRepository.findAllByCA(CompetentAuthorityEnum.ENGLAND, pageable)).thenReturn(page);

        // invoke
        MiReportUserDefinedResults actualResults = service.findAllByCA(CompetentAuthorityEnum.ENGLAND, pageNumber, pageSize);

        assertEquals(actualResults, expectedResults);
    }

    @Test
    void findById() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        final MiReportUserDefinedDTO expectedResult = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        when(miReportUserDefinedRepository.findById(queryId)).thenReturn(Optional.of(miReportQueryEntity));

        // invoke
        MiReportUserDefinedDTO actualResult = service.findById(queryId);

        assertEquals(actualResult, expectedResult);
    }

    @Test
    void create() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        // invoke
        service.create(userId, ca, miReportQueryDTO);

        ArgumentCaptor<MiReportUserDefinedEntity> captor = ArgumentCaptor.forClass(MiReportUserDefinedEntity.class);
        verify(miReportUserDefinedRepository).save(captor.capture());

        MiReportUserDefinedEntity miReportQueryEntity = captor.getValue();

        // verify
        assertEquals(queryDefinition, miReportQueryEntity.getQueryDefinition());
        assertEquals(reportName, miReportQueryEntity.getReportName());
        assertEquals(description, miReportQueryEntity.getDescription());
        assertEquals(userId, miReportQueryEntity.getCreatedBy());
        assertEquals(ca, miReportQueryEntity.getCompetentAuthority());
    }

    @Test
    void create_with_error() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        when(miReportUserDefinedRepository.findIdByReportNameAndCA(reportName, ca))
                .thenReturn(Optional.of(1L));

        // invoke
        final BusinessException be = assertThrows(BusinessException.class,
                () -> service.create(userId, ca, miReportQueryDTO));

        // verify
        assertEquals(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA, be.getErrorCode());
        verify(miReportUserDefinedRepository).findIdByReportNameAndCA(reportName, ca);
    }

    @Test
    void update() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);

        final String queryDefinitionUpdated = "select * from facility_audit fd order by fd.id desc";
        final String reportNameUpdated = "test report name UPDATED";

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinitionUpdated)
                .reportName(reportNameUpdated)
                .description(description)
                .build();

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        when(miReportUserDefinedRepository.findById(queryId)).thenReturn(Optional.of(miReportQueryEntity));
        when(miReportUserDefinedRepository.findIdByReportNameAndCA(reportNameUpdated, miReportQueryEntity.getCompetentAuthority()))
                .thenReturn(Optional.empty());

        // invoke
        service.update(queryId, miReportQueryDTO);

        ArgumentCaptor<MiReportUserDefinedEntity> captor = ArgumentCaptor.forClass(MiReportUserDefinedEntity.class);
        verify(miReportUserDefinedRepository).save(captor.capture());

        MiReportUserDefinedEntity updatedMiReportQueryEntity = captor.getValue();

        // verify
        assertEquals(queryDefinitionUpdated, updatedMiReportQueryEntity.getQueryDefinition());
        assertEquals(reportNameUpdated, updatedMiReportQueryEntity.getReportName());
        assertEquals(description, updatedMiReportQueryEntity.getDescription());
        assertEquals(CompetentAuthorityEnum.ENGLAND, updatedMiReportQueryEntity.getCompetentAuthority());
        assertEquals(userId, updatedMiReportQueryEntity.getCreatedBy());
    }

    @Test
    void update_with_error() {
        final Long queryId = 1L;
        final Long queryId2 = 2L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);

        final String queryDefinitionUpdated = "select * from facility_audit fd order by fd.id desc";
        final String reportNameUpdated = "test report name UPDATED";

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinitionUpdated)
                .reportName(reportNameUpdated)
                .description(description)
                .build();

        final MiReportUserDefinedEntity miReportQueryEntity = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .createdBy(userId)
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        when(miReportUserDefinedRepository.findById(queryId)).thenReturn(Optional.of(miReportQueryEntity));
        when(miReportUserDefinedRepository.findIdByReportNameAndCA(reportNameUpdated, miReportQueryEntity.getCompetentAuthority()))
                .thenReturn(Optional.of(queryId2));

        // invoke
        final BusinessException be = assertThrows(BusinessException.class,
                () -> service.update(queryId, miReportQueryDTO));

        // verify
        assertEquals(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA, be.getErrorCode());
        verify(miReportUserDefinedRepository).findById(queryId);
        verify(miReportUserDefinedRepository).findIdByReportNameAndCA(reportNameUpdated, miReportQueryEntity.getCompetentAuthority());
    }

    @Test
    void delete() {
        final Long queryId = 1L;

        // invoke
        service.delete(queryId);

        // verify
        verify(miReportUserDefinedRepository, times(1)).deleteById(queryId);
    }

    @Test
    void generateReport() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        Long miReportUserDefinedId = 1L;
        MiReportUserDefinedEntity entity = MiReportUserDefinedEntity.builder()
                .queryDefinition("queryDef")
                .competentAuthority(competentAuthority)
                .build();
        when(miReportUserDefinedRepository.findById(miReportUserDefinedId)).thenReturn(Optional.of(entity));

        MiReportUserDefinedResult result = MiReportUserDefinedResult.builder()
                .columnNames(List.of("col1"))
                .build();

        when(miReportUserDefinedGeneratorDelegator.generateReport(competentAuthority, entity.getQueryDefinition())).thenReturn(result);

        var actualResult = service.generateReport(miReportUserDefinedId);

        assertThat(actualResult).isEqualTo(result);
        verify(miReportUserDefinedRepository, times(1)).findById(miReportUserDefinedId);
        verify(miReportUserDefinedGeneratorDelegator, times(1)).generateReport(competentAuthority, entity.getQueryDefinition());
    }

    @Test
    void generateCustomReport() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        CustomMiReportQuery customQuery = CustomMiReportQuery.builder().sqlQuery("custom sql query").build();

        MiReportUserDefinedResult result = MiReportUserDefinedResult.builder()
                .columnNames(List.of("col1"))
                .build();

        when(miReportUserDefinedGeneratorDelegator.generateReport(competentAuthority, customQuery.getSqlQuery())).thenReturn(result);

        var actualResult = service.generateCustomReport(competentAuthority, customQuery);

        assertThat(actualResult).isEqualTo(result);
        verify(miReportUserDefinedGeneratorDelegator, times(1)).generateReport(competentAuthority, customQuery.getSqlQuery());
    }


}