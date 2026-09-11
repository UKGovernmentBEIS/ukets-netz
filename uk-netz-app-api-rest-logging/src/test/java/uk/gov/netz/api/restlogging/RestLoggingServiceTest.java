package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.ObjectMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.unit.DataSize;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestLoggingServiceTest {
    private static final String URI_EXCLUDED_PATTERN = "/api/*";
    private static final String REQUEST_URI = "/api/test";

    private MockHttpServletRequest request;
    private MultiReadHttpServletRequestWrapper wrappedRequest;

    private MockHttpServletResponse response;
    private ContentCachingResponseWrapper wrappedResponse;

    private Logger logger;
    private List<LogEvent> capturedLogEvents;

    @InjectMocks
    private RestLoggingService restLoggingService;

    @Spy
    private RestLoggingProperties restLoggingProperties;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Appender mockedAppender;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        wrappedRequest = new MultiReadHttpServletRequestWrapper(request);

        response = new MockHttpServletResponse();
        wrappedResponse = new ContentCachingResponseWrapper(response);

        initLogger();
        capturedLogEvents = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        logger.removeAppender(mockedAppender);
    }
    
    @Test
    void log_SuccessWhenLevelIsEnabled() {
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
        request.setQueryString("");
        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());

		restLoggingService.log(wrappedRequest, wrappedResponse,
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertEquals(2, capturedLogEvents.size());
        LogEvent requestLogEvent = capturedLogEvents.get(0);
        RestLoggingEntry loggedRequest = getRestLoggingEntry(requestLogEvent);
        assertEquals(correlationIdHeader, loggedRequest.getCorrelationId());
        assertEquals(correlationParentIdHeader, loggedRequest.getCorrelationParentId());
        assertEquals(REQUEST_URI, loggedRequest.getUri());
        assertEquals("EMPTY", loggedRequest.getPayloadCapture().get("status"));
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        RestLoggingEntry loggedResponse = getRestLoggingEntry(responseLogEvent);
        assertEquals(HttpStatus.OK.value(), loggedResponse.getHttpStatus());
        assertEquals("EMPTY", loggedResponse.getPayloadCapture().get("status"));
    }

    @Test
    void log_doNotLogWhenUriExcludedDoNotLog() {
        restLoggingProperties.setExcludedUriPatterns(List.of(URI_EXCLUDED_PATTERN));

        restLoggingService.log(wrappedRequest, wrappedResponse, LocalDateTime.now(), "1234", "5678");

        Mockito.verify(mockedAppender, Mockito.never()).append(any());
    }

    @Test
    void log_doNotLogWhenLevelNotEnabled() {
        logger.setLevel(Level.ERROR);

        restLoggingService.log(wrappedRequest, wrappedResponse, LocalDateTime.now(), "1234", "5678");

        Mockito.verify(mockedAppender, Mockito.never()).append(any());
    }

    @Test
    void log_alwaysLogErrors() {
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());

		restLoggingService.log(wrappedRequest, wrappedResponse,
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertEquals(2, capturedLogEvents.size());

        LogEvent requestLogEvent = capturedLogEvents.get(0);
        RestLoggingEntry loggedRequest = getRestLoggingEntry(requestLogEvent);
        assertEquals(correlationIdHeader, loggedRequest.getCorrelationId());
        assertEquals(correlationParentIdHeader, loggedRequest.getCorrelationParentId());
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        RestLoggingEntry loggedResponse = getRestLoggingEntry(responseLogEvent);
        assertEquals(HttpStatus.BAD_REQUEST.value(), loggedResponse.getHttpStatus());
        assertEquals(correlationIdHeader, loggedResponse.getCorrelationId());
        assertEquals(correlationParentIdHeader, loggedResponse.getCorrelationParentId());
    }

    @Test
    void log_boundedWrappersEmitCompleteJsonAndRawQueryValues() throws Exception {
        byte[] requestBody = "{\"field\":\"request-value\"}".getBytes(StandardCharsets.UTF_8);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(requestBody);
        request.setQueryString("requestId=123&mode=test");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer secret-token");
        request.addHeader(HttpHeaders.COOKIE, "session=secret-cookie");
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 1024);
        boundedRequest.getInputStream().readAllBytes();

        BoundedContentCachingResponseWrapper boundedResponse = new BoundedContentCachingResponseWrapper(response, 1024);
        boundedResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        boundedResponse.addHeader(HttpHeaders.SET_COOKIE, "session=response-secret");
        boundedResponse.getOutputStream().write("{\"field\":\"response-value\"}".getBytes(StandardCharsets.UTF_8));
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", "5678");

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("COMPLETE", loggedRequest.getPayloadCapture().get("status"),
                loggedRequest.getPayloadCapture().toString());
        assertEquals("request-value", loggedRequest.getPayload().get("field"));
        assertEquals("/api/test?requestId=123&mode=test", loggedRequest.getUri());
        assertEquals("[REDACTED]", loggedRequest.getHeaders().get(HttpHeaders.AUTHORIZATION));
        assertEquals("[REDACTED]", loggedRequest.getHeaders().get(HttpHeaders.COOKIE));
        RestLoggingEntry loggedResponse = getRestLoggingEntry(capturedLogEvents.get(1));
        assertEquals("response-value", loggedResponse.getPayload().get("field"));
        assertEquals("COMPLETE", loggedResponse.getPayloadCapture().get("status"));
        assertEquals("[REDACTED]", loggedResponse.getHeaders().get(HttpHeaders.SET_COOKIE));
    }

    @Test
    void log_oversizedJsonEmitsMetadataWithoutPartialPayload() throws Exception {
        byte[] requestBody = "{\"secret\":\"value-that-must-not-be-partially-logged\"}"
                .getBytes(StandardCharsets.UTF_8);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(requestBody);
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 16);
        boundedRequest.getInputStream().readAllBytes();

        BoundedContentCachingResponseWrapper boundedResponse = new BoundedContentCachingResponseWrapper(response, 16);
        boundedResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals(Map.of(), loggedRequest.getPayload());
        assertEquals("TRUNCATED", loggedRequest.getPayloadCapture().get("status"));
        assertEquals((long) requestBody.length, loggedRequest.getPayloadCapture().get("sizeBytes"));
        assertFalse(loggedRequest.getPayloadCapture().containsKey("sha256"));
    }

    @Test
    void log_knownOversizedUnconsumedJsonBodiesEmitTruncatedStatus() {
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"field\":\"request-body-over-limit\"}".getBytes(StandardCharsets.UTF_8));
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 16);

        BoundedContentCachingResponseWrapper boundedResponse =
                new BoundedContentCachingResponseWrapper(response, 16);
        boundedResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        boundedResponse.setContentLengthLong(32);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("TRUNCATED", loggedRequest.getPayloadCapture().get("status"));
        assertEquals(0L, loggedRequest.getPayloadCapture().get("sizeBytes"));
        RestLoggingEntry loggedResponse = getRestLoggingEntry(capturedLogEvents.get(1));
        assertEquals("TRUNCATED", loggedResponse.getPayloadCapture().get("status"));
        assertEquals(0L, loggedResponse.getPayloadCapture().get("sizeBytes"));
    }

    @Test
    void log_skippedStatusTakesPrecedenceOverKnownOversizedContent() {
        request.setContentType(MediaType.TEXT_PLAIN_VALUE);
        request.setContent("request-body-over-limit".getBytes(StandardCharsets.UTF_8));
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 8);
        BoundedContentCachingResponseWrapper boundedResponse =
                new BoundedContentCachingResponseWrapper(response, 8);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("SKIPPED", loggedRequest.getPayloadCapture().get("status"));
        assertEquals("NON_JSON_CONTENT_TYPE", loggedRequest.getPayloadCapture().get("reason"));
    }

    @Test
    void log_errorIncludesRawBodyWhenCompleteRequestIsInvalidJson() throws Exception {
        String invalidJson = "{\"password\":\"secret\", broken";
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(invalidJson.getBytes(StandardCharsets.UTF_8));
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 1024);
        boundedRequest.getInputStream().readAllBytes();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        BoundedContentCachingResponseWrapper boundedResponse =
                new BoundedContentCachingResponseWrapper(response, 1024);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals(invalidJson, loggedRequest.getPayload().get("rawBody"));
        assertEquals("FAILED", loggedRequest.getPayloadCapture().get("status"));
        assertEquals("INVALID_JSON", loggedRequest.getPayloadCapture().get("reason"));
    }

    @Test
    void log_successOmitsRawBodyWhenCompleteRequestIsInvalidJson() throws Exception {
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{broken".getBytes(StandardCharsets.UTF_8));
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(request, 1024);
        boundedRequest.getInputStream().readAllBytes();
        BoundedContentCachingResponseWrapper boundedResponse =
                new BoundedContentCachingResponseWrapper(response, 1024);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals(Map.of(), loggedRequest.getPayload());
        assertEquals("FAILED", loggedRequest.getPayloadCapture().get("status"));
        assertEquals("INVALID_JSON", loggedRequest.getPayloadCapture().get("reason"));
    }

    @Test
    void log_boundsHeadersUriAndIdentifiersWithTruncationMetadata() throws Exception {
        request.setContentType(MediaType.APPLICATION_JSON_VALUE + ";profile=" + "p".repeat(9000));
        request.addHeader("X-Large-One", "a\"\\".repeat(5000));
        request.addHeader("X-Large-Two", "b".repeat(9000));
        request.addHeader("X-Large-Three", "c".repeat(9000));
        request.setQueryString("parameter".repeat(2000) + "=secret-value");
        request.setRemoteUser("😀".repeat(1000));
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(
                request, RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        BoundedContentCachingResponseWrapper boundedResponse = new BoundedContentCachingResponseWrapper(
                response, RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(),
                "correlation".repeat(1000), "parent".repeat(1000));

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        byte[] serializedHeaders = objectMapper.writeValueAsBytes(loggedRequest.getHeaders());
        assertTrue(serializedHeaders.length <= RestLoggingSizeLimits.MAX_HEADERS_BYTES);
        assertEquals("TRUNCATED", loggedRequest.getHeadersCapture().get("status"));
        assertTrue((int) loggedRequest.getHeadersCapture().get("omittedCount") > 0);
        assertTrue(loggedRequest.getUri().getBytes(StandardCharsets.UTF_8).length
                <= RestLoggingSizeLimits.MAX_URI_BYTES);
        assertTrue(loggedRequest.getUri().endsWith(RestLoggingSizeLimits.TRUNCATED_SUFFIX));
        assertTrue(loggedRequest.getCorrelationId().getBytes(StandardCharsets.UTF_8).length
                <= RestLoggingSizeLimits.MAX_ID_BYTES);
        assertTrue(loggedRequest.getCorrelationParentId().getBytes(StandardCharsets.UTF_8).length
                <= RestLoggingSizeLimits.MAX_ID_BYTES);
        assertTrue(loggedRequest.getUserId().getBytes(StandardCharsets.UTF_8).length
                <= RestLoggingSizeLimits.MAX_ID_BYTES);
        String loggedContentType = (String) loggedRequest.getPayloadCapture().get("contentType");
        assertTrue(loggedContentType.getBytes(StandardCharsets.UTF_8).length
                <= RestLoggingSizeLimits.MAX_HEADER_VALUE_BYTES);
        assertTrue(loggedContentType.endsWith(RestLoggingSizeLimits.TRUNCATED_SUFFIX));
    }

    @Test
    void log_legacyErrorControllerWrapperReusesCaptureFromMainFilter() throws Exception {
        byte[] requestBody = "{\"field\":\"captured-before-error-dispatch\"}".getBytes(StandardCharsets.UTF_8);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(requestBody);
        BoundedRequestCaptureWrapper filterRequest = new BoundedRequestCaptureWrapper(request, 1024);
        filterRequest.getInputStream().readAllBytes();
        MultiReadHttpServletRequestWrapper errorControllerRequest =
                new MultiReadHttpServletRequestWrapper(filterRequest);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        startCapturingAppender();

        restLoggingService.log(errorControllerRequest, wrappedResponse,
                LocalDateTime.now(), "1234", null, REQUEST_URI, HttpStatus.INTERNAL_SERVER_ERROR);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("captured-before-error-dispatch", loggedRequest.getPayload().get("field"));
        assertEquals("COMPLETE", loggedRequest.getPayloadCapture().get("status"));
    }

    @Test
    void log_unifiedPayloadLimitAppliesToLegacyMultipartRequestAndResponse() {
        restLoggingProperties.setMaxPayloadSize(DataSize.ofBytes(1234));
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        startCapturingAppender();

        restLoggingService.log(wrappedRequest, wrappedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals(1234, loggedRequest.getPayloadCapture().get("limitBytes"));
        assertEquals("NO_JSON_PART", loggedRequest.getPayloadCapture().get("reason"));
        RestLoggingEntry loggedResponse = getRestLoggingEntry(capturedLogEvents.get(1));
        assertEquals(1234, loggedResponse.getPayloadCapture().get("limitBytes"));
    }

    @Test
    void log_boundedMultipartDoesNotInitiatePartParsing() {
        MockHttpServletRequest multipartRequest = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI) {
            @Override
            public java.util.Collection<jakarta.servlet.http.Part> getParts() {
                throw new AssertionError("logging must not initiate multipart parsing");
            }
        };
        multipartRequest.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        BoundedRequestCaptureWrapper boundedRequest = new BoundedRequestCaptureWrapper(multipartRequest, 1024);
        BoundedContentCachingResponseWrapper boundedResponse =
                new BoundedContentCachingResponseWrapper(response, 1024);
        boundedResponse.finishCapture();
        startCapturingAppender();

        restLoggingService.log(boundedRequest, boundedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("MULTIPART", loggedRequest.getPayloadCapture().get("reason"));
    }

    @Test
    void log_legacyMultipartHandlesIllegalStateException() {
        MockHttpServletRequest multipartRequest = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI) {
            @Override
            public java.util.Collection<jakarta.servlet.http.Part> getParts() {
                throw new IllegalStateException("multipart configuration unavailable");
            }
        };
        multipartRequest.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        MultiReadHttpServletRequestWrapper legacyRequest =
                new MultiReadHttpServletRequestWrapper(multipartRequest);
        startCapturingAppender();

        restLoggingService.log(legacyRequest, wrappedResponse, LocalDateTime.now(), "1234", null);

        RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
        assertEquals("READ_ERROR", loggedRequest.getPayloadCapture().get("reason"));
    }

    @Test
    void log_legacyMultipartPreservesJsonPartExtraction() throws Exception {
        byte[] json = "{\"field\":\"legacy-metadata\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest multipartRequest = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        multipartRequest.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        Part metadata = Mockito.mock(Part.class);
        when(metadata.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);
        when(metadata.getSize()).thenReturn((long) json.length);

        try (InputStream inputStream = Mockito.spy(new ByteArrayInputStream(json))) {
            when(metadata.getInputStream()).thenReturn(inputStream);
            multipartRequest.addPart(metadata);
            MultiReadHttpServletRequestWrapper legacyRequest =
                    new MultiReadHttpServletRequestWrapper(multipartRequest);
            startCapturingAppender();

            restLoggingService.log(legacyRequest, wrappedResponse, LocalDateTime.now(), "1234", null);

            RestLoggingEntry loggedRequest = getRestLoggingEntry(capturedLogEvents.get(0));
            assertEquals("COMPLETE", loggedRequest.getPayloadCapture().get("status"));
            assertEquals("legacy-metadata", loggedRequest.getPayload().get("field"));
            Mockito.verify(inputStream).close();
        }
    }

    @Test
    void log_legacyRequestAndResponseCloseOwnedCaptureStreams() throws Exception {
        byte[] requestBody = "{\"field\":\"request\"}".getBytes(StandardCharsets.UTF_8);
        byte[] responseBody = "{\"field\":\"response\"}".getBytes(StandardCharsets.UTF_8);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        try (ServletInputStream requestStream = Mockito.spy(
                new MultiReadHttpServletRequestWrapper.CachedServletInputStream(requestBody));
                InputStream responseStream = Mockito.spy(new ByteArrayInputStream(responseBody))) {
            MultiReadHttpServletRequestWrapper legacyRequest = new MultiReadHttpServletRequestWrapper(request) {
                @Override
                public ServletInputStream getInputStream() {
                    return requestStream;
                }
            };
            ContentCachingResponseWrapper legacyResponse = new ContentCachingResponseWrapper(response) {
                @Override
                public InputStream getContentInputStream() {
                    return responseStream;
                }

                @Override
                public int getContentSize() {
                    return responseBody.length;
                }
            };
            startCapturingAppender();

            restLoggingService.log(legacyRequest, legacyResponse, LocalDateTime.now(), "1234", null);

            Mockito.verify(requestStream).close();
            Mockito.verify(responseStream).close();
        }
    }

    private void initLogger() {
        when(mockedAppender.getName()).thenReturn("MockAppender");
        logger = (Logger) LogManager.getLogger(RestLoggingService.class);
        logger.addAppender(this.mockedAppender);
        logger.setLevel(Level.INFO);
    }

    private void startCapturingAppender() {
        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(mockedAppender).append(any());
    }

    private RestLoggingEntry getRestLoggingEntry(LogEvent event) {
        return (RestLoggingEntry) ((ObjectMessage) event.getMessage()).getParameter();
    }


}
