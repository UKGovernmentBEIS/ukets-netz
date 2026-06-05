package uk.gov.netz.api.kafka.correlation;

public final class KafkaCorrelationContextScope implements AutoCloseable {

    private final KafkaCorrelationContext previousContext;
    private boolean closed;

    KafkaCorrelationContextScope(KafkaCorrelationContext previousContext) {
        this.previousContext = previousContext;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        KafkaCorrelationContextHolder.setContext(previousContext);
        closed = true;
    }
}
