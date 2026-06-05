package uk.gov.netz.api.kafka.correlation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaCorrelationContextHolderTest {

    @AfterEach
    void tearDown() {
        KafkaCorrelationContextHolder.clear();
    }

    @Test
    void set_and_get_correlation_context_values() {
        KafkaCorrelationContextHolder.setCorrelationId("C");
        KafkaCorrelationContextHolder.setParentCorrelationId("O");

        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isEqualTo("C");
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isEqualTo("O");
    }

    @Test
    void unset_values_are_null() {
        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isNull();
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isNull();
    }

    @Test
    void clear_removes_context() {
        KafkaCorrelationContextHolder.setCorrelationId("C");
        KafkaCorrelationContextHolder.setParentCorrelationId("O");

        KafkaCorrelationContextHolder.clear();

        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isNull();
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isNull();
    }

    @Test
    void values_do_not_leak_across_threads() throws ExecutionException, InterruptedException {
        KafkaCorrelationContextHolder.setCorrelationId("C");
        KafkaCorrelationContextHolder.setParentCorrelationId("O");

        String otherThreadCorrelationId = CompletableFuture
                .supplyAsync(KafkaCorrelationContextHolder::getCorrelationId)
                .get();
        String otherThreadParentCorrelationId = CompletableFuture
                .supplyAsync(KafkaCorrelationContextHolder::getParentCorrelationId)
                .get();

        assertThat(otherThreadCorrelationId).isNull();
        assertThat(otherThreadParentCorrelationId).isNull();
        assertThat(KafkaCorrelationContextHolder.getCorrelationId()).isEqualTo("C");
        assertThat(KafkaCorrelationContextHolder.getParentCorrelationId()).isEqualTo("O");
    }
}
