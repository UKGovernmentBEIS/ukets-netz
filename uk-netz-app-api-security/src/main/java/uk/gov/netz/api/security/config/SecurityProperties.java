package uk.gov.netz.api.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {

    private List<String> unauthenticatedApis = Collections.singletonList("");

}
