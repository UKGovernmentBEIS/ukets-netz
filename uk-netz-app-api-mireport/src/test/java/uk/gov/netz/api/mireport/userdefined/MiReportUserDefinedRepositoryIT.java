package uk.gov.netz.api.mireport.userdefined;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryRepository;
import uk.gov.netz.api.mireport.userdefined.favourite.MiReportUserDefinedFavouriteEntity;
import uk.gov.netz.api.mireport.userdefined.favourite.MiReportUserDefinedFavouriteRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
class MiReportUserDefinedRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private MiReportUserDefinedRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MiReportUserDefinedCategoryRepository categoryRepository;

    @Autowired
    private MiReportUserDefinedFavouriteRepository favouriteRepository;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "reportName"));

    private MiReportUserDefinedCategoryEntity financeCategory;
    private MiReportUserDefinedCategoryEntity complianceCategory;

    private static final String USER_ID = "user-1";
    private static final String USER_WITHOUT_FAVOURITES = "user-2";

    @BeforeEach
    void setUp() {
        financeCategory = categoryRepository.save(
                MiReportUserDefinedCategoryEntity.builder().name("Finance").enabled(true).build());
        complianceCategory = categoryRepository.save(
                MiReportUserDefinedCategoryEntity.builder().name("Compliance").enabled(true).build());

        save("Annual finance report", "Yearly financial summary", Set.of(financeCategory));
        save("Compliance overview", "Regulatory compliance details", Set.of(complianceCategory));
        save("50% coverage report", "Special _underscore_ description", Set.of());

        repository.flush();
        categoryRepository.flush();
        favouriteRepository.flush();
    }

    @Test
    void findAllByFilters_withNoFilters_returnsAllForCa() {
        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findAllByFilters_filtersByCategory() {
        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                financeCategory.getId(), null, null, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    @Test
    void findAllByFilters_filtersByTermOnReportName_caseInsensitive() {
        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, QuerySearchUtils.toSearchPattern("COMPLIANCE"), null, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Compliance overview");
    }

    @Test
    void findAllByFilters_filtersByTermOnDescription() {
        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, QuerySearchUtils.toSearchPattern("Yearly"), null, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    @Test
    void findAllByFilters_escapesLikeWildcards() {
        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, QuerySearchUtils.toSearchPattern("50%"), null, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("50% coverage report");
    }

    @Test
    void findIdByReportNameAndCA() {
        Optional<Long> annualFinanceReport = repository.findIdByReportName("Annual finance report");

        assertThat(annualFinanceReport).isPresent();
        Optional<MiReportUserDefinedEntity> expected = repository.findById(annualFinanceReport.get());
        assertThat(expected).isPresent();
        assertThat(annualFinanceReport.get()).isEqualTo(expected.get().getId());
    }

    @Test
    void delete_softDeletesRow_hiddenFromFinders_butStillInTable() {
        Long id = repository
            .findIdByReportName("Annual finance report").get();

        repository.deleteById(id);
        repository.flush();
        entityManager.clear();

        assertThat(repository.findById(id)).isEmpty();
        assertThat(repository.findIdByReportName("Annual finance report")).isEmpty();
        assertThat(repository.findAll(pageable)
                .getContent())
                .noneMatch(r -> r.getId().equals(id));

        Number inactiveCount = (Number) entityManager.getEntityManager()
                .createNativeQuery("select count(*) from mi_report_user_defined where id = :id and active = false")
                .setParameter("id", id)
                .getSingleResult();
        assertThat(inactiveCount.longValue()).isEqualTo(1);
    }

    @Test
    void softDeletedName_canBeReusedForNewActiveReport() {
        Long originalId = repository
            .findIdByReportName("Annual finance report").get();

        repository.deleteById(originalId);
        repository.flush();
        entityManager.clear();

        save("Annual finance report", "Recreated after soft delete", Set.of());
        repository.flush();
        entityManager.clear();

        Long activeId = repository.findIdByReportName("Annual finance report").get();
        Optional<MiReportUserDefinedEntity> expected = repository.findById(activeId);
        assertThat(expected).isPresent();
        assertThat(expected.get().getReportName()).isEqualTo("Annual finance report");
        assertThat(expected.get().getId()).isNotEqualTo(originalId);
    }

    @Test
    void findAllByFilters_filtersByFavourites_returnsOnlyUsersFavourites() {
        Long financeReportId = reportId("Annual finance report");
        favourite(financeReportId);

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, null, USER_ID, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    @Test
    void findAllByFilters_filtersByFavourites_isScopedPerUser() {
        favourite(reportId("Annual finance report"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, null, USER_WITHOUT_FAVOURITES, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByFilters_nullUserId_ignoresFavouritesFilter() {
        favourite(reportId("Annual finance report"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void findAllByFilters_favouritesCombinedWithCategory() {
        favourite(reportId("Annual finance report"));
        favourite(reportId("Compliance overview"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                financeCategory.getId(), null, USER_ID, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    private void favourite(Long miReportId) {
        favouriteRepository.save(MiReportUserDefinedFavouriteEntity.builder()
                .userId(MiReportUserDefinedRepositoryIT.USER_ID)
                .miReportId(miReportId)
                .build());
        favouriteRepository.flush();
    }

    @Test
    void findAllByFilters_favouritesCombinedWithSearchTerm() {
        favourite(reportId("Annual finance report"));
        favourite(reportId("Compliance overview"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                null, QuerySearchUtils.toSearchPattern("finance"), USER_ID, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    @Test
    void findAllByFilters_favouritesCombinedWithCategoryAndSearchTerm() {
        favourite(reportId("Annual finance report"));
        favourite(reportId("Compliance overview"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                financeCategory.getId(),
                QuerySearchUtils.toSearchPattern("finance"), USER_ID, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getReportName)
                .containsExactly("Annual finance report");
    }

    @Test
    void findAllByFilters_favouritesCategoryAndSearchTerm_noMatches() {
        favourite(reportId("Annual finance report"));

        Page<MiReportUserDefinedEntity> result = repository.findAllBySearchCriteria(
                financeCategory.getId(),
                QuerySearchUtils.toSearchPattern("compliance"), USER_ID, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private Long reportId(String reportName) {
        return repository.findIdByReportName(reportName).get();
    }

    private void save(String reportName, String description,
                      Set<MiReportUserDefinedCategoryEntity> categories) {
        repository.save(MiReportUserDefinedEntity.builder()
                .reportName(reportName)
                .description(description)
                .queryDefinition("select 1")
                .createdBy("test-user")
                .lastUpdatedOn(LocalDateTime.now())
                .categories(new java.util.HashSet<>(categories))
                .build());
    }
}