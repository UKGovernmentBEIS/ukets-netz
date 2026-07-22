package uk.gov.netz.docgenerator.client.config;

import java.time.Duration;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = DocGenClientProperties.PREFIX)
public class DocGenClientProperties {

    public static final String PREFIX = "docgen.client";
    private String storageContainer = "doc-gen";
    private String syncQueueDestination = "doc-gen-sync-jobs";
    private String asyncQueueDestination = "doc-gen-async-jobs";
    private String messageGroupId;
    private Duration syncTimeout = Duration.ofSeconds(30);
    private Duration syncPollInterval = Duration.ofMillis(500);
    private Duration downloadUrlExpiry = Duration.ofMinutes(15);
    @Min(1)
    private int maxHandlerRetries = 3;
}
