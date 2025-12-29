package uk.gov.netz.api.notification.template.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Length;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.domain.dto.NotificationTemplateInfoDTO;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification_template")
@NamedEntityGraph(
        name = "notification-template-with-text",
        attributeNodes = {
                @NamedAttributeNode("text")
        })
@NamedQuery(
        name = NotificationTemplate.NAMED_QUERY_FIND_MANAGED_NOTIFICATION_TEMPLATE_BY_ID,
        query = "select template from NotificationTemplate template "
                + "where template.id = :id "
                + "and template.managed = true")
@SqlResultSetMapping(
        name = NotificationTemplate.NOTIFICATION_TEMPLATE_INFO_DTO_RESULT_MAPPER,
        classes = {
                @ConstructorResult(
                        targetClass = NotificationTemplateInfoDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = Long.class),
                                @ColumnResult(name = "name"),
                                @ColumnResult(name = "roleType"),
                                @ColumnResult(name = "workflow"),
                                @ColumnResult(name = "lastUpdatedDate", type = LocalDateTime.class)
                        }
                )})
public class NotificationTemplate {

    public static final String NOTIFICATION_TEMPLATE_INFO_DTO_RESULT_MAPPER = "NotificationTemplateInfoDTOResultMapper";
    public static final String NAMED_QUERY_FIND_MANAGED_NOTIFICATION_TEMPLATE_BY_ID = "NotificationTemplate.findManagedNotificationTemplateById";

    @Id
    @SequenceGenerator(name = "notification_template_id_generator", sequenceName = "notification_template_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_template_id_generator")
    private Long id;

    @EqualsAndHashCode.Include()
    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "subject")
    private String subject;

    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "text", length = Length.LOB_DEFAULT)
    private String text;

    @EqualsAndHashCode.Include()
    @Enumerated(EnumType.STRING)
    @Column(name = "competent_authority")
    private CompetentAuthorityEnum competentAuthority;

    @Column(name = "event_trigger")
    private String eventTrigger;

    @Column(name = "workflow")
    private String workflow;

    @Column(name = "role_type")
    private String roleType;

    @Column(name = "is_managed", columnDefinition = "boolean default false")
    private boolean managed;

    @Column(name = "last_updated_date")
    @LastModifiedDate
    private LocalDateTime lastUpdatedDate;

}
