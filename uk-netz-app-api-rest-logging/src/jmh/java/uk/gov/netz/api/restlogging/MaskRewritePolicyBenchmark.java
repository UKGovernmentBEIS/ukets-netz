package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@Threads(1)
@State(Scope.Thread)
public class MaskRewritePolicyBenchmark {

    private static final String SENSITIVE_KEY = "password";
    private static final String SECRET_VALUE = "s".repeat(64);
    private static final String VISIBLE_VALUE = "v".repeat(96);

    @Param({"16384", "184320"})
    private int targetPayloadBytes = 16384;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final List<Map<String, Object>> sensitiveObjects = new ArrayList<>();

    private MaskRewritePolicy policy;
    private LogEvent structuredRestEvent;
    private LogEvent jsonTextEvent;

    @Setup(Level.Trial)
    public void setUpTrial() throws Exception {
        policy = MaskRewritePolicy.create(new Property[] {
                Property.createProperty("payloadProperty", SENSITIVE_KEY)
        });

        Map<String, Object> payload = createPayload();
        RestLoggingEntry restEntry = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .payload(payload)
                .payloadCapture(Map.of(
                        "status", "COMPLETE",
                        "sizeBytes", objectMapper.writeValueAsBytes(payload).length,
                        "limitBytes", RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES))
                .httpStatus(200)
                .build();

        structuredRestEvent = Log4jLogEvent.newBuilder()
                .setMessage(new ObjectMessage(restEntry))
                .build();
        jsonTextEvent = Log4jLogEvent.newBuilder()
                .setMessage(new SimpleMessage(objectMapper.writeValueAsString(restEntry)))
                .build();
    }

    @TearDown(Level.Invocation)
    public void restoreSensitiveValues() {
        for (Map<String, Object> item : sensitiveObjects) {
            item.put(SENSITIVE_KEY, SECRET_VALUE);
        }
    }

    @Benchmark
    public LogEvent rewriteStructuredRestEntry() {
        return policy.rewrite(structuredRestEvent);
    }

    @Benchmark
    public LogEvent rewriteJsonTextFallback() {
        return policy.rewrite(jsonTextEvent);
    }

    private Map<String, Object> createPayload() throws Exception {
        sensitiveObjects.clear();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", sensitiveObjects);

        int estimatedItemBytes = SECRET_VALUE.length() + VISIBLE_VALUE.length() + 48;
        int initialItemCount = Math.max(1, targetPayloadBytes / estimatedItemBytes);
        for (int index = 0; index < initialItemCount; index++) {
            addItem(index);
        }

        int payloadBytes = objectMapper.writeValueAsBytes(payload).length;
        while (payloadBytes < targetPayloadBytes) {
            addItem(sensitiveObjects.size());
            payloadBytes = objectMapper.writeValueAsBytes(payload).length;
        }
        if (payloadBytes > RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES) {
            throw new IllegalStateException("Benchmark payload exceeded the REST logging limit: " + payloadBytes);
        }
        return payload;
    }

    private void addItem(int index) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put(SENSITIVE_KEY, SECRET_VALUE);
        item.put("visible", VISIBLE_VALUE);
        item.put("index", index);
        sensitiveObjects.add(item);
    }
}
