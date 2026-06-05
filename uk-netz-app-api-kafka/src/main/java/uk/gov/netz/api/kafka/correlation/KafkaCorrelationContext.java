package uk.gov.netz.api.kafka.correlation;

public final class KafkaCorrelationContext {

    private final String correlationId;
    private final String parentCorrelationId;

    private KafkaCorrelationContext(Builder builder) {
        this.correlationId = builder.correlationId;
        this.parentCorrelationId = builder.parentCorrelationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getParentCorrelationId() {
        return parentCorrelationId;
    }

    public static final class Builder {
        private String correlationId;
        private String parentCorrelationId;

        private Builder() {
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder parentCorrelationId(String parentCorrelationId) {
            this.parentCorrelationId = parentCorrelationId;
            return this;
        }

        public KafkaCorrelationContext build() {
            return new KafkaCorrelationContext(this);
        }
    }
}
