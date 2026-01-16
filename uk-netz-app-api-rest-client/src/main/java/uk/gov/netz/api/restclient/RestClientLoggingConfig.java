package uk.gov.netz.api.restclient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.netz.api.restlogging.RestLoggingProperties;

@Configuration
public class RestClientLoggingConfig {

    @Bean
    @ConfigurationProperties("rest.client.logging")
    RestLoggingProperties restClientLoggingProperties() {
        return new RestLoggingProperties();
    }
    
}
