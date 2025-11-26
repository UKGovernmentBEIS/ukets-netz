package uk.gov.netz.api.workflow.payment.config.property;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Validated
@Data
@ConfigurationProperties(prefix = "govuk-pay")
@ConditionalOnProperty(prefix = "govuk-pay", name = "isActive", havingValue = "true")
public class GovukPayProperties {

    @Valid
    @NotBlank
    private String serviceUrl;

    @Valid
    @NotBlank
    private String confirmationReturnUrl;

    @Valid
    @NotEmpty
    private Map<String, @NotBlank String> apiKeys = new HashMap<>();
}
