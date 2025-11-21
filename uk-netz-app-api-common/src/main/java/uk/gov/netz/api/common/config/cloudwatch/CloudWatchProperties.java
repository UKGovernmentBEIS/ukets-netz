package uk.gov.netz.api.common.config.cloudwatch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "cloudwatch")
@Getter
@Setter
public class CloudWatchProperties {

    @Valid
    @NotEmpty
    private String enabled;

    @Valid
    @NotEmpty
    private String namespace;

    @Valid
    @NotEmpty
    private String batchSize;

    @Valid
    @NotEmpty
    private String step;

    @Valid
    @NotEmpty
    private String region;

    @Valid
    @NotEmpty
    private String awsEndpointUrl;
}
