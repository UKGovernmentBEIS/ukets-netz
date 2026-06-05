package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationContextHolder;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationParentIdResolver;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaCorrelationParentHeaderProducerDefaultInterceptorTest {

    private static final String TOPIC = "test-topic";
    private static final String EXISTING_PARENT = "existing-parent";
    private static final String RESOLVED_HTTP_PARENT = "http-parent";
    private static final String CONTEXT_PARENT = "context-parent";

    @AfterEach
    void tearDown() {
        KafkaCorrelationContextHolder.clear();
    }

    @Test
    void explicit_parent_header_wins() {
        KafkaCorrelationContextHolder.setParentCorrelationId(CONTEXT_PARENT);
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptor(() -> RESOLVED_HTTP_PARENT);
        ProducerRecord<String, String> record = newRecord();
        record.headers().add(KafkaConstants.CORRELATION_PARENT_ID_HEADER,
                EXISTING_PARENT.getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, String> result = interceptor.onSend(record);

        assertThat(parentHeader(result)).isEqualTo(EXISTING_PARENT);
    }

    @Test
    void resolver_fallback_wins_over_context_when_header_absent() {
        KafkaCorrelationContextHolder.setParentCorrelationId(CONTEXT_PARENT);
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptor(() -> RESOLVED_HTTP_PARENT);

        ProducerRecord<String, String> result = interceptor.onSend(newRecord());

        assertThat(parentHeader(result)).isEqualTo(RESOLVED_HTTP_PARENT);
    }

    @Test
    void resolver_fallback_is_used_when_context_absent() {
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptor(() -> RESOLVED_HTTP_PARENT);

        ProducerRecord<String, String> result = interceptor.onSend(newRecord());

        assertThat(parentHeader(result)).isEqualTo(RESOLVED_HTTP_PARENT);
    }

    @Test
    void context_parent_used_when_header_and_resolver_value_absent() {
        KafkaCorrelationContextHolder.setParentCorrelationId(CONTEXT_PARENT);
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptor(() -> null);

        ProducerRecord<String, String> result = interceptor.onSend(newRecord());

        assertThat(parentHeader(result)).isEqualTo(CONTEXT_PARENT);
    }

    @Test
    void context_parent_used_when_no_resolver_exists() {
        KafkaCorrelationContextHolder.setParentCorrelationId(CONTEXT_PARENT);
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptorWithoutResolvers();

        ProducerRecord<String, String> result = interceptor.onSend(newRecord());

        assertThat(parentHeader(result)).isEqualTo(CONTEXT_PARENT);
    }

    @Test
    void no_parent_header_added_when_no_source_exists() {
        KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> interceptor =
                newInterceptorWithoutResolvers();

        ProducerRecord<String, String> result = interceptor.onSend(newRecord());

        assertThat(result.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER)).isNull();
    }

    private static KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> newInterceptor(
            KafkaCorrelationParentIdResolver resolver) {
        return new KafkaCorrelationParentHeaderProducerDefaultInterceptor<>(List.of(resolver));
    }

    private static KafkaCorrelationParentHeaderProducerDefaultInterceptor<String, String> newInterceptorWithoutResolvers() {
        return new KafkaCorrelationParentHeaderProducerDefaultInterceptor<>(List.of());
    }

    private static ProducerRecord<String, String> newRecord() {
        return new ProducerRecord<>(TOPIC, null, "key", "value");
    }

    private static String parentHeader(ProducerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(KafkaConstants.CORRELATION_PARENT_ID_HEADER);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
