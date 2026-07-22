package uk.gov.netz.docgenerator.client.kafka;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import uk.gov.netz.api.kafka.consumer.NetzKafkaConsumerProperties;

@Validated
@ConfigurationProperties(prefix = "kafka.docgen-consumer")
@Getter
@Setter
public class DocGenKafkaConsumerConfigProperties extends NetzKafkaConsumerProperties {

    private boolean enabled;
    @NotBlank
    private String topic = "doc.converted";
    @NotBlank
    private String group = "app-api-doc-result-consumer";
}
