package uk.gov.netz.api.authorization.regulator.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegulatorUserUpdateStatusDTO {

    @NotBlank
    private String userId;

    @NotNull
    private AuthorityStatus authorityStatus;
    
}
