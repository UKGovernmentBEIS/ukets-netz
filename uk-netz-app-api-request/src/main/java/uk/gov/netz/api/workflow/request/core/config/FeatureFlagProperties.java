package uk.gov.netz.api.workflow.request.core.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "feature-flag")
@Getter
@Setter
public class FeatureFlagProperties {

    private Set<String> disabledWorkflows = new HashSet<>();
}
