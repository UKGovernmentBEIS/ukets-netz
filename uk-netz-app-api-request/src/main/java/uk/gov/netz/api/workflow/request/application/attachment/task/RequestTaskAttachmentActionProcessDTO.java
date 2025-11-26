package uk.gov.netz.api.workflow.request.application.attachment.task;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestTaskAttachmentActionProcessDTO {

    @NotNull
    private Long requestTaskId;

    @NotNull
    private String requestTaskActionType;

}
