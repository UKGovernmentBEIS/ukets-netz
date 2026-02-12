package uk.gov.netz.api.thirdpartydataprovider.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProviderClientResponseDTO {
    private String name;
    private String clientId;
    private String jwksUrl;
    private String serviceAccountUserId;
}
