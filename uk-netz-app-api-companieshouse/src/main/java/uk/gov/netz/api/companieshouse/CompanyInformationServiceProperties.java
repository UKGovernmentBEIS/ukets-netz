package uk.gov.netz.api.companieshouse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@ConfigurationProperties(prefix = "company-information-service")
public class CompanyInformationServiceProperties {

    @Valid
    @NotBlank
    private String url;

    @Valid
    @NotBlank
    private String apiKey;
}
