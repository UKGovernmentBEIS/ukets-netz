package uk.gov.netz.api.mireport.userdefined;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@NamedQuery(
        name = MiReportUserDefinedEntity.NAMED_QUERY_FIND_ALL_BY_CA,
        query = "select new uk.gov.netz.api.mireport.userdefined.MiReportUserDefinedInfoDTO(m.id, m.reportName, m.description) "
                + "from MiReportUserDefinedEntity m "
                + "where m.competentAuthority = :competentAuthority "
)
@NamedQuery(
        name = MiReportUserDefinedEntity.NAMED_QUERY_FIND_BY_REPORT_NAME_AND_CA,
        query = "select m.id from MiReportUserDefinedEntity m "
                + "where m.reportName = :reportName "
                + "and m.competentAuthority = :competentAuthority"
)
@Table(name = "mi_report_user_defined")
public class MiReportUserDefinedEntity {
	
    public static final String NAMED_QUERY_FIND_ALL_BY_CA = "MiReportUserDefinedEntity.findAllByCA";
    public static final String NAMED_QUERY_FIND_BY_REPORT_NAME_AND_CA = "MiReportUserDefinedEntity.findByReportNameAndCA";

    @Id
    @SequenceGenerator(name = "mi_report_user_defined_id_generator", sequenceName = "mi_report_user_defined_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mi_report_user_defined_id_generator")
    private Long id;

    @Column(name = "report_name", nullable = false)
    @NotBlank
    private String reportName;

    @Column(name = "description")
    @Size(max = 10000)
    private String description;

    @Column(name = "query_definition")
    @Size(max = 10000)
    @NotNull
    private String queryDefinition;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "competent_authority")
    @NotNull
    private CompetentAuthorityEnum competentAuthority;

    /** The user id */
    @Column(name = "created_by")
    @NotNull
    private String createdBy;

    @NotNull
    @Column(name = "last_updated_on")
    @LastModifiedDate
    private LocalDateTime lastUpdatedOn;
}
