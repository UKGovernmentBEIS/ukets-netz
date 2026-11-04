package uk.gov.netz.api.feedback.domain;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "feedback")
public class Feedback {

    @Id
    @SequenceGenerator(name = "feedback_id_generator", sequenceName = "feedback_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedback_id_generator")
    private Long id;

    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    @NotNull
    private FeedbackPayload payload;

    @Column(name = "submitter_id")
    @NotNull
    private String submitterId;

    @NotNull
    @Column(name = "submission_date", nullable = false, updatable = false)
    private LocalDateTime submissionDate;
}
