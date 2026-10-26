package uk.gov.netz.api.request.feedback.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "request_feedback")
public class RequestFeedback {

    @Id
    @SequenceGenerator(name = "request_feedback_id_generator", sequenceName = "request_feedback_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_feedback_id_generator")
    private Long id;

    @Column(name = "request_id")
    @NotNull
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull
    private RequestFeedbackStatus status;

    @Column(name = "type", nullable = false)
    @NotNull
    private String type;

    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private RequestFeedbackPayload payload;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_by_id")
    private String createdById;

    @Column(name = "submitter")
    private String submitter;

    @Column(name = "submitter_id")
    private String submitterId;

    @NotNull
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;
}
