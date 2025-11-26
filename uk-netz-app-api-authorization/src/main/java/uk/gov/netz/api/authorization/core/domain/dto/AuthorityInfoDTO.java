package uk.gov.netz.api.authorization.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AuthorityStatus;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorityInfoDTO {

    private Long id;
    private String userId;
    private AuthorityStatus authorityStatus;
    private LocalDateTime creationDate;
    private Long accountId;
    private String code;
    private CompetentAuthorityEnum competentAuthority;
    private Long verificationBodyId;
    private Long thirdPartyDataProviderId;
}