package uk.gov.netz.api.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.gov.netz.api.kafka.correlation.KafkaCorrelationContextHolder;
import uk.gov.netz.api.kafka.utils.KafkaConstants;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaCorrelationHeaderProducerDefaultInterceptorTest {

    private static final String TOPIC = "test-topic";
    private static final String EXISTING_CORRELATION_ID = "B";
    private static final String CONTEXT_CORRELATION_ID = "C";

    private final KafkaCorrelationHeaderProducerDefaultInterceptor<String, String> interceptor =
            new KafkaCorrelationHeaderProducerDefaultInterceptor<>();

    @AfterEach
    void tearDown() {
        KafkaCorrelationContextHolder.clear();
    }

    @Test
    void explicit_correlation_id_header_wins() {
        KafkaCorrelationContextHolder.setCorrelationId(CONTEXT_CORRELATION_ID);
        ProducerRecord<String, String> record = newRecord();
        record.headers().add(KafkaConstants.CORRELATION_ID_HEADER,
                EXISTING_CORRELATION_ID.getBytes(StandardCharsets.UTF_8));

        ProducerRecord<String, String> result = interceptor.onSend(record);

        assertThat(correlationHeader(result)).isEqualTo(EXISTING_CORRELATION_ID);
    }

    @Test
    void context_correlation_id_is_used_when_header_absent() {
        KafkaCorrelationContextHolder.setCorrelationId(CONTEXT_CORRELATION_ID);
        ProducerRecord<String, String> record = newRecord();

        ProducerRecord<String, String> result = interceptor.onSend(record);

        assertThat(correlationHeader(result)).isEqualTo(CONTEXT_CORRELATION_ID);
    }

    @Test
    void multiple_sends_in_same_context_share_same_correlation_id() {
        KafkaCorrelationContextHolder.setCorrelationId(CONTEXT_CORRELATION_ID);

        ProducerRecord<String, String> first = interceptor.onSend(newRecord());
        ProducerRecord<String, String> second = interceptor.onSend(newRecord());
        ProducerRecord<String, String> third = interceptor.onSend(newRecord());

        assertThat(correlationHeader(first)).isEqualTo(CONTEXT_CORRELATION_ID);
        assertThat(correlationHeader(second)).isEqualTo(CONTEXT_CORRELATION_ID);
        assertThat(correlationHeader(third)).isEqualTo(CONTEXT_CORRELATION_ID);
    }

    @Test
    void uuid_fallback_still_works_when_header_and_context_absent() {
        ProducerRecord<String, String> record = newRecord();

        ProducerRecord<String, String> result = interceptor.onSend(record);

        String header = correlationHeader(result);
        assertThat(header).isNotNull();
        assertThat(UUID.fromString(header)).isNotNull();
    }

    private static ProducerRecord<String, String> newRecord() {
        return new ProducerRecord<>(TOPIC, null, "key", "value");
    }

    private static String correlationHeader(ProducerRecord<?, ?> record) {
        Header header = record.headers().lastHeader(KafkaConstants.CORRELATION_ID_HEADER);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
