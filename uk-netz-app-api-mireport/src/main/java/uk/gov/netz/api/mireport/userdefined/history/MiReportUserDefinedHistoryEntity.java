package uk.gov.netz.api.mireport.userdefined.history;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Check(name = "chk_mi_rep_user_def_hist_reason", constraints = "change_type != 'UPDATE' OR reason_for_change IS NOT NULL")
@Table(name = "mi_report_user_defined_history")
public class MiReportUserDefinedHistoryEntity {

    @Id
    @SequenceGenerator(name = "mi_report_user_defined_history_id_generator",
            sequenceName = "mi_report_user_defined_history_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mi_report_user_defined_history_id_generator")
    private Long id;

    @Column(name = "mi_report_id", nullable = false)
    @NotNull
    private Long miReportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    @NotNull
    private MiReportUserDefinedChangeType changeType;

    @Column(name = "submission_date", nullable = false)
    @NotNull
    @CreatedDate
    private LocalDateTime submissionDate;

    @Column(name = "reason_for_change")
    @Size(max = 10000)
    private String reasonForChange;

    @Column(name = "submitted_by", nullable = false)
    @Size(max = 255)
    @NotNull
    private String submittedBy;

    @Column(name = "report_name", nullable = false)
    @NotBlank
    private String reportName;

    @Column(name = "categories")
    @Size(max = 10000)
    private String categories;

    @Column(name = "description")
    @Size(max = 10000)
    private String description;

    @Column(name = "query_definition",nullable = false)
    @NotNull
    @Size(max = 50000)
    private String queryDefinition;

}