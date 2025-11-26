package uk.gov.netz.api.thirdpartydataprovider.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProvidersResponseDTO {

    private boolean editable;

    private List<ThirdPartyDataProviderDTO> thirdPartyDataProviders;
}
