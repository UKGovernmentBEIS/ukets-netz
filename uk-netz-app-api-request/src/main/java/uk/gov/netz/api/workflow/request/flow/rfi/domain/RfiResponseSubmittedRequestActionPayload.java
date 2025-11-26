package uk.gov.netz.api.workflow.request.flow.rfi.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.common.validation.SpELExpression;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SpELExpression(expression = "{#rfiQuestionPayload.questions.size() == #rfiResponsePayload.answers.size()}", message = "rfi.questions.same.size.with.answers")
public class RfiResponseSubmittedRequestActionPayload extends RequestActionPayload {
    
    @NotNull
    @Valid
    private RfiQuestionPayload rfiQuestionPayload;

    @NotNull
    @Valid
    private RfiResponsePayload rfiResponsePayload;

    @Builder.Default
    private Map<UUID, String> rfiAttachments = new HashMap<>();
    
    @Override
    public Map<UUID, String> getAttachments() {
        return this.rfiAttachments;
    }
}
