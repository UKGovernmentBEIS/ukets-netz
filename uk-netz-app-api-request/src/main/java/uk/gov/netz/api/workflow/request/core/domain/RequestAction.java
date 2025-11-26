package uk.gov.netz.api.workflow.request.core.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@EntityListeners({AuditingEntityListener.class})
@Table(name = "request_action")
public class RequestAction {

    @Id
    @SequenceGenerator(name = "request_action_id_generator", sequenceName = "request_action_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_action_id_generator")
    private Long id;

    @EqualsAndHashCode.Include
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(name = "type")
    private String type;

    @Basic(fetch = FetchType.LAZY)
    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private RequestActionPayload payload;

    @EqualsAndHashCode.Include
    @Column(name = "submitter_id")
    private String submitterId;
    
    @EqualsAndHashCode.Include
    @Column(name = "submitter")
    private String submitter;
    
    @NotNull
    @Column(name = "creation_date")
    @CreatedDate
    private LocalDateTime creationDate;

}
