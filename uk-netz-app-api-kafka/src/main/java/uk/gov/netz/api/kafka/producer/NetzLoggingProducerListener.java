package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.lang.Nullable;

import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry.Type;
import uk.gov.netz.api.kafka.utils.KafkaConstants;
import uk.gov.netz.api.kafka.utils.KafkaUtils;

@Log4j2
public class NetzLoggingProducerListener<K, V> implements ProducerListener<K, V> {

	@Override
	public void onSuccess(ProducerRecord<K, V> producerRecord, RecordMetadata recordMetadata) {
		log.info(KafkaLoggingEntry.builder()
				.type(Type.PRODUCING)
				.correlationId(KafkaUtils.resolveHeader(KafkaConstants.CORRELATION_ID_HEADER, producerRecord.headers()))
				.correlationParentId(KafkaUtils.resolveHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER, producerRecord.headers()))
				.clientId(KafkaUtils.resolveHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER, producerRecord.headers()))
				.recordKey(producerRecord.key())
				.recordValue(KafkaUtils.resolveRecordValueAsMap(producerRecord.value()))
				.topic(producerRecord.topic())
				.partition(recordMetadata.partition())
				.offset(recordMetadata.offset())
				.build());
	}
	
	@Override
	public void onError(ProducerRecord<K, V> producerRecord, @Nullable RecordMetadata recordMetadata,
						Exception exception) {
		log.error(KafkaLoggingEntry.builder()
				.type(Type.PRODUCING)
				.correlationId(KafkaUtils.resolveHeader(KafkaConstants.CORRELATION_ID_HEADER, producerRecord.headers()))
				.clientId(KafkaUtils.resolveHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER, producerRecord.headers()))
				.recordKey(producerRecord.key())
				.recordValue(KafkaUtils.resolveRecordValueAsMap(producerRecord.value()))
				.topic(producerRecord.topic())
				.partition(recordMetadata != null ? recordMetadata.partition() : producerRecord.partition())
				.offset(recordMetadata != null ? recordMetadata.offset() : null)
				.build(), exception);
	}
	
}
