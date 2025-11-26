package uk.gov.netz.api.thirdpartydataprovider.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProviderClientCreateResponseDTO {
    @JsonProperty("id")
    private String clientEntityId;
    private String name;
    private String clientId;
    private String clientSecret;
    private String serviceAccountUserId;
}
