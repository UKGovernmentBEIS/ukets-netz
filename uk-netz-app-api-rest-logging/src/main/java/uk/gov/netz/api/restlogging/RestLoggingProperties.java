package uk.gov.netz.api.restlogging;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.spi.StandardLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * Class representing application properties with prefix rest.logging.
 */
@ConfigurationProperties(prefix = "rest.logging")
@Getter
@Setter
public class RestLoggingProperties {

    /* List of uri patterns to be excluded from logging in successful actions (error cases are always logged). */
    private List<String> excludedUriPatterns = Collections.emptyList();
    
    /* List of urls that excluded at all during logging (neither successful nor error cases are logged) */
	private List<String> excludedTotallyUriPatterns = Collections.emptyList();
	
    private StandardLevel level = StandardLevel.INFO;
}
