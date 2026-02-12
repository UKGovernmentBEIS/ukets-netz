package uk.gov.netz.api.thirdpartydataprovider.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdPartyDataProviderCreateDTO {

	@NotBlank
    private String name;

    @NotBlank
    @URL(protocol = "https")
    private String jwksUrl;
}
