package uk.gov.netz.api.companieshouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountyAddressDTO {

    @NotBlank(message = "{address.line1.notEmpty}")
    @Size(max = 255, message = "{address.line1.typeMismatch}")
    @JsonProperty("address_line_1")
    private String line1;

    @Size(max = 255, message = "{address.line2.typeMismatch}")
    @JsonProperty("address_line_2")
    private String line2;

    @NotBlank(message = "{address.city.notEmpty}")
    @Size(max = 255, message = "{address.city.typeMismatch}")
    @JsonProperty("locality")
    private String city;

    @NotBlank(message = "{address.county.notEmpty}")
    @Size(max = 255, message = "{address.county.typeMismatch}")
    @JsonProperty("region")
    private String county;

    @NotBlank(message = "{address.postcode.notEmpty}")
    @Size(max = 64, message = "{address.postcode.typeMismatch}")
    @JsonProperty("postal_code")
    private String postcode;
}
