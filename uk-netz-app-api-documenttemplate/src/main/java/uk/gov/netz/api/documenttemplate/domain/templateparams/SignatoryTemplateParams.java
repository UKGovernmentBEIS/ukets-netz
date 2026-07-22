package uk.gov.netz.api.documenttemplate.domain.templateparams;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatoryTemplateParams {

    @NotBlank
    private String fullName;
    
    private String jobTitle;
    
    @NotEmpty
    private byte[] signature;
}
