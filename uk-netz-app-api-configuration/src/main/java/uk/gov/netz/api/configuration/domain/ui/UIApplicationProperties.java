package uk.gov.netz.api.configuration.domain.ui;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "ui")
@Getter
@Setter
public class UIApplicationProperties {
	
	private Map<String, Boolean> features;
	private Map<String, String> analytics;
	private Map<String, String> properties;
    private String keycloakServerUrl;
    
}
