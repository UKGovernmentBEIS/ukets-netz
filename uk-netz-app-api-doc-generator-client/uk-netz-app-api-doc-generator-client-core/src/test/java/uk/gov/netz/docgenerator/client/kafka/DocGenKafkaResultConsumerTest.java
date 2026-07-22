package uk.gov.netz.docgenerator.client.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.kafka.support.Acknowledgment;
import uk.gov.netz.docgenerator.client.ConversionResultHandler;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.model.ConversionEvent;

class DocGenKafkaResultConsumerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    void onConversionEvent_acknowledgesWhenNoHandlersAreRegistered() {
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        DocGenKafkaResultConsumer consumer = consumer(List.of(), 3);

        consumer.onConversionEvent(record("job-1"), acknowledgment);

        verify(acknowledgment).acknowledge();
    }

    @Test
    void onConversionEvent_acknowledgesNullRecordValueWithoutInvokingHandlers() {
        ConversionResultHandler handler = mock(ConversionResultHandler.class);
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        DocGenKafkaResultConsumer consumer = consumer(List.of(handler), 3);

        consumer.onConversionEvent(new ConsumerRecord<>("doc.converted", 1, 10L, "job-1", null), acknowledgment);

        verify(handler, never()).handle(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onConversionEvent_invokesHandlersInOrderAndAcknowledgesWhenTheySucceed() {
        List<String> calls = new ArrayList<>();
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        DocGenKafkaResultConsumer consumer = consumer(List.of(
            new OrderedHandler(2, event -> calls.add("second")),
            new OrderedHandler(1, event -> calls.add("first"))
        ), 3);

        consumer.onConversionEvent(record("job-1"), acknowledgment);

        assertThat(calls).containsExactly("first", "second");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onConversionEvent_passesMetadataToHandlers() {
        List<Map<String, String>> handledMetadata = new ArrayList<>();
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        DocGenKafkaResultConsumer consumer = consumer(List.of(event -> handledMetadata.add(event.getMetadata())), 3);

        consumer.onConversionEvent(record(ConversionEvent.builder()
            .jobId("job-1")
            .status("COMPLETE")
            .outputS3Key("output/job-1.pdf")
            .durationMs(100L)
            .metadata(Map.of("requestId", "request-1"))
            .build()), acknowledgment);

        assertThat(handledMetadata).singleElement().satisfies(metadata -> assertThat(metadata)
            .containsEntry("requestId", "request-1"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void conversionEventDeserializesOmittedMetadataAsEmptyMap() throws Exception {
        String json = """
            {"jobId":"job-1","status":"COMPLETE","outputS3Key":"output/job-1.pdf","errorReason":null,"durationMs":100}
            """;

        ConversionEvent event = OBJECT_MAPPER.readValue(json, ConversionEvent.class);

        assertThat(event.getMetadata()).isEmpty();
    }

    @Test
    void onConversionEvent_acknowledgesPoisonEventAfterMaxHandlerRetries() {
        List<String> calls = new ArrayList<>();
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        DocGenKafkaResultConsumer consumer = consumer(List.of(
            event -> {
                calls.add("failed");
                throw new IllegalStateException("handler failed");
            },
            event -> calls.add("succeeded")
        ), 2);
        ConsumerRecord<String, ConversionEvent> record = record("job-1");

        assertThatThrownBy(() -> consumer.onConversionEvent(record, acknowledgment))
            .isInstanceOf(DocGenKafkaHandlerException.class);
        verify(acknowledgment, never()).acknowledge();

        consumer.onConversionEvent(record, acknowledgment);

        assertThat(calls).containsExactly("failed", "succeeded", "failed", "succeeded");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void onConversionEvent_clearsRetryCounterAfterSuccess() {
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        ToggleHandler toggleHandler = new ToggleHandler();
        DocGenKafkaResultConsumer consumer = consumer(List.of(toggleHandler), 2);
        ConsumerRecord<String, ConversionEvent> record = record("job-1");

        toggleHandler.fail = true;
        assertThatThrownBy(() -> consumer.onConversionEvent(record, acknowledgment))
            .isInstanceOf(DocGenKafkaHandlerException.class);

        toggleHandler.fail = false;
        consumer.onConversionEvent(record, acknowledgment);

        toggleHandler.fail = true;
        assertThatThrownBy(() -> consumer.onConversionEvent(record, acknowledgment))
            .isInstanceOf(DocGenKafkaHandlerException.class);
    }

    @Test
    void onConversionEvent_logsJobIdAndHandlerClassWhenHandlerFails() {
        Acknowledgment acknowledgment = mock(Acknowledgment.class);
        FailingHandler failingHandler = new FailingHandler();
        DocGenKafkaResultConsumer consumer = consumer(List.of(failingHandler), 3);
        RecordingAppender appender = new RecordingAppender();

        withAppender(appender, () -> assertThatThrownBy(() -> consumer.onConversionEvent(record("job-log-1"), acknowledgment))
            .isInstanceOf(DocGenKafkaHandlerException.class));

        assertThat(appender.events)
            .anySatisfy(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getMessage().getFormattedMessage())
                    .contains("job-log-1")
                    .contains(FailingHandler.class.getName());
            });
    }

    private static DocGenKafkaResultConsumer consumer(List<ConversionResultHandler> handlers, int maxHandlerRetries) {
        DocGenClientProperties properties = new DocGenClientProperties();
        properties.setMaxHandlerRetries(maxHandlerRetries);
        return new DocGenKafkaResultConsumer(handlers, new DocGenKafkaPoisonEventTracker(), properties);
    }

    private static ConsumerRecord<String, ConversionEvent> record(String jobId) {
        return record(ConversionEvent.builder()
            .jobId(jobId)
            .status("COMPLETE")
            .outputS3Key("output/" + jobId + ".pdf")
            .durationMs(100L)
            .build());
    }

    private static ConsumerRecord<String, ConversionEvent> record(ConversionEvent event) {
        return new ConsumerRecord<>("doc.converted", 1, 10L, event.getJobId(), event);
    }

    private static void withAppender(Appender appender, Runnable action) {
        LoggerContext context = LoggerContext.getContext(false);
        Configuration configuration = context.getConfiguration();
        String loggerName = DocGenKafkaResultConsumer.class.getName();
        LoggerConfig loggerConfig = configuration.getLoggerConfig(loggerName);
        boolean addedLoggerConfig = false;
        if (!loggerName.equals(loggerConfig.getName())) {
            loggerConfig = new LoggerConfig(loggerName, Level.WARN, true);
            configuration.addLogger(loggerName, loggerConfig);
            addedLoggerConfig = true;
        }
        appender.start();
        loggerConfig.addAppender(appender, Level.WARN, null);
        context.updateLoggers();
        try {
            action.run();
        } finally {
            loggerConfig.removeAppender(appender.getName());
            if (addedLoggerConfig) {
                configuration.removeLogger(loggerName);
            }
            appender.stop();
            context.updateLoggers();
        }
    }

    private record OrderedHandler(int order, ConversionResultHandler delegate) implements ConversionResultHandler, Ordered {

        @Override
        public void handle(ConversionEvent event) {
            delegate.handle(event);
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    private static class ToggleHandler implements ConversionResultHandler {

        private boolean fail;

        @Override
        public void handle(ConversionEvent event) {
            if (fail) {
                throw new IllegalStateException("handler failed");
            }
        }
    }

    private static class FailingHandler implements ConversionResultHandler {

        @Override
        public void handle(ConversionEvent event) {
            throw new IllegalStateException("handler failed");
        }
    }

    private static class RecordingAppender extends AbstractAppender {

        private final List<LogEvent> events = new ArrayList<>();

        RecordingAppender() {
            super("recording", null, null, false, null);
        }

        @Override
        public void append(LogEvent event) {
            events.add(event.toImmutable());
        }
    }
}
