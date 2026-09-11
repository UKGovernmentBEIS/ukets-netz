package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
class RestLoggingDockerIT {

    private static final int APPLICATION_PORT = 8080;
    private static final int LARGE_BODY_BYTES = 20 * 1024 * 1024;
    private static final long CONTAINER_MEMORY_BYTES = 512L * 1024 * 1024;
    private static final String SMALL_CORRELATION_ID = "rest-logging-it-small";
    private static final String LARGE_CORRELATION_ID = "rest-logging-it-large";
    private static final String PARTIAL_ERROR_CORRELATION_ID = "rest-logging-it-partial-error";
    private static final String MULTIPART_CORRELATION_ID = "rest-logging-it-multipart";
    private static final String MULTIPART_IGNORED_CORRELATION_ID = "rest-logging-it-multipart-ignored";
    private static final String LARGE_REQUEST_SECRET = "large-request-secret";
    private static final String LARGE_RESPONSE_SECRET = "large-response-secret";
    private static final String PARTIAL_ERROR_SECRET = "partial-error-secret";
    private static final byte[] LARGE_REQUEST_PREFIX =
            ("{\"password\":\"" + LARGE_REQUEST_SECRET + "\",\"data\":\"").getBytes(StandardCharsets.UTF_8);
    private static final byte[] LARGE_RESPONSE_PREFIX =
            ("{\"password\":\"" + LARGE_RESPONSE_SECRET + "\",\"data\":\"").getBytes(StandardCharsets.UTF_8);
    private static final byte[] JSON_SUFFIX = "\"}".getBytes(StandardCharsets.UTF_8);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ImageFromDockerfile APPLICATION_IMAGE = new ImageFromDockerfile()
            .withFileFromPath("Dockerfile", projectPath("src/test/docker/Dockerfile"))
            .withFileFromPath("classes", projectPath("target/classes"))
            .withFileFromPath("test-classes", projectPath("target/test-classes"))
            .withFileFromPath("lib", projectPath("target/integration-libs"));

    @Container
    static final GenericContainer<?> APPLICATION = new GenericContainer<>(APPLICATION_IMAGE)
            .withExposedPorts(APPLICATION_PORT)
            .withCreateContainerCmdModifier(command -> command.getHostConfig()
                    .withMemory(CONTAINER_MEMORY_BYTES)
                    .withMemorySwap(CONTAINER_MEMORY_BYTES)
                    .withNanoCPUs(1_000_000_000L))
            .waitingFor(Wait.forHttp("/it/health").forStatusCode(200))
            .withStartupTimeout(Duration.ofSeconds(90));

    @AfterAll
    static void closeHttpClient() {
        HTTP_CLIENT.close();
    }

