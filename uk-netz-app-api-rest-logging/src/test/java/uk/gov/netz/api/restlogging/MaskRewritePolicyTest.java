package uk.gov.netz.api.restlogging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MaskRewritePolicyTest {
    private MaskRewritePolicy maskRewritePolicy;

    @BeforeEach
    public void setUp() {
        Property property = Property.createProperty("payloadProperty", "key1");
        maskRewritePolicy = MaskRewritePolicy.create(new Property[] { property });
    }
    @Test
    void rewrite_SimpleMessage_json() {
        SimpleMessage simpleMessage = new SimpleMessage("\"key1\":\"value1\", \"key2\":\"value2\"");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(simpleMessage).build();
        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .isEqualTo("\"key1\":\"[REDACTED]\", \"key2\":\"value2\"");
    }

    @Test
    void rewrite_SimpleMessage_java() {
        SimpleMessage simpleMessage = new SimpleMessage("key1=value1, key2=value2)");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(simpleMessage).build();
        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage()).isEqualTo("key1=[REDACTED], key2=value2)");
    }

    @Test
    void rewrite_ObjectMessage() {
        RestLoggingEntry restLoggingEntry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .headers(Map.of())
                .payload(Map.of("key1", "value1", "key2", "value2"))
                .uri("uri")
                .userId("user")
                .httpMethod(HttpMethod.POST.name())
                .build();
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(restLoggingEntry))
                .build();
        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .contains("key1=[REDACTED]")
                .contains("key2=value2");

    }

    @Test
    void rewrite_ObjectMessage_masksNestedCaseInsensitiveKeysOfAnyType() {
        RestLoggingEntry restLoggingEntry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .payload(Map.of("outer", Map.of("KEY1", 123456), "key2", "visible"))
                .build();
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(restLoggingEntry))
                .build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .contains("KEY1=[REDACTED]")
                .contains("key2=visible")
                .doesNotContain("123456");
    }

    @Test
    void rewrite_ValidJsonSimpleMessage_masksStructurally() {
        SimpleMessage message = new SimpleMessage("{\"items\":[{\"KeY1\":true}],\"key2\":\"visible\"}");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(message).build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .isEqualTo("{\"items\":[{\"KeY1\":\"[REDACTED]\"}],\"key2\":\"visible\"}");
    }

    @Test
    void rewrite_InvalidJsonThatLooksStructuredFailsClosed() {
        SimpleMessage message = new SimpleMessage("{\"key1\":\"secret-value");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(message).build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .isEqualTo("[MESSAGE OMITTED: MASKING FAILED]")
                .doesNotContain("secret-value");
    }

    @Test
    void rewrite_InvalidRawRestPayloadUsesBestEffortTextMasking() {
        RestLoggingEntry restLoggingEntry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .payload(Map.of("rawBody", "{\"key1\":\"secret-value\",\"visible\":\"retained\", broken"))
                .payloadCapture(Map.of("status", "FAILED", "reason", "INVALID_JSON"))
                .build();
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(restLoggingEntry))
                .build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage())
                .contains("\"key1\":\"[REDACTED]\"")
                .contains("\"visible\":\"retained\"")
                .doesNotContain("secret-value");
    }

    @Test
    void rewrite_MessageWithoutSensitiveKeysReturnsOriginalEvent() {
        SimpleMessage message = new SimpleMessage("no sensitive data");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(message).build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked).isSameAs(logEvent);
    }

    @Test
    void rewrite_MalformedTextIsUnchangedWhenConfigurationHasNoEffectiveSensitiveKeys() {
        MaskRewritePolicy emptyPolicy = MaskRewritePolicy.create(new Property[]{
                Property.createProperty("payloadProperty", " "),
                Property.createProperty("ignoredProperty", "key1")
        });
        String malformed = "{\"key1\":\"secret-value";
        Log4jLogEvent simpleEvent = Log4jLogEvent.newBuilder()
                .setMessage(new SimpleMessage(malformed))
                .build();
        Log4jLogEvent objectEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(malformed))
                .build();

        assertThat(emptyPolicy.rewrite(simpleEvent)).isSameAs(simpleEvent);
        assertThat(emptyPolicy.rewrite(objectEvent)).isSameAs(objectEvent);
    }

    @Test
    void rewrite_RestEntryIsStillConvertedWhenConfigurationHasNoEffectiveSensitiveKeys() {
        MaskRewritePolicy emptyPolicy = MaskRewritePolicy.create(new Property[]{
                Property.createProperty("payloadProperty", " ")
        });
        RestLoggingEntry entry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .payload(Map.of("visible", "value"))
                .payloadCapture(Map.of("status", "COMPLETE"))
                .build();
        LogEvent rewritten = emptyPolicy.rewrite(Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(entry))
                .build());

        Object parameter = ((ObjectMessage) rewritten.getMessage()).getParameter();
        assertThat(parameter).isInstanceOf(Map.class);
        Map<?, ?> mappedEntry = (Map<?, ?>) parameter;
        assertThat(mappedEntry.get("type")).isEqualTo(RestLoggingEntry.RestLoggingEntryType.REQUEST);
        assertThat(mappedEntry.containsKey("payloadCapture")).isTrue();
    }

    @Test
    void rewrite_OversizedSimpleMessageOmitsTheWholeMessage() {
        SimpleMessage message = new SimpleMessage("x".repeat(768 * 1024 + 1));
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(message).build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage()).isEqualTo("[MESSAGE OMITTED: OVERSIZED]");
    }

    @Test
    void rewrite_MaskingFailureNeverFallsBackToOriginalObject() {
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(new BrokenMessage()))
                .build();

        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertThat(masked.getMessage().getFormattedMessage()).isEqualTo("[MESSAGE OMITTED: MASKING FAILED]");
    }

    @Test
    void rewrite_MaximumConfiguredPayloadFitsRenderedEventLimit() {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < 2100; index++) {
            payload.put("field" + index, "x".repeat(64));
        }
        RestLoggingEntry entry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .payload(payload)
                .payloadCapture(Map.of("status", "COMPLETE", "limitBytes", 192 * 1024))
                .build();
        LogEvent event = Log4jLogEvent.newBuilder().setMessage(new ObjectMessage(entry)).build();
        LogEvent masked = maskRewritePolicy.rewrite(event);
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        BoundedRestJsonTemplateLayout layout = BoundedRestJsonTemplateLayout.create(
                context.getConfiguration(), "classpath:ApplicationLogJsonLayout.json", true,
                32768, RestLoggingSizeLimits.DEFAULT_MAX_RENDERED_EVENT_BYTES);

        byte[] renderedEvent = layout.toByteArray(masked);

        assertThat(renderedEvent.length).isLessThanOrEqualTo(
                RestLoggingSizeLimits.DEFAULT_MAX_RENDERED_EVENT_BYTES);
    }

    private static final class BrokenMessage {

        public String getKey1() {
            throw new IllegalStateException("secret-value");
        }
    }
}
