package uk.gov.netz.api.kafka.correlation;

public final class KafkaCorrelationContextHolder {

    private static final ThreadLocal<KafkaCorrelationContext> CONTEXT = new ThreadLocal<>();

    private KafkaCorrelationContextHolder() {
    }

    public static KafkaCorrelationContextScope open(KafkaCorrelationContext context) {
        KafkaCorrelationContext previousContext = CONTEXT.get();
        setContext(context);
        return new KafkaCorrelationContextScope(previousContext);
    }

    public static void setCorrelationId(String correlationId) {
        KafkaCorrelationContext current = CONTEXT.get();
        setContext(KafkaCorrelationContext.builder()
                .correlationId(correlationId)
                .parentCorrelationId(current != null ? current.getParentCorrelationId() : null)
                .build());
    }

    public static String getCorrelationId() {
        KafkaCorrelationContext context = CONTEXT.get();
        return context != null ? context.getCorrelationId() : null;
    }

    public static void setParentCorrelationId(String parentCorrelationId) {
        KafkaCorrelationContext current = CONTEXT.get();
        setContext(KafkaCorrelationContext.builder()
                .correlationId(current != null ? current.getCorrelationId() : null)
                .parentCorrelationId(parentCorrelationId)
                .build());
    }

    public static String getParentCorrelationId() {
        KafkaCorrelationContext context = CONTEXT.get();
        return context != null ? context.getParentCorrelationId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    static void setContext(KafkaCorrelationContext context) {
        if (context == null) {
            clear();
        } else {
            CONTEXT.set(context);
        }
    }
}
