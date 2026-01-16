package uk.gov.netz.api.restlogging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestLoggingConfig {

	@Bean
    @ConfigurationProperties("rest.logging")
    RestLoggingProperties restLoggingProperties() {
        return new RestLoggingProperties();
    }

}
