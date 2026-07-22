package uk.gov.netz.docgenerator.client.kafka;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.netz.api.kafka.producer.KafkaCorrelationParentHeaderProducerInterceptor;

public class DocGenKafkaCorrelationParentHeaderProducerInterceptor<K, V>
    implements KafkaCorrelationParentHeaderProducerInterceptor<K, V> {

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
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
