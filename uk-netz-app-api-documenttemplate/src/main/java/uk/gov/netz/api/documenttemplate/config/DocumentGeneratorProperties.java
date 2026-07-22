package uk.gov.netz.api.documenttemplate.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "document-generator")
@Data
public class DocumentGeneratorProperties {

    @Valid
    @NotEmpty @URL
    private String url;
}
