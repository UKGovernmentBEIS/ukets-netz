package uk.gov.netz.api.kafka.producer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.CompositeProducerInterceptor;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import uk.gov.netz.api.kafka.correlation.KafkaCorrelationParentIdResolver;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

@Component
public class NetzKafkaProducerFactory<K, V> {
	
	private final KafkaProperties kafkaProperties;
	private final KafkaCorrelationHeaderProducerInterceptor<K, V> correlationHeaderInterceptor;
	private final KafkaCorrelationParentHeaderProducerInterceptor<K, V> correlationParentHeaderInterceptor;

	@Autowired
	public NetzKafkaProducerFactory(
			KafkaProperties kafkaProperties,
			KafkaCorrelationHeaderProducerInterceptor<K, V> correlationHeaderInterceptor,
			ObjectProvider<KafkaCorrelationParentHeaderProducerInterceptor<K, V>> correlationParentHeaderInterceptor,
			ObjectProvider<KafkaCorrelationParentIdResolver> parentIdResolvers) {
		this(kafkaProperties, correlationHeaderInterceptor,
				correlationParentHeaderInterceptor.getIfAvailable(() ->
						new KafkaCorrelationParentHeaderProducerDefaultInterceptor<>(
								parentIdResolvers.orderedStream().toList())));
	}

	public NetzKafkaProducerFactory(
			KafkaProperties kafkaProperties,
			KafkaCorrelationHeaderProducerInterceptor<K, V> correlationHeaderInterceptor,
			KafkaCorrelationParentHeaderProducerInterceptor<K, V> correlationParentHeaderInterceptor) {
		this.kafkaProperties = kafkaProperties;
		this.correlationHeaderInterceptor = correlationHeaderInterceptor;
		this.correlationParentHeaderInterceptor = correlationParentHeaderInterceptor;
	}

	public KafkaTemplate<K, V> createKafkaTemplate() {
		return createKafkaTemplate(null);
	}

	public KafkaTemplate<K, V> createKafkaTemplate(NetzKafkaProducerProperties netzKafkaProducerProperties) {
		ProducerFactory<K, V> producerFactory = createProducerFactory(netzKafkaProducerProperties);
		KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate<>(producerFactory);
		try (ClientIdHeaderProducerInterceptor<K, V> clientIdHeaderProducerInterceptor = new ClientIdHeaderProducerInterceptor<>();
				CompositeProducerInterceptor<K, V> compositeProducerInterceptor = new CompositeProducerInterceptor<>(
						clientIdHeaderProducerInterceptor, correlationHeaderInterceptor, correlationParentHeaderInterceptor);) {
			clientIdHeaderProducerInterceptor.configure(producerFactory.getConfigurationProperties());
			correlationHeaderInterceptor.configure(producerFactory.getConfigurationProperties());
			kafkaTemplate.setProducerInterceptor(compositeProducerInterceptor);
			kafkaTemplate.setProducerListener(new NetzLoggingProducerListener<>());
		}
		return kafkaTemplate;
	}

	private ProducerFactory<K, V> createProducerFactory(NetzKafkaProducerProperties netzKafkaProducerProperties) {
		//common kafka properties
		Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
		// producer's buildProperties method doesn't set transaction-id-prefix. should be manually set
		populateTransactionIdPrefix(kafkaProperties.getProducer().getTransactionIdPrefix() != null, props,
				kafkaProperties.getProducer().getTransactionIdPrefix());

		//topic-specific kafka properties if exist 
		if (netzKafkaProducerProperties != null &&
				netzKafkaProducerProperties.getProducer() != null) {
			props.putAll(netzKafkaProducerProperties.getProducer().buildProperties(null));
			populateTransactionIdPrefix(netzKafkaProducerProperties.isTransactional(), props,
					netzKafkaProducerProperties.getProducer().getTransactionIdPrefix());
		}

		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		props.put(ProducerConfig.ACKS_CONFIG, "all");

		DefaultKafkaProducerFactory<K, V> factory = new DefaultKafkaProducerFactory<>(props);
		factory.setMaxAge(Duration.ofSeconds(Long.parseLong(props
			.getOrDefault(KafkaConstants.PRODUCER_MAX_AGE_SECONDS, KafkaConstants.DEFAULT_PRODUCER_MAX_AGE_SECONDS).toString())));
		return factory;
	}

	private void populateTransactionIdPrefix(boolean isTransactional, Map<String, Object> props, String transactionIdPrefix) {
		if(isTransactional && transactionIdPrefix != null) {
			props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionIdPrefix + UUID.randomUUID() + "-");
		}
	}

}
