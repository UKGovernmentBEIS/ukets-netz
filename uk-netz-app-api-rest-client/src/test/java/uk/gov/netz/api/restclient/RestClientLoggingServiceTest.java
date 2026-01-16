package uk.gov.netz.api.restclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import uk.gov.netz.api.restlogging.RestLoggingProperties;

@ExtendWith(MockitoExtension.class)
class RestClientLoggingServiceTest {

    private static final String REQUEST_URI = "http://localhost:8091/auth/test";

    private Logger logger;
    private List<LogEvent> capturedLogEvents;

    @InjectMocks
    private RestClientLoggingService cut;

    @Spy
    private RestLoggingProperties restClientLoggingProperties;

    @Mock
    private Appender mockedAppender;

    @BeforeEach
    public void setUp() {
        initLogger();
        capturedLogEvents = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() {
        logger.removeAppender(mockedAppender);
    }
    
    @Test
    void log_SuccessWhenLevelIsEnabled() throws IOException {
    	MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, REQUEST_URI);
        request.getHeaders().add("headerReq1", "headerReq1Value");

        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        response.getHeaders().add("headerRes1", "headerRes1Value");
    	
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
        when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());

        cut.log(request, new byte[0], response, new byte[0],
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertThat(capturedLogEvents).hasSize(2);
        LogEvent requestLogEvent = capturedLogEvents.get(0);
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains(correlationIdHeader);
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains(correlationParentIdHeader);
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        assertThat(responseLogEvent.getMessage().getFormattedMessage()).contains(String.valueOf(HttpStatus.OK.value()));
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains("payload");
    }

    @Test
    void log_doNotLogWhenUriExcluded() throws IOException {
    	MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, REQUEST_URI);
        request.getHeaders().add("headerReq1", "headerReq1Value");

        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        response.getHeaders().add("headerRes1", "headerRes1Value");
    	
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
    	
        when(restClientLoggingProperties.getExcludedUriPatterns()).thenReturn(List.of("http://localhost:8091/auth"));

        cut.log(request, new byte[0], response, new byte[0],
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        verify(mockedAppender, never()).append(any());
    }

    @Test
    void log_doNotLogWhenLevelNotEnabled() throws IOException {
        logger.setLevel(Level.DEBUG);

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, REQUEST_URI);
        request.getHeaders().add("headerReq1", "headerReq1Value");

        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        response.getHeaders().add("headerRes1", "headerRes1Value");
    	
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
    	
        when(restClientLoggingProperties.getExcludedUriPatterns()).thenReturn(List.of("http://localhost:8091/auth"));

        cut.log(request, new byte[0], response, new byte[0],
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        verify(mockedAppender, never()).append(any());
    }

    @Test
    void log_alwaysLogErrors() throws IOException {
    	MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, REQUEST_URI);
        request.getHeaders().add("headerReq1", "headerReq1Value");

        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add("headerRes1", "headerRes1Value");
    	
    	String correlationIdHeader = "1234";
    	String correlationParentIdHeader = "5678";
    	
    	when(mockedAppender.isStarted()).thenReturn(true);
        doAnswer(answerVoid((LogEvent event) -> capturedLogEvents.add(event.toImmutable())))
                .when(this.mockedAppender).append(any());
    	
        cut.log(request, new byte[0], response, new byte[0],
				LocalDateTime.now(), correlationIdHeader, correlationParentIdHeader);

        assertThat(capturedLogEvents).hasSize(2);

        assertThat(capturedLogEvents).hasSize(2);
        LogEvent requestLogEvent = capturedLogEvents.get(0);
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains(correlationIdHeader);
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains(correlationParentIdHeader);
        
        LogEvent responseLogEvent = capturedLogEvents.get(1);
        assertThat(responseLogEvent.getMessage().getFormattedMessage()).contains(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        assertThat(requestLogEvent.getMessage().getFormattedMessage()).contains("payload");
    }

    private void initLogger() {
        when(mockedAppender.getName()).thenReturn("MockAppender");
        logger = (Logger) LogManager.getLogger(RestClientLoggingService.class);
        logger.addAppender(this.mockedAppender);
        logger.setLevel(Level.INFO);
    }
    
}
