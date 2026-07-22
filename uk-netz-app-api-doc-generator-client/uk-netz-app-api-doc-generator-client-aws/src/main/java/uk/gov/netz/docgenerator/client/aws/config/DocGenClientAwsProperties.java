package uk.gov.netz.docgenerator.client.aws.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = DocGenClientAwsProperties.PREFIX)
public class DocGenClientAwsProperties {

    public static final String PREFIX = "docgen.client.aws";
    private boolean enabled = true;
}
