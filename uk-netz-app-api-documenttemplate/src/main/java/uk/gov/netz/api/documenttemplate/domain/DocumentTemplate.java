package uk.gov.netz.api.documenttemplate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.documenttemplate.domain.dto.DocumentTemplateInfoDTO;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "document_template")
@SqlResultSetMapping(
        name = DocumentTemplate.DOCUMENT_TEMPLATE_INFO_DTO_RESULT_MAPPER,
        classes = {
                @ConstructorResult(
                        targetClass = DocumentTemplateInfoDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "name"),
                                @ColumnResult(name = "roleType"),
                                @ColumnResult(name = "workflow"),
                                @ColumnResult(name = "lastUpdatedDate", type = LocalDateTime.class)
                        }
                )})
public class DocumentTemplate {

    public static final String DOCUMENT_TEMPLATE_INFO_DTO_RESULT_MAPPER = "DocumentTemplateInfoDTOResultMapper";

    @Id
    @SequenceGenerator(name = "document_template_id_generator", sequenceName = "document_template_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_template_id_generator")
    private Long id;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(name = "type")
    private String type;

    @EqualsAndHashCode.Include
    @Enumerated(EnumType.STRING)
    @Column(name = "competent_authority")
    @NotNull
    private CompetentAuthorityEnum competentAuthority;

    @NotBlank
    @Column(name = "name")
    private String name;

    @Column(name = "workflow")
    @NotBlank
    private String workflow;
    
    @Column(name = "role_type")
    private String roleType;

    @NotNull
    @Column(name = "file_document_template_id", unique = true)
    private Long fileDocumentTemplateId;
    
    @Column(name = "notification_template_id")
    private Long notificationTemplateId;

    @Column(name = "process_required")
    private boolean processRequired;

    @Column(name = "convert_required")
    private boolean convertRequired;
    
    @Column(name = "last_updated_date")
    @LastModifiedDate
    @NotNull
    private LocalDateTime lastUpdatedDate;

}
