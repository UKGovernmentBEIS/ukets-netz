package uk.gov.netz.api.kafka.producer;

import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import uk.gov.netz.api.kafka.utils.KafkaConstants;

public class TestKafkaCorrelationHeaderProducerInterceptor<K, V>
		implements KafkaCorrelationHeaderProducerInterceptor<K, V> {

	@Override
	public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
		if (record.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER) == null) {
			record.headers().add(KafkaConstants.CORRELATION_ID_HEADER, UUID.randomUUID().toString().getBytes());
		}
		return record;
	}

	@Override
	public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

	}

	@Override
	public void close() {

	}

	@Override
	public void configure(Map<String, ?> configs) {

	}
}
