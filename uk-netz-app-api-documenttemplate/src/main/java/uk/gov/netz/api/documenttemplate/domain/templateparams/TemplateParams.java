package uk.gov.netz.api.documenttemplate.domain.templateparams;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class TemplateParams {
    
    @Valid
    private CompetentAuthorityTemplateParams competentAuthorityParams;
    
    private String competentAuthorityCentralInfo;
    
    @Valid
    private SignatoryTemplateParams signatoryParams;
    
    @Valid
    private AccountTemplateParams accountParams;

    private String permitId;

    @Valid
    private WorkflowTemplateParams workflowParams;

    @Builder.Default
    private Map<String, Object> params = new HashMap<>();
}
