package uk.gov.netz.api.kafka.producer;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NetzKafkaProducerProperties {
	
	protected Producer producer;
	protected boolean transactional = true;
	
}
