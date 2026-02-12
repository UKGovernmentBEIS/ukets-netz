package uk.gov.netz.api.thirdpartydataprovider.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProviderDTO {

    @NotNull
    private Long id;

	@NotBlank
    private String name;

    @NotBlank
    private String clientId;

    @NotBlank
    @URL(protocol = "https")
    @With
    private String jwksUrl;
}
