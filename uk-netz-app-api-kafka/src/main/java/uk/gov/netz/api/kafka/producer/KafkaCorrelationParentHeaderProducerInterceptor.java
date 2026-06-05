package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerInterceptor;

public interface KafkaCorrelationParentHeaderProducerInterceptor<K, V> extends ProducerInterceptor<K, V> {

}
