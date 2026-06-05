package uk.gov.netz.api.kafka.correlation;

@FunctionalInterface
public interface KafkaCorrelationParentIdResolver {

    String resolveParentCorrelationId();
}
