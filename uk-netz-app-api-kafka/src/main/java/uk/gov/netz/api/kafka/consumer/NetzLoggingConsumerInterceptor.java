package uk.gov.netz.api.kafka.consumer;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry;
import uk.gov.netz.api.kafka.logging.KafkaLoggingEntry.Type;
import uk.gov.netz.api.kafka.utils.KafkaConstants;
import uk.gov.netz.api.kafka.utils.KafkaUtils;

@Log4j2
public class NetzLoggingConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

	@Override
	public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
		records.forEach(record -> log.info(KafkaLoggingEntry.builder()
				.type(Type.CONSUMING)
				.correlationId(KafkaUtils.resolveHeader(KafkaConstants.CORRELATION_ID_HEADER, record.headers()))
				.correlationParentId(
						KafkaUtils.resolveHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER, record.headers()))
				.clientId(KafkaUtils.resolveHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER, record.headers()))
				.recordKey(record.key())
				.recordValue(KafkaUtils.resolveRecordValueAsMap(record.value()))
				.topic(record.topic())
				.partition(record.partition())
				.offset(record.offset())
				.build()));
		return records;
	}

	@Override
	public void configure(Map<String, ?> configs) {

	}

	@Override
	public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {

	}

	@Override
	public void close() {

	}

}
