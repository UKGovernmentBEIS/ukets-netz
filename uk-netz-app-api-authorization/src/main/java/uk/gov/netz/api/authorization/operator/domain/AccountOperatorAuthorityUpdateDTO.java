package uk.gov.netz.api.authorization.operator.domain;

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
public class AccountOperatorAuthorityUpdateDTO {

    @NotBlank
    private String userId;

    @RoleCode(roleType = RoleTypeConstants.OPERATOR)
    private String roleCode;

    @NotNull
    private AuthorityStatus authorityStatus;
    
}
