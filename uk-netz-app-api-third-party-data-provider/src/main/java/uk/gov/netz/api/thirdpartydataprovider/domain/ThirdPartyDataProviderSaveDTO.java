package uk.gov.netz.api.thirdpartydataprovider.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProviderSaveDTO {

	@NotBlank
    private String name;

    @NotBlank
    private String clientId;

    @NotBlank
    @With
    private String clientEntityId;
}
