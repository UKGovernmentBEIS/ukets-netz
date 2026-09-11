package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ObjectMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BoundedRestJsonTemplateLayoutTest {

    private static final int TEST_EVENT_LIMIT_BYTES = 240 * 1024;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private BoundedRestJsonTemplateLayout layout;
    private MaskRewritePolicy maskingPolicy;

    @BeforeEach
    void setUp() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        layout = BoundedRestJsonTemplateLayout.create(
                context.getConfiguration(), "classpath:ApplicationLogJsonLayout.json", true,
                32768, TEST_EVENT_LIMIT_BYTES);
        maskingPolicy = MaskRewritePolicy.create(new Property[]{
                Property.createProperty("payloadProperty", "secret")
        });
    }

    @Test
    void maximumConfiguredRestPayloadRemainsOneValidJsonEvent() throws Exception {
        RestLoggingEntry entry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .headers(Map.of("content-type", "application/json"))
                .payload(fields(2100, 64))
                .payloadCapture(Map.of(
                        "status", "COMPLETE",
                        "sizeBytes", 192L * 1024,
                        "limitBytes", 192 * 1024))
                .httpStatus(200)
                .build();

        byte[] rendered = renderMasked(entry);
        JsonNode log = objectMapper.readTree(rendered);

        assertThat(rendered.length).isGreaterThan(160 * 1024);
        assertThat(rendered.length).isLessThanOrEqualTo(TEST_EVENT_LIMIT_BYTES);
        assertThat(log.path("message").path("payload").size()).isEqualTo(2100);
        assertThat(log.path("message").has("logEventCapture")).isFalse();
    }

    @Test
    void oversizedRestEventFallsBackToCompactValidJson() throws Exception {
        Map<String, Object> payload = fields(4000, 64);
        payload.put("secret", "must-not-appear");
        RestLoggingEntry entry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .headers(Map.of("large", "x".repeat(16 * 1024)))
                .headersCapture(Map.of("status", "TRUNCATED", "omittedCount", 2))
                .payload(payload)
                .payloadCapture(Map.of(
                        "status", "COMPLETE",
                        "sizeBytes", 192L * 1024,
                        "limitBytes", 192 * 1024))
                .uri("/api/test")
                .httpMethod("POST")
                .build();

        byte[] rendered = renderMasked(entry);
        JsonNode message = objectMapper.readTree(rendered).path("message");

        assertThat(rendered.length).isLessThanOrEqualTo(TEST_EVENT_LIMIT_BYTES);
        assertThat(message.path("payload").isEmpty()).isTrue();
        assertThat(message.path("headers").isEmpty()).isTrue();
        assertThat(message.path("headersCapture").path("omittedCount").asInt()).isEqualTo(3);
        assertThat(message.path("payloadCapture").path("sizeBytes").asLong()).isEqualTo(192L * 1024);
        assertThat(message.path("logEventCapture").path("reason").asText())
                .isEqualTo("LOG_EVENT_SIZE_LIMIT");
        assertThat(new String(rendered, java.nio.charset.StandardCharsets.UTF_8))
                .doesNotContain("must-not-appear");
    }

    @Test
    void genericApplicationObjectMessageIsNotChangedByRestOnlyGuard() {
        LogEvent genericEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(fields(4000, 64)))
                .build();

        byte[] rendered = layout.toByteArray(genericEvent);

        assertThat(rendered.length).isGreaterThan(TEST_EVENT_LIMIT_BYTES);
    }

    @Test
    void irreducibleRestEntryUsesMinimalValidJsonFallback() throws Exception {
        Map<String, Object> hugeMetadata = fields(4000, 64);
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "REQUEST");
        message.put("headers", Map.of());
        message.put("payload", fields(4000, 64));
        message.put("payloadCapture", hugeMetadata);
        LogEvent event = Log4jLogEvent.newBuilder().setMessage(new ObjectMessage(message)).build();

        byte[] rendered = layout.toByteArray(event);
        JsonNode log = objectMapper.readTree(rendered);

        assertThat(rendered.length).isLessThanOrEqualTo(TEST_EVENT_LIMIT_BYTES);
        assertThat(log.path("message").asText()).isEqualTo("[REST LOG OMITTED: EVENT SIZE LIMIT]");
    }

    @Test
    void factoryAcceptsLimitAboveDefault() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        BoundedRestJsonTemplateLayout largeLimitLayout = BoundedRestJsonTemplateLayout.create(
                context.getConfiguration(), "classpath:ApplicationLogJsonLayout.json", true,
                32768, 2_000_000);

        assertThat(largeLimitLayout.getMaxEventBytes()).isEqualTo(2_000_000);
    }

    @Test
    void factoryRejectsLimitThatCannotContainMinimalFallback() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        assertThatIllegalArgumentException().isThrownBy(() -> BoundedRestJsonTemplateLayout.create(
                context.getConfiguration(), "classpath:ApplicationLogJsonLayout.json", true,
                32768, 1));
    }

    @Test
    void productionLog4jConfigurationLoadsSafeLayoutPlugin() throws Exception {
        try (LoggerContext context = new LoggerContext("bounded-rest-layout-test");
                InputStream input = getClass().getClassLoader().getResourceAsStream("log4j2-json.xml")) {
            assertThat(input).isNotNull();
            XmlConfiguration configuration = new XmlConfiguration(context, new ConfigurationSource(input));
            configuration.start();
            try {
                ConsoleAppender appender = (ConsoleAppender) configuration.getAppender("consoleJSONAppender");
                assertThat(appender.getLayout()).isInstanceOf(BoundedRestJsonTemplateLayout.class);
                assertThat(((BoundedRestJsonTemplateLayout) appender.getLayout()).getMaxEventBytes())
                        .isEqualTo(RestLoggingSizeLimits.DEFAULT_MAX_RENDERED_EVENT_BYTES);
            } finally {
                configuration.stop();
            }
        }
    }

    private byte[] renderMasked(RestLoggingEntry entry) {
        LogEvent event = Log4jLogEvent.newBuilder().setMessage(new ObjectMessage(entry)).build();
        return layout.toByteArray(maskingPolicy.rewrite(event));
    }

    private static Map<String, Object> fields(int count, int valueLength) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            values.put("field" + index, "x".repeat(valueLength));
        }
        return values;
    }
}
