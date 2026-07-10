package uk.gov.netz.api.configuration.domain.ui;

import lombok.Builder;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Validated
@Data
@Builder
public class UIConfigurationPropertiesDTO {
	
    private Map<String, Boolean> features;
    private Map<String, String> analytics;
    private Map<String, String> properties;
    private String keycloakServerUrl;
    
}
