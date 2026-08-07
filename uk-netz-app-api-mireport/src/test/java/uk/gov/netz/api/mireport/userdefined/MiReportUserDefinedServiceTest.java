package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryDTO;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryMapper;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryService;
import uk.gov.netz.api.mireport.userdefined.custom.CustomMiReportQuery;
import uk.gov.netz.api.mireport.userdefined.favourite.MiReportUserDefinedFavouriteService;
import uk.gov.netz.api.mireport.userdefined.history.MiReportUserDefinedHistoryService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiReportUserDefinedServiceTest {

    @InjectMocks
    private MiReportUserDefinedService service;

    @Mock
    private MiReportUserDefinedRepository miReportUserDefinedRepository;

    @Mock
    private MiReportUserDefinedGeneratorDelegator miReportUserDefinedGeneratorDelegator;

    @Mock
    private MiReportUserDefinedCategoryService miReportUserDefinedCategoryService;

    @Mock
    private MiReportUserDefinedHistoryService miReportUserDefinedHistoryService;

    @Mock
    private MiReportUserDefinedFavouriteService miReportUserDefinedFavouriteService;

    @BeforeEach
    void setUp() {
        MiReportUserDefinedMapper mapper = Mappers.getMapper(MiReportUserDefinedMapper.class);
        ReflectionTestUtils.setField(mapper, "miReportUserDefinedCategoryMapper",
                Mappers.getMapper(MiReportUserDefinedCategoryMapper.class));

        service = new MiReportUserDefinedService(
                miReportUserDefinedRepository,
                miReportUserDefinedCategoryService,
                miReportUserDefinedGeneratorDelegator,
                mapper,
                miReportUserDefinedHistoryService,
                miReportUserDefinedFavouriteService);
    }

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
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));

        final MiReportUserDefinedEntity entity1 = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .reportName(reportName)
                .description(description)
                .build();

        final MiReportUserDefinedEntity entity2 = MiReportUserDefinedEntity.builder()
                .id(queryId2)
                .reportName(reportName2)
                .description(description2)
                .build();

        final Page<MiReportUserDefinedEntity> page = new PageImpl<>(List.of(entity1, entity2));

        when(miReportUserDefinedRepository.findAllByCompetentAuthority(CompetentAuthorityEnum.ENGLAND, pageable)).thenReturn(page);

        // invoke
        MiReportUserDefinedResults actualResults = service.findAllByCA(CompetentAuthorityEnum.ENGLAND, pageNumber, pageSize);

        assertThat(actualResults.getTotal()).isEqualTo(2);
        assertThat(actualResults.getQueries()).hasSize(2).containsExactlyInAnyOrder(
                MiReportUserDefinedInfoDTO.builder().id(queryId).reportName(reportName).description(description).build(),
                MiReportUserDefinedInfoDTO.builder().id(queryId2).reportName(reportName2).description(description2).build()
        );
    }

    @Test
    void findAllByCAAndFilters() {
        final Long queryId = 1L;
        final String reportName = "test report name";
        final String description = "test description";

        final Long queryId2 = 2L;
        final String reportName2 = "test report name 2";
        final String description2 = "test description 2";

        final int pageNumber = 1;
        final int pageSize = 10;
        final Long categoryId = 5L;
        final String searchTerm = "Report";
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));

        final AppUser appUser = getAppUser();

        final MiReportUserDefinedEntity entity1 = MiReportUserDefinedEntity.builder()
                .id(queryId)
                .reportName(reportName)
                .description(description)
                .build();

        final MiReportUserDefinedEntity entity2 = MiReportUserDefinedEntity.builder()
                .id(queryId2)
                .reportName(reportName2)
                .description(description2)
                .build();

        final Page<MiReportUserDefinedEntity> page = new PageImpl<>(List.of(entity1, entity2));

        when(miReportUserDefinedRepository.findAllByCompetentAuthorityAndFilters(
                CompetentAuthorityEnum.ENGLAND, categoryId, "%report%", appUser.getUserId(), pageable)).thenReturn(page);

        // invoke
        MiReportUserDefinedResults actualResults = service.findAllByCA(
                appUser, pageNumber, pageSize, categoryId, searchTerm, true);

        assertThat(actualResults.getTotal()).isEqualTo(2);
        assertThat(actualResults.getQueries()).hasSize(2).containsExactlyInAnyOrder(
                MiReportUserDefinedInfoDTO.builder().id(queryId).reportName(reportName).description(description).build(),
                MiReportUserDefinedInfoDTO.builder().id(queryId2).reportName(reportName2).description(description2).build()
        );

        verify(miReportUserDefinedRepository)
                .findAllByCompetentAuthorityAndFilters(CompetentAuthorityEnum.ENGLAND, categoryId, "%report%", appUser.getUserId(), pageable);
    }

    @Test
    void findAllByCAAndFilters_noFilters_passesNulls() {
        final int pageNumber = 0;
        final int pageSize = 20;
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "lastUpdatedOn"));
        final AppUser appUser = getAppUser();

        when(miReportUserDefinedRepository.findAllByCompetentAuthorityAndFilters(
                CompetentAuthorityEnum.ENGLAND, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of()));

        MiReportUserDefinedResults actualResults = service.findAllByCA(
                appUser, pageNumber, pageSize, null, null, false);

        assertThat(actualResults.getTotal()).isZero();
        assertThat(actualResults.getQueries()).isEmpty();

        verify(miReportUserDefinedRepository)
                .findAllByCompetentAuthorityAndFilters(CompetentAuthorityEnum.ENGLAND, null, null, null, pageable);
    }

    @Test
    void findById() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);
        final AppUser appUser = getAppUser();

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
                .categories(new HashSet<>())
                .lastUpdatedOn(lastUpdatedOn)
                .build();

        when(miReportUserDefinedRepository.findById(queryId)).thenReturn(Optional.of(miReportQueryEntity));

        MiReportUserDefinedDTO actualResult = service.findById(appUser, queryId);

        assertEquals(actualResult, expectedResult);
    }

    @Test
    void findById_setsFavouriteTrueWhenUserFavourited() {
        final Long queryId = 1L;
        final AppUser appUser = getAppUser();
        final MiReportUserDefinedEntity entity = MiReportUserDefinedEntity.builder()
                .queryDefinition("select 1").reportName("n").build();

        when(miReportUserDefinedRepository.findById(queryId)).thenReturn(Optional.of(entity));
        when(miReportUserDefinedFavouriteService.isFavourite(appUser, queryId)).thenReturn(true);

        assertThat(service.findById(appUser, queryId).isFavourite()).isTrue();
    }

    @Test
    void create() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final AppUser appUser = getAppUser();
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .build();

        when(miReportUserDefinedCategoryService.getByIds(new HashSet<>())).thenReturn(new HashSet<>());
        when(miReportUserDefinedRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // invoke
        service.create(appUser, miReportQueryDTO);

        ArgumentCaptor<MiReportUserDefinedEntity> captor = ArgumentCaptor.forClass(MiReportUserDefinedEntity.class);
        verify(miReportUserDefinedRepository).save(captor.capture());

        MiReportUserDefinedEntity miReportQueryEntity = captor.getValue();

        // verify
        assertEquals(queryDefinition, miReportQueryEntity.getQueryDefinition());
        assertEquals(reportName, miReportQueryEntity.getReportName());
        assertEquals(description, miReportQueryEntity.getDescription());
        assertEquals(userId, miReportQueryEntity.getCreatedBy());
        assertEquals(ca, miReportQueryEntity.getCompetentAuthority());
        assert(miReportQueryEntity.getCategories().isEmpty());
        verify(miReportUserDefinedHistoryService).recordCreate(appUser, miReportQueryEntity);
    }

    @Test
    void create_with_categories() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final AppUser appUser = getAppUser();
        final Set<Long> categoryIds = Set.of(1L, 2L);
        CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinition)
                .reportName(reportName)
                .description(description)
                .categories(Set.of(
                        MiReportUserDefinedCategoryDTO.builder().id(1L).build(),
                        MiReportUserDefinedCategoryDTO.builder().id(2L).build()
                ))
                .build();

        when(miReportUserDefinedCategoryService.getByIds(categoryIds)).thenReturn(Set.of(
                MiReportUserDefinedCategoryEntity.builder().id(1L).name("Category 1").build(),
                MiReportUserDefinedCategoryEntity.builder().id(2L).name("Category 2").build()
        ));
        when(miReportUserDefinedRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // invoke
        service.create(appUser, miReportQueryDTO);

        ArgumentCaptor<MiReportUserDefinedEntity> captor = ArgumentCaptor.forClass(MiReportUserDefinedEntity.class);
        verify(miReportUserDefinedRepository).save(captor.capture());

        MiReportUserDefinedEntity miReportQueryEntity = captor.getValue();

        // verify
        assertEquals(queryDefinition, miReportQueryEntity.getQueryDefinition());
        assertEquals(reportName, miReportQueryEntity.getReportName());
        assertEquals(description, miReportQueryEntity.getDescription());
        assertEquals(userId, miReportQueryEntity.getCreatedBy());
        assertEquals(ca, miReportQueryEntity.getCompetentAuthority());
        assertEquals(Set.of(
                MiReportUserDefinedCategoryEntity.builder().id(1L).name("Category 1").build(),
                MiReportUserDefinedCategoryEntity.builder().id(2L).name("Category 2").build()
        ), miReportQueryEntity.getCategories());
        verify(miReportUserDefinedHistoryService).recordCreate(appUser, miReportQueryEntity);
    }

    @Test
    void create_with_error() {
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final AppUser appUser = getAppUser();
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
                () -> service.create(appUser, miReportQueryDTO));

        // verify
        assertEquals(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA, be.getErrorCode());
        verify(miReportUserDefinedRepository).findIdByReportNameAndCA(reportName, ca);
        verifyNoInteractions(miReportUserDefinedHistoryService);
    }


    @Test
    void update() {
        final Long queryId = 1L;
        final String queryDefinition = "select * from facility_audit";
        final String reportName = "test report name";
        final String description = "test description";
        final String userId = "test user id";
        final LocalDateTime lastUpdatedOn = LocalDateTime.of(2023, 9, 10, 12, 0);
        final AppUser appUser = AppUser.builder().userId(userId).build();

        final String queryDefinitionUpdated = "select * from facility_audit fd order by fd.id desc";
        final String reportNameUpdated = "test report name UPDATED";

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinitionUpdated)
                .reportName(reportNameUpdated)
                .description(description)
                .build();

        final MiReportUserDefinedUpdateDTO miReportUserDefinedUpdateDTO = MiReportUserDefinedUpdateDTO.builder()
                .userDefinedDTO(miReportQueryDTO)
                .reasonForChange("Updating report name and query definition")
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
        when(miReportUserDefinedRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // invoke
        service.update(queryId, appUser, miReportUserDefinedUpdateDTO);

        ArgumentCaptor<MiReportUserDefinedEntity> captor = ArgumentCaptor.forClass(MiReportUserDefinedEntity.class);
        verify(miReportUserDefinedRepository).save(captor.capture());

        MiReportUserDefinedEntity updatedMiReportQueryEntity = captor.getValue();

        // verify
        assertEquals(queryDefinitionUpdated, updatedMiReportQueryEntity.getQueryDefinition());
        assertEquals(reportNameUpdated, updatedMiReportQueryEntity.getReportName());
        assertEquals(description, updatedMiReportQueryEntity.getDescription());
        assertEquals(CompetentAuthorityEnum.ENGLAND, updatedMiReportQueryEntity.getCompetentAuthority());
        assertEquals(userId, updatedMiReportQueryEntity.getCreatedBy());
        verify(miReportUserDefinedHistoryService).recordUpdate(appUser, updatedMiReportQueryEntity,
                "Updating report name and query definition");
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
        final AppUser appUser = AppUser.builder().userId(userId).build();

        final String queryDefinitionUpdated = "select * from facility_audit fd order by fd.id desc";
        final String reportNameUpdated = "test report name UPDATED";

        final MiReportUserDefinedDTO miReportQueryDTO = MiReportUserDefinedDTO.builder()
                .queryDefinition(queryDefinitionUpdated)
                .reportName(reportNameUpdated)
                .description(description)
                .build();

        final MiReportUserDefinedUpdateDTO miReportUserDefinedUpdateDTO = MiReportUserDefinedUpdateDTO.builder()
                .userDefinedDTO(miReportQueryDTO)
                .reasonForChange("Updating report name and query definition")
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
                () -> service.update(queryId, appUser,miReportUserDefinedUpdateDTO));

        // verify
        assertEquals(ErrorCode.MI_REPORT_NAME_EXISTS_FOR_CA, be.getErrorCode());
        verify(miReportUserDefinedRepository).findById(queryId);
        verify(miReportUserDefinedRepository).findIdByReportNameAndCA(reportNameUpdated, miReportQueryEntity.getCompetentAuthority());
        verifyNoInteractions(miReportUserDefinedHistoryService);
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

    @Test
    void previewCustomReport() {
        CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        CustomMiReportQuery customQuery = CustomMiReportQuery.builder().sqlQuery("custom sql query").build();

        MiReportUserDefinedResult result = MiReportUserDefinedResult.builder()
                .columnNames(List.of("col1"))
                .build();

        when(miReportUserDefinedGeneratorDelegator.generateReport(competentAuthority, customQuery.getSqlQuery(), 10)).thenReturn(result);

        var actualResult = service.previewCustomReport(competentAuthority, customQuery);

        assertThat(actualResult).isEqualTo(result);
        verify(miReportUserDefinedGeneratorDelegator, times(1)).generateReport(competentAuthority, customQuery.getSqlQuery(), 10);
    }

    private AppUser getAppUser() {
        AppAuthority authority = AppAuthority.builder().competentAuthority(CompetentAuthorityEnum.ENGLAND).build();

        AppUser appUser = new AppUser();
        appUser.setFirstName("firstName");
        appUser.setLastName("lastName");
        appUser.setUserId("test user id");
        appUser.setAuthorities(List.of(authority));
        return appUser;
    }


}