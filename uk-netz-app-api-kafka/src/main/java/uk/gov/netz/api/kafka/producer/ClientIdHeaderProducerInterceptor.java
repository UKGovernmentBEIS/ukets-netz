package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.util.Map;

public class ClientIdHeaderProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {
    
    private String clientId;

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        if (record.headers().lastHeader(KafkaConstants.PRODUCER_CLIENT_ID_HEADER) == null) {
            record.headers().add(KafkaConstants.PRODUCER_CLIENT_ID_HEADER, this.clientId.getBytes());
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
        this.clientId = (String) configs.get(CommonClientConfigs.CLIENT_ID_CONFIG);
    }
}
