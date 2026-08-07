package uk.gov.netz.api.mireport.userdefined.history;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.common.AbstractContainerBaseTest;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(MiReportUserDefinedHistoryRepositoryIT.JpaAuditingTestConfig.class)
class MiReportUserDefinedHistoryRepositoryIT extends AbstractContainerBaseTest {

    @EnableJpaAuditing(dateTimeProviderRef = "testDateTimeProvider")
    static class JpaAuditingTestConfig {

        @Bean
        MutableDateTimeProvider testDateTimeProvider() {
            return new MutableDateTimeProvider();
        }
    }

    static class MutableDateTimeProvider implements DateTimeProvider {
        private LocalDateTime now = LocalDateTime.now();

        void setNow(LocalDateTime now) {
            this.now = now;
        }

        @Override
        public Optional<TemporalAccessor> getNow() {
            return Optional.of(now);
        }
    }

    @Autowired
    private MiReportUserDefinedHistoryRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MutableDateTimeProvider dateTimeProvider;

    @Test
    void save_populatesSubmissionDateViaAuditing() {
        LocalDateTime auditedNow = LocalDateTime.of(2024, 6, 1, 9, 30);
        dateTimeProvider.setNow(auditedNow);

        MiReportUserDefinedHistoryEntity entity = MiReportUserDefinedHistoryEntity.builder()
                .miReportId(1L)
                .changeType(MiReportUserDefinedChangeType.CREATE)
                .submittedBy("John Doe")
                .reportName("Annual finance report")
                .queryDefinition("select 1")
                .build();

        repository.saveAndFlush(entity);
        entityManager.clear();

        MiReportUserDefinedHistoryEntity reloaded = repository.findById(entity.getId()).orElseThrow();

        assertThat(reloaded.getSubmissionDate())
                .as("submissionDate should be populated automatically by JPA auditing")
                .isEqualTo(auditedNow);
    }

    @Test
    void findByMiReportId_returnsOnlyMatchingRowsOrderedBySubmissionDateDesc() {
        MiReportUserDefinedHistoryEntity created = save(10L, MiReportUserDefinedChangeType.CREATE, "John Doe", null,
                "Report A", "select 1", LocalDateTime.of(2023, 1, 1, 10, 0));
        MiReportUserDefinedHistoryEntity updated = save(10L, MiReportUserDefinedChangeType.UPDATE, "Jane Roe",
                "Renamed report", "Report A renamed", "select 2", LocalDateTime.of(2023, 2, 1, 10, 0));
        save(99L, MiReportUserDefinedChangeType.CREATE, "Other User", null,
                "Unrelated report", "select 3", LocalDateTime.of(2023, 3, 1, 10, 0));

        repository.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "submissionDate"));
        Page<MiReportUserDefinedHistoryEntity> result = repository.findByMiReportId(10L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(MiReportUserDefinedHistoryEntity::getId)
                .containsExactly(updated.getId(), created.getId());
    }

    @Test
    void saveAndFlush_rejectsUpdateWithoutReasonForChange() {
        MiReportUserDefinedHistoryEntity entity = MiReportUserDefinedHistoryEntity.builder()
                .miReportId(10L)
                .changeType(MiReportUserDefinedChangeType.UPDATE)
                .submittedBy("Jane Roe")
                .reportName("Annual finance report")
                .queryDefinition("select 2")
                .build();

        assertThatThrownBy(() -> repository.saveAndFlush(entity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private MiReportUserDefinedHistoryEntity save(Long miReportId, MiReportUserDefinedChangeType changeType,
                                                  String submittedBy, String reasonForChange, String reportName,
                                                  String queryDefinition, LocalDateTime submissionDate) {
        dateTimeProvider.setNow(submissionDate);
        return repository.save(MiReportUserDefinedHistoryEntity.builder()
                .miReportId(miReportId)
                .changeType(changeType)
                .submittedBy(submittedBy)
                .reasonForChange(reasonForChange)
                .reportName(reportName)
                .queryDefinition(queryDefinition)
                .build());
    }
}
