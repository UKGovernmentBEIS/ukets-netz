package uk.gov.netz.api.kafka.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaCorrelationContextScopeTest {

    @AfterEach
    void tearDown() {
        KafkaCorrelationContextHolder.clear();
    }

    @Test
    void scope_clears_context_on_success_when_no_previous_context_exists() {
        try (KafkaCorrelationContextScope ignored = KafkaCorrelationContextHolder.open(context("C", "O"))) {
            assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isEqualTo("C");
            assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isEqualTo("O");
        }

        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isNull();
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isNull();
    }

    @Test
    void scope_clears_context_when_exception_is_thrown() {
        assertThatThrownBy(() -> {
            try (KafkaCorrelationContextScope ignored = KafkaCorrelationContextHolder.open(context("C", "O"))) {
                throw new IllegalStateException("boom");
            }
        }).isInstanceOf(IllegalStateException.class);

        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isNull();
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isNull();
    }

    @Test
    void nested_scope_restores_previous_context() {
        try (KafkaCorrelationContextScope ignored = KafkaCorrelationContextHolder.open(context("outer", "root"))) {
            try (KafkaCorrelationContextScope nested = KafkaCorrelationContextHolder.open(context("inner", "parent"))) {
                assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isEqualTo("inner");
                assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isEqualTo("parent");
            }

            assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isEqualTo("outer");
            assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isEqualTo("root");
        }
    }

    private static KafkaCorrelationContext context(String correlationId, String parentCorrelationId) {
        return KafkaCorrelationContext.builder()
                .correlationId(correlationId)
                .parentCorrelationId(parentCorrelationId)
                .build();
    }
}
