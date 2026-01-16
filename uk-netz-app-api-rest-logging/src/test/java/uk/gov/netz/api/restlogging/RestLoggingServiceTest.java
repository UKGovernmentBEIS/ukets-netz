package uk.gov.netz.api.restlogging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
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
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private RestLoggingEntry requestLog, responseLog;
    private Logger logger;
    private List<LogEvent> capturedLogEvents;

    @InjectMocks
    private RestLoggingService restLoggingService;

    @Spy
    private RestLoggingProperties restLoggingProperties;

    @Mock
    private Appender mockedAppender;

    @BeforeEach
    public void setUp() {
        request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        wrappedRequest = new MultiReadHttpServletRequestWrapper(request);

        response = new MockHttpServletResponse();
        wrappedResponse = new ContentCachingResponseWrapper(response);

        requestLog = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .headers(Map.of())
                .payload(Map.of("body", "payload"))
                .uri(REQUEST_URI)
                .userId("user")
                .httpMethod(HttpMethod.POST.name())
                .build();


        responseLog = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .headers(Map.of())
                .payload(Map.of("body", "payload"))
                .httpStatus(HttpStatus.ACCEPTED.value())
                .build();
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
        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());

		restLoggingService.log(wrappedRequest, wrappedResponse,
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertEquals(2, capturedLogEvents.size());
        LogEvent requestLogEvent = capturedLogEvents.get(0);
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(requestLog.getUserId()));
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(correlationIdHeader));
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(correlationParentIdHeader));
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        assertTrue(responseLogEvent.getMessage().getFormattedMessage().contains(String.valueOf(responseLog.getHttpStatus())));
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains("payload"));
    }

    @Test
    void log_doNotLogWhenUriExcludedDoNotLog() {
        when(restLoggingProperties.getExcludedUriPatterns()).thenReturn(List.of(URI_EXCLUDED_PATTERN));

        restLoggingService.log(wrappedRequest, wrappedResponse, LocalDateTime.now(), "1234", "5678");

        Mockito.verify(mockedAppender, Mockito.never()).append(any());
    }

    @Test
    void log_doNotLogWhenLevelNotEnabled() {
        logger.setLevel(Level.DEBUG);

        restLoggingService.log(wrappedRequest, wrappedResponse, LocalDateTime.now(), "1234", "5678");

        Mockito.verify(mockedAppender, Mockito.never()).append(any());
    }

    @Test
    void log_alwaysLogErrors() {
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
        responseLog.setHttpStatus(HttpStatus.BAD_REQUEST.value());
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());

		restLoggingService.log(wrappedRequest, wrappedResponse,
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertEquals(2, capturedLogEvents.size());

        LogEvent requestLogEvent = capturedLogEvents.get(0);
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(requestLog.getUserId()));
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(correlationIdHeader));
        assertTrue(requestLogEvent.getMessage().getFormattedMessage().contains(correlationParentIdHeader));
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        assertTrue(responseLogEvent.getMessage().getFormattedMessage().contains(String.valueOf(responseLog.getHttpStatus())));
        assertTrue(responseLogEvent.getMessage().getFormattedMessage().contains(correlationIdHeader));
        assertTrue(responseLogEvent.getMessage().getFormattedMessage().contains(correlationParentIdHeader));
    }

    private void initLogger() {
        when(mockedAppender.getName()).thenReturn("MockAppender");
        logger = (Logger) LogManager.getLogger(RestLoggingService.class);
        logger.addAppender(this.mockedAppender);
        logger.setLevel(Level.INFO);
    }


}