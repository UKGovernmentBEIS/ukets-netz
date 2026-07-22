package uk.gov.netz.api.documenttemplate.domain.templateparams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTemplateParams {

    @NotBlank
    private String requestId;

    @NotBlank
    private String requestTypeInfo;

    @NotBlank
    private String requestType;

    @NotNull
    private Date requestSubmissionDate;

    @NotNull
    private LocalDateTime requestEndDate;
}
