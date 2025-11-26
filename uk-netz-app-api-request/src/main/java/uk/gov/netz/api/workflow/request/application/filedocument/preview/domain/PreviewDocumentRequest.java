package uk.gov.netz.api.workflow.request.application.filedocument.preview.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.workflow.request.flow.common.domain.DecisionNotification;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviewDocumentRequest {

    @NotNull
    private String documentType;

    @NotNull
    @Valid
    private DecisionNotification decisionNotification;
}
