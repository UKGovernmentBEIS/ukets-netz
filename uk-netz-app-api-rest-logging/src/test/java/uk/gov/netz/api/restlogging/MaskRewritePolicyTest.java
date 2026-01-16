package uk.gov.netz.api.restlogging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(masked.getMessage().getFormattedMessage().equals("\"key1\":\"******\", \"key2\":\"value2\""));
    }

    @Test
    void rewrite_SimpleMessage_java() {
        SimpleMessage simpleMessage = new SimpleMessage("key1=value1, key2=value2)");
        Log4jLogEvent logEvent = Log4jLogEvent.newBuilder().setMessage(simpleMessage).build();
        LogEvent masked = maskRewritePolicy.rewrite(logEvent);

        assertTrue(masked.getMessage().getFormattedMessage().equals("key1=******, key2=value2)"));
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

        assertTrue(masked.getMessage().getFormattedMessage().contains("key1=******"));
        assertTrue(masked.getMessage().getFormattedMessage().contains("key2=value2"));

    }
}