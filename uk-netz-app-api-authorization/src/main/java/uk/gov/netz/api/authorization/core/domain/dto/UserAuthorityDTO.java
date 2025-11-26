package uk.gov.netz.api.authorization.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserAuthorityDTO {
    private String userId;
    private String roleName;
    private String roleCode;
    private AuthorityStatus authorityStatus;
    private LocalDateTime authorityCreationDate;
}
