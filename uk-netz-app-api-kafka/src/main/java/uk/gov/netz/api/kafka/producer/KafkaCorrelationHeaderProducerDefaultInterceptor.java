package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Component;

import uk.gov.netz.api.kafka.correlation.KafkaCorrelationContextHolder;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaCorrelationHeaderProducerDefaultInterceptor<K, V> implements KafkaCorrelationHeaderProducerInterceptor<K, V> {

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        if (record.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER) == null) {
            String correlationId = KafkaCorrelationContextHolder.getCorrelationId();
            record.headers().add(KafkaConstants.CORRELATION_ID_HEADER,
                    (correlationId != null ? correlationId : UUID.randomUUID().toString())
                            .getBytes(StandardCharsets.UTF_8));
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
