package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.ObjectProvider;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationContextHolder;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationParentIdResolver;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Adds a parent correlation id header when one is available.
 *
 * <p>Resolution order: existing header, configured resolvers, Kafka context,
 * no header.
 */
public class KafkaCorrelationParentHeaderProducerDefaultInterceptor<K, V>
        implements KafkaCorrelationParentHeaderProducerInterceptor<K, V> {

    private final List<KafkaCorrelationParentIdResolver> parentIdResolvers;

    public KafkaCorrelationParentHeaderProducerDefaultInterceptor(
            ObjectProvider<KafkaCorrelationParentIdResolver> parentIdResolvers) {
        this(parentIdResolvers.orderedStream().toList());
    }

    public KafkaCorrelationParentHeaderProducerDefaultInterceptor(
            List<KafkaCorrelationParentIdResolver> parentIdResolvers) {
        this.parentIdResolvers = parentIdResolvers != null ? List.copyOf(parentIdResolvers) : List.of();
    }

    @Override
    public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
        if (record.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER) != null) {
            return record;
        }

        String parentCorrelationId = resolveParentCorrelationId();
        if (parentCorrelationId != null) {
            record.headers().add(KafkaConstants.CORRELATION_PARENT_ID_HEADER,
                    parentCorrelationId.getBytes(StandardCharsets.UTF_8));
        }

        return record;
    }

    private String resolveParentCorrelationId() {
        for (KafkaCorrelationParentIdResolver parentIdResolver : parentIdResolvers) {
            String parentCorrelationId = parentIdResolver.resolveParentCorrelationId();
            if (parentCorrelationId != null) {
                return parentCorrelationId;
            }
        }

        return KafkaCorrelationContextHolder.getParentCorrelationId();
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