    @Test
    void smallExchangeProducesCompleteMaskedRequestAndResponseLogs() throws Exception {
        String requestBody = "{\"message\":\"hello\",\"password\":\"small-request-secret\"}";
        HttpRequest request = HttpRequest.newBuilder(endpoint("/it/small?token=query-secret"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header(RestLoggingUtils.CORRELATION_ID_HEADER, SMALL_CORRELATION_ID)
                .header("Authorization", "Bearer authorization-secret")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue(RestLoggingUtils.CORRELATION_ID_HEADER))
                .contains(SMALL_CORRELATION_ID);
        assertThat(OBJECT_MAPPER.readTree(response.body()))
                .isEqualTo(OBJECT_MAPPER.readTree(
                        "{\"result\":\"hello\",\"password\":\"small-response-secret\"}"));

        List<LoggedEvent> events = awaitEvents(SMALL_CORRELATION_ID);
        LoggedEvent requestEvent = eventOfType(events, "REQUEST");
        LoggedEvent responseEvent = eventOfType(events, "RESPONSE");

        assertCommonEvent(requestEvent);
        JsonNode requestLog = requestEvent.message();
        assertThat(requestLog.path("httpMethod").asText()).isEqualTo("POST");
        assertThat(requestLog.path("uri").asText()).isEqualTo("/it/small?token=query-secret");
        assertThat(requestLog.path("payload").path("message").asText()).isEqualTo("hello");
        assertThat(requestLog.path("payload").path("password").asText()).isEqualTo("[REDACTED]");
        assertThat(requestLog.path("payloadCapture").path("status").asText()).isEqualTo("COMPLETE");
        assertThat(requestLog.path("payloadCapture").path("sizeBytes").asLong())
                .isEqualTo(requestBody.getBytes(StandardCharsets.UTF_8).length);
        assertThat(header(requestLog, "authorization")).contains("[REDACTED]");

        assertCommonEvent(responseEvent);
        JsonNode responseLog = responseEvent.message();
        assertThat(responseLog.path("httpStatus").asInt()).isEqualTo(200);
        assertThat(responseLog.path("payload").path("result").asText()).isEqualTo("hello");
        assertThat(responseLog.path("payload").path("password").asText()).isEqualTo("[REDACTED]");
        assertThat(responseLog.path("payloadCapture").path("status").asText()).isEqualTo("COMPLETE");
        assertThat(responseLog.path("payloadCapture").path("sizeBytes").asLong())
                .isEqualTo(response.body().getBytes(StandardCharsets.UTF_8).length);
        assertThat(responseLog.path("responseTimeInMillis").canConvertToLong()).isTrue();

        assertThat(stdout()).doesNotContain("small-request-secret", "small-response-secret",
                "authorization-secret");
    }

    @Test
    void largeExchangePreservesTwentyMegabyteStreamsAndEmitsTruncationMetadata() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(endpoint("/it/large"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .header(RestLoggingUtils.CORRELATION_ID_HEADER, LARGE_CORRELATION_ID)
                .POST(HttpRequest.BodyPublishers.ofInputStream(largeRequestBody()))
                .build();

        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        BodySummary responseBody;
        try (InputStream body = response.body()) {
            responseBody = summarize(body, LARGE_RESPONSE_PREFIX.length);
        }

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Consumed-Bytes"))
                .contains(Long.toString(LARGE_BODY_BYTES));
        assertThat(response.headers().firstValue(RestLoggingUtils.CORRELATION_ID_HEADER))
                .contains(LARGE_CORRELATION_ID);
        assertThat(responseBody.sizeBytes()).isEqualTo(LARGE_BODY_BYTES);
        assertThat(responseBody.prefix()).isEqualTo(LARGE_RESPONSE_PREFIX);
        assertThat(responseBody.suffix()).isEqualTo(JSON_SUFFIX);
        assertThat(APPLICATION.isRunning()).isTrue();

        List<LoggedEvent> events = awaitEvents(LARGE_CORRELATION_ID);
        assertTruncatedPayload(eventOfType(events, "REQUEST"), "POST", 0);
        assertTruncatedPayload(eventOfType(events, "RESPONSE"), "", 200);

        assertThat(stdout()).doesNotContain(LARGE_REQUEST_SECRET, LARGE_RESPONSE_SECRET);
    }

    @Test
    void errorExchangeCompletesAndLogsPartiallyConsumedRequest() throws Exception {
        String requestBody = "{\"message\":\"provider-payload\",\"password\":\""
                + PARTIAL_ERROR_SECRET + "\",\"data\":\"" + "x".repeat(63_000) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(endpoint("/it/partial-error"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header(RestLoggingUtils.CORRELATION_ID_HEADER, PARTIAL_ERROR_CORRELATION_ID)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.headers().firstValue("X-Consumed-Bytes")).contains("256");
        List<LoggedEvent> events = awaitEvents(PARTIAL_ERROR_CORRELATION_ID);
        LoggedEvent requestEvent = eventOfType(events, "REQUEST");
        LoggedEvent responseEvent = eventOfType(events, "RESPONSE");

        assertCommonEvent(requestEvent, "ERROR");
        JsonNode requestLog = requestEvent.message();
        assertThat(requestLog.path("payloadCapture").path("status").asText()).isEqualTo("COMPLETE");
        assertThat(requestLog.path("payloadCapture").path("sizeBytes").asLong())
                .isEqualTo(requestBody.getBytes(StandardCharsets.UTF_8).length);
        assertThat(requestLog.path("payload").path("message").asText()).isEqualTo("provider-payload");
        assertThat(requestLog.path("payload").path("password").asText()).isEqualTo("[REDACTED]");
        assertThat(requestLog.path("payload").path("data").asText()).startsWith("x");

        assertCommonEvent(responseEvent, "ERROR");
        assertThat(responseEvent.message().path("httpStatus").asInt()).isEqualTo(400);
        assertThat(stdout()).doesNotContain(PARTIAL_ERROR_SECRET);
    }

    @Test
    void applicationDrivenMultipartParsingCapturesJsonPartWithoutFileContent() throws Exception {
        String metadataSecret = "multipart-metadata-secret";
        String fileSecret = "multipart-file-secret";
        String boundary = "rest-logging-boundary";
        byte[] requestBody = multipartBody(boundary,
                "{\"message\":\"metadata\",\"password\":\"" + metadataSecret + "\"}", fileSecret);
        HttpRequest request = HttpRequest.newBuilder(endpoint("/it/multipart"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header(RestLoggingUtils.CORRELATION_ID_HEADER, MULTIPART_CORRELATION_ID)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        List<LoggedEvent> events = awaitEvents(MULTIPART_CORRELATION_ID);
        JsonNode requestLog = eventOfType(events, "REQUEST").message();
        assertThat(requestLog.path("payloadCapture").path("status").asText()).isEqualTo("COMPLETE");
        assertThat(requestLog.path("payload").path("message").asText()).isEqualTo("metadata");
        assertThat(requestLog.path("payload").path("password").asText()).isEqualTo("[REDACTED]");
        assertThat(stdout()).doesNotContain(metadataSecret, fileSecret);
    }

    @Test
    void multipartWithoutConsumedJsonPartRemainsIntentionallySkipped() throws Exception {
        String boundary = "rest-logging-ignored-boundary";
        byte[] requestBody = fileOnlyMultipartBody(boundary, "ignored-file-content");
        HttpRequest request = HttpRequest.newBuilder(endpoint("/it/multipart-ignored"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header(RestLoggingUtils.CORRELATION_ID_HEADER, MULTIPART_IGNORED_CORRELATION_ID)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode requestLog = eventOfType(awaitEvents(MULTIPART_IGNORED_CORRELATION_ID), "REQUEST").message();
        assertThat(requestLog.path("payloadCapture").path("status").asText()).isEqualTo("SKIPPED");
        assertThat(requestLog.path("payloadCapture").path("reason").asText()).isEqualTo("MULTIPART");
    }

    private static void assertTruncatedPayload(LoggedEvent event, String httpMethod, int httpStatus) {
        assertCommonEvent(event);
        JsonNode message = event.message();
        assertThat(message.path("payload").isEmpty()).isTrue();
        assertThat(message.path("payloadCapture").path("status").asText()).isEqualTo("TRUNCATED");
        assertThat(message.path("payloadCapture").path("sizeBytes").asLong()).isEqualTo(LARGE_BODY_BYTES);
        assertThat(message.path("payloadCapture").path("limitBytes").asInt())
                .isEqualTo(RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        assertThat(message.path("payloadCapture").has("sha256")).isFalse();
        if (!httpMethod.isEmpty()) {
            assertThat(message.path("httpMethod").asText()).isEqualTo(httpMethod);
        }
        if (httpStatus != 0) {
            assertThat(message.path("httpStatus").asInt()).isEqualTo(httpStatus);
        }
    }

    private static void assertCommonEvent(LoggedEvent event) {
        assertCommonEvent(event, "INFO");
    }

    private static void assertCommonEvent(LoggedEvent event, String expectedLevel) {
        assertThat(event.root().path("log.level").asText()).isEqualTo(expectedLevel);
        assertThat(event.root().path("log.logger").asText())
                .isEqualTo(RestLoggingService.class.getName());
        assertThat(event.root().path("@timestamp").asText()).isNotBlank();
        assertThat(event.message().path("payloadCapture").isObject()).isTrue();
        assertThat(event.raw().getBytes(StandardCharsets.UTF_8).length)
                .isLessThanOrEqualTo(RestLoggingSizeLimits.DEFAULT_MAX_RENDERED_EVENT_BYTES);
    }

    private static List<LoggedEvent> awaitEvents(String correlationId) {
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(events(correlationId)).hasSize(2));
        return events(correlationId);
    }

    private static List<LoggedEvent> events(String correlationId) {
        List<LoggedEvent> events = new ArrayList<>();
        stdout().lines().forEach(line -> parseEvent(line, correlationId).ifPresent(events::add));
        return events;
    }

    private static Optional<LoggedEvent> parseEvent(String line, String correlationId) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(line);
            JsonNode message = root.path("message");
            if (correlationId.equals(message.path("correlationId").asText())) {
                return Optional.of(new LoggedEvent(line, root, message));
            }
        } catch (IOException ignored) {
            // The JVM or container runtime may emit non-JSON diagnostics outside the application logger.
        }
        return Optional.empty();
    }

    private static LoggedEvent eventOfType(List<LoggedEvent> events, String type) {
        List<LoggedEvent> matchingEvents = events.stream()
                .filter(event -> type.equals(event.message().path("type").asText()))
                .toList();
        assertThat(matchingEvents).hasSize(1);
        return matchingEvents.getFirst();
    }

    private static Optional<String> header(JsonNode message, String expectedName) {
        for (Map.Entry<String, JsonNode> field : message.path("headers").properties()) {
            if (field.getKey().equalsIgnoreCase(expectedName)) {
                return Optional.of(field.getValue().asText());
            }
        }
        return Optional.empty();
    }

    private static Supplier<InputStream> largeRequestBody() {
        return () -> new JsonBodyInputStream(LARGE_REQUEST_PREFIX, JSON_SUFFIX, LARGE_BODY_BYTES, (byte) 'q');
    }

    private static byte[] multipartBody(String boundary, String metadata, String fileContent) {
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"metadata\"; filename=\"metadata.json\"\r\n"
                + "Content-Type: application/json\r\n\r\n"
                + metadata + "\r\n"
                + "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"file.txt\"\r\n"
                + "Content-Type: text/plain\r\n\r\n"
                + fileContent + "\r\n"
                + "--" + boundary + "--\r\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] fileOnlyMultipartBody(String boundary, String fileContent) {
        String body = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"file.txt\"\r\n"
                + "Content-Type: text/plain\r\n\r\n"
                + fileContent + "\r\n"
                + "--" + boundary + "--\r\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }

    private static BodySummary summarize(InputStream input, int prefixLength) throws IOException {
        byte[] buffer = new byte[8192];
        ByteArrayOutputStream prefix = new ByteArrayOutputStream(prefixLength);
        byte[] suffix = new byte[JSON_SUFFIX.length];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            int prefixBytes = Math.min(read, prefixLength - prefix.size());
            if (prefixBytes > 0) {
                prefix.write(buffer, 0, prefixBytes);
            }
            for (int index = 0; index < read; index++) {
                suffix[(int) ((total + index) % suffix.length)] = buffer[index];
            }
            total += read;
        }
        byte[] orderedSuffix = new byte[suffix.length];
        for (int index = 0; index < suffix.length; index++) {
            orderedSuffix[index] = suffix[(int) ((total + index) % suffix.length)];
        }
        return new BodySummary(total, prefix.toByteArray(), orderedSuffix);
    }

    private static String stdout() {
        return APPLICATION.getLogs(OutputFrame.OutputType.STDOUT);
    }

    private static URI endpoint(String path) {
        return URI.create("http://" + APPLICATION.getHost() + ":"
                + APPLICATION.getMappedPort(APPLICATION_PORT) + path);
    }

    private static Path projectPath(String relativePath) {
        return Path.of(System.getProperty("user.dir")).resolve(relativePath);
    }

    private record LoggedEvent(String raw, JsonNode root, JsonNode message) {
    }

    private record BodySummary(long sizeBytes, byte[] prefix, byte[] suffix) {
    }

    private static final class JsonBodyInputStream extends InputStream {

        private final ByteArrayInputStream prefix;
        private final ByteArrayInputStream suffix;
        private final byte repeatedValue;
        private int repeatedRemaining;

        private JsonBodyInputStream(byte[] prefix, byte[] suffix, int totalBytes, byte repeatedValue) {
            this.prefix = new ByteArrayInputStream(prefix);
            this.suffix = new ByteArrayInputStream(suffix);
            this.repeatedRemaining = totalBytes - prefix.length - suffix.length;
            this.repeatedValue = repeatedValue;
        }

        @Override
        public int read() {
            int value = prefix.read();
            if (value != -1) {
                return value;
            }
            if (repeatedRemaining > 0) {
                repeatedRemaining--;
                return repeatedValue;
            }
            return suffix.read();
        }

        @Override
        public int read(byte[] content, int offset, int length) {
            int read = prefix.read(content, offset, length);
            if (read != -1) {
                return read;
            }
            if (repeatedRemaining > 0) {
                int repeated = Math.min(length, repeatedRemaining);
                Arrays.fill(content, offset, offset + repeated, repeatedValue);
                repeatedRemaining -= repeated;
                return repeated;
            }
            return suffix.read(content, offset, length);
        }
    }
}
