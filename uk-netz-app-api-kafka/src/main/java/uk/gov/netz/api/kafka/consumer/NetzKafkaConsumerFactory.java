package uk.gov.netz.api.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.IsolationLevel;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;
import uk.gov.netz.api.kafka.producer.NetzKafkaProducerFactory;
import uk.gov.netz.api.kafka.producer.NetzKafkaProducerProperties;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@Log4j2
@Component
@RequiredArgsConstructor
public class NetzKafkaConsumerFactory<K, V> {
	
	private final KafkaProperties kafkaProperties;
	private final NetzKafkaProducerFactory<K, V> netzKafkaProducerFactory;
	
	public ConcurrentKafkaListenerContainerFactory<K, V> createKafkaListenerContainerFactory(
			String groupId,
			Class<V> valueType) {
		return createKafkaListenerContainerFactory(groupId, null, valueType);
	}

	public ConcurrentKafkaListenerContainerFactory<K, V> createKafkaListenerContainerFactory(
			String groupId,
			NetzKafkaConsumerProperties netzKafkaConsumerProperties,
			Class<V> valueType) {
		Objects.requireNonNull(groupId);

		ConsumerFactory<K, V> consumerFactory = createConsumerFactory(groupId,
				netzKafkaConsumerProperties, valueType);

		DefaultErrorHandler errorHandler = getErrorHandler(netzKafkaConsumerProperties);

		ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.setCommonErrorHandler(errorHandler);
		factory.getContainerProperties().setMissingTopicsFatal(true);

		return factory;
	}

	private DefaultErrorHandler getErrorHandler(NetzKafkaConsumerProperties netzKafkaConsumerProperties) {
		KafkaTemplate<K, V> dlqKafkaTemplate = createDLQKafkaTemplate(netzKafkaConsumerProperties);
		
		BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> dlqResolver = (record, e) -> {
			log.error(String.format(
					"Error when trying to consume record with headers %s for topic %s and record key %s and record payload %s .Error message: %s",
					record.headers().toString(), record.topic(), record.key(), record.value(), e.getMessage()), e);
			return new TopicPartition(record.topic() + "-dlt", record.partition());
		};
		
		DeadLetterPublishingRecoverer dlqRecoverer = new DeadLetterPublishingRecoverer(dlqKafkaTemplate, dlqResolver);
		dlqRecoverer.setFailIfSendResultIsError(false);
		dlqRecoverer.setLogRecoveryRecord(true);
		
		DefaultErrorHandler errorHandler = new DefaultErrorHandler(dlqRecoverer,
				new FixedBackOff(
						netzKafkaConsumerProperties != null && netzKafkaConsumerProperties.getRetryInterval() != null
								? netzKafkaConsumerProperties.getRetryInterval()
								: NetzKafkaConsumerProperties.RETRY_INTERVAL_DEFAULT_VALUE,
						netzKafkaConsumerProperties != null && netzKafkaConsumerProperties.getRetryMaxAttempts() != null
								? netzKafkaConsumerProperties.getRetryMaxAttempts()
								: NetzKafkaConsumerProperties.RETRY_MAX_ATTEMPTS_DEFAULT_VALUE));

		errorHandler.defaultFalse(); //any exception is classified as non retryable
		errorHandler.addRetryableExceptions(KafkaRetryableException.class);
		return errorHandler;
	}

	private KafkaTemplate<K, V> createDLQKafkaTemplate(NetzKafkaConsumerProperties netzKafkaConsumerProperties) {
		NetzKafkaProducerProperties dlqNetzProducerProperties = null;

		if (netzKafkaConsumerProperties != null &&
				netzKafkaConsumerProperties.getDlqProducer() != null) {
			dlqNetzProducerProperties = new NetzKafkaProducerProperties();
			dlqNetzProducerProperties.setProducer(netzKafkaConsumerProperties.getDlqProducer());
		}

		return netzKafkaProducerFactory.createKafkaTemplate(dlqNetzProducerProperties);
	}

	private ConsumerFactory<K, V> createConsumerFactory(String groupId, NetzKafkaConsumerProperties netzKafkaConsumerProperties, Class<V> valueType) {

		// common kafka properties
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));

		//topic-specific kafka properties if exist 
		if(netzKafkaConsumerProperties != null &&
				netzKafkaConsumerProperties.getConsumer() != null) {
			props.putAll(netzKafkaConsumerProperties.getConsumer().buildProperties(null));
		}

		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
		props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
		props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType);
		props.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, true);
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, IsolationLevel.READ_COMMITTED.toString().toLowerCase(Locale.ROOT));
		props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, NetzLoggingConsumerInterceptor.class.getName());

        return new DefaultKafkaConsumerFactory<>(props);
	}

}
