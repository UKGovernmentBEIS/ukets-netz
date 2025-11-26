package uk.gov.netz.api.authorization.verifier.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.authorization.core.domain.dto.RoleCode;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifierAuthorityUpdateDTO {

    @NotBlank
    private String userId;

    @NotNull
    private AuthorityStatus authorityStatus;

    @RoleCode(roleType = RoleTypeConstants.VERIFIER)
    private String roleCode;
}
