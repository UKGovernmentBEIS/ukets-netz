package uk.gov.netz.api.mireport.userdefined.favourite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedEntity;
import uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
class MiReportUserDefinedFavouriteRepositoryIT extends AbstractContainerBaseTest {

    private static final String USER_ID = "user-1";
    private static final String OTHER_USER_ID = "user-2";

    @Autowired
    private MiReportUserDefinedFavouriteRepository repository;

    @Autowired
    private MiReportUserDefinedRepository reportRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "reportName"));

    private Long favouritedReportId;
    private Long otherReportId;

    @BeforeEach
    void setUp() {
        favouritedReportId = saveReport("Annual finance report");
        otherReportId = saveReport("Compliance overview");

        repository.save(MiReportUserDefinedFavouriteEntity.builder()
                .userId(USER_ID)
                .miReportId(favouritedReportId)
                .build());

        reportRepository.flush();
        repository.flush();
        entityManager.clear();
    }

    @Test
    void existsByUserIdAndMiReportId_trueForFavourited() {
        assertThat(repository.existsByUserIdAndMiReportId(USER_ID, favouritedReportId)).isTrue();
    }

    @Test
    void existsByUserIdAndMiReportId_falseForNonFavouritedReport() {
        assertThat(repository.existsByUserIdAndMiReportId(USER_ID, otherReportId)).isFalse();
    }

    @Test
    void existsByUserIdAndMiReportId_falseForOtherUser() {
        assertThat(repository.existsByUserIdAndMiReportId(OTHER_USER_ID, favouritedReportId)).isFalse();
    }

    @Test
    void deleteByUserIdAndMiReportId_removesFavourite() {
        repository.deleteByUserIdAndMiReportId(USER_ID, favouritedReportId);
        repository.flush();
        entityManager.clear();

        assertThat(repository.existsByUserIdAndMiReportId(USER_ID, favouritedReportId)).isFalse();
    }

    @Test
    void deleteByUserIdAndMiReportId_idempotentWhenNotFavourited() {
        repository.deleteByUserIdAndMiReportId(USER_ID, otherReportId);
        repository.flush();

        assertThat(repository.existsByUserIdAndMiReportId(USER_ID, favouritedReportId)).isTrue();
    }

    @Test
    void uniqueConstraint_preventsDuplicateFavourite() {
        repository.save(MiReportUserDefinedFavouriteEntity.builder()
                .userId(USER_ID)
                .miReportId(favouritedReportId)
                .build());

        assertThatThrownBy(() -> repository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filter_withUserId_returnsOnlyFavouritedReports() {
        Page<MiReportUserDefinedEntity> result = reportRepository.findAllByCompetentAuthorityAndFilters(
                CompetentAuthorityEnum.ENGLAND, null, null, USER_ID, pageable);

        assertThat(result.getContent())
                .extracting(MiReportUserDefinedEntity::getId)
                .containsExactly(favouritedReportId);
    }

    @Test
    void filter_withNullUserId_returnsAllReports() {
        Page<MiReportUserDefinedEntity> result = reportRepository.findAllByCompetentAuthorityAndFilters(
                CompetentAuthorityEnum.ENGLAND, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void filter_withUserIdWithoutFavourites_returnsEmpty() {
        Page<MiReportUserDefinedEntity> result = reportRepository.findAllByCompetentAuthorityAndFilters(
                CompetentAuthorityEnum.ENGLAND, null, null, OTHER_USER_ID, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    private Long saveReport(String reportName) {
        return reportRepository.save(MiReportUserDefinedEntity.builder()
                .reportName(reportName)
                .description("desc")
                .queryDefinition("select 1")
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .createdBy("test-user")
                .lastUpdatedOn(LocalDateTime.now())
                .categories(new HashSet<>())
                .build()).getId();
    }
}