package uk.gov.netz.api.thirdpartydataprovider.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThirdPartyDataProviderNameInfoDTO {
    
    private Long id;
    private String name;
}
