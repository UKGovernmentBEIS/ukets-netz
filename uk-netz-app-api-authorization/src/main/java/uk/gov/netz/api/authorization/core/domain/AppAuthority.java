package uk.gov.netz.api.authorization.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

/**
 * The authenticated User's applicable accounts.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AppAuthority {

    private String code;

    private Long accountId;

    private CompetentAuthorityEnum competentAuthority;

    private Long verificationBodyId;
    
    private Long thirdPartyDataProviderId;

    private List<String> permissions;
}
