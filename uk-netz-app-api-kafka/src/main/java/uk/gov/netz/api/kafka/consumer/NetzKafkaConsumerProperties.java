package uk.gov.netz.api.kafka.consumer;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Consumer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;

import lombok.Data;

@Data
public class NetzKafkaConsumerProperties {
	
	public static final long RETRY_INTERVAL_DEFAULT_VALUE = 5000;
	public static final long RETRY_MAX_ATTEMPTS_DEFAULT_VALUE = 3;

	protected Consumer consumer;
	protected Long retryInterval; //milliseconds
	protected Long retryMaxAttempts;
	
	protected Producer dlqProducer;
	
}
