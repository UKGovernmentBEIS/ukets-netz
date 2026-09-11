package uk.gov.netz.api.restlogging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestLoggingFilterTest {
    private static final String REQUEST_URI = "/api/test";

    @InjectMocks
    private RestLoggingFilter restLoggingFilter;

    @Mock
    private RestLoggingService restLoggingService;
    
    @Mock
    private RestLoggingProperties restLoggingProperties;

    private MockFilterChain filterChain;

    @BeforeEach
    public void setUp() {
        filterChain = Mockito.spy(new MockFilterChain());
    }
    
    @Test
    void doFilterInternal_no_log_when_excluded() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());

        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(true);
        
        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNull();
        
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
        verify(restLoggingProperties, times(1)).isTotallyExcluded(REQUEST_URI);
        verifyNoInteractions(restLoggingService);
    }

    @Test
    void doFilterInternal_propagates_correlation_headers_when_totally_excluded()
            throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        request.addHeader(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
        request.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(true);

        restLoggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isEqualTo("1234");
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isEqualTo("5678");
        verify(filterChain, times(1)).doFilter(request, response);
        verify(restLoggingProperties, times(1)).isTotallyExcluded(REQUEST_URI);
        verifyNoInteractions(restLoggingService);
    }

    @Test
    void doFilterInternal_correlation_id_new() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());

        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(false);
        when(restLoggingProperties.getMaxPayloadBytes()).thenReturn(1234);
        
        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNull();
        
        ArgumentCaptor<BoundedRequestCaptureWrapper> requestCaptor =
                ArgumentCaptor.forClass(BoundedRequestCaptureWrapper.class);
        ArgumentCaptor<BoundedContentCachingResponseWrapper> responseCaptor =
                ArgumentCaptor.forClass(BoundedContentCachingResponseWrapper.class);
        Mockito.verify(restLoggingService, Mockito.times(1)).log(requestCaptor.capture(), responseCaptor.capture(),
                any(LocalDateTime.class), anyString(), eq(null));
        assertThat(requestCaptor.getValue().getPayloadCapture().getLimitBytes()).isEqualTo(1234);
        assertThat(responseCaptor.getValue().getPayloadCapture().getLimitBytes()).isEqualTo(1234);
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).isTotallyExcluded(REQUEST_URI);
    }
    
    @Test
    void doFilterInternal_correlation_id_in_request() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        request.addHeader(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
        request.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());
        
        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(false);

        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNotNull();
        
        Mockito.verify(restLoggingService, Mockito.times(1)).log(any(BoundedRequestCaptureWrapper.class),
                any(BoundedContentCachingResponseWrapper.class),
                any(LocalDateTime.class), eq("1234"), eq("5678"));
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).isTotallyExcluded(REQUEST_URI);
    }
    
    @Test
    void doFilterInternal_correlation_id_in_reponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());
        response.setHeader(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
        request.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
        
        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(false);

        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNotNull();
        
        Mockito.verify(restLoggingService, Mockito.times(1)).log(any(BoundedRequestCaptureWrapper.class),
                any(BoundedContentCachingResponseWrapper.class), any(LocalDateTime.class), eq("1234"), eq("5678"));
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).isTotallyExcluded(REQUEST_URI);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    void doFilterInternal_completesPartiallyConsumedKnownLengthJsonForErrors(int status)
            throws IOException, ServletException {
        byte[] body = "{\"value\":\"complete-after-error\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(false);
        when(restLoggingProperties.getMaxPayloadBytes()).thenReturn(1024);
        FilterChain partialReadChain = (wrappedRequest, wrappedResponse) -> {
            wrappedRequest.getInputStream().readNBytes(8);
            ((jakarta.servlet.http.HttpServletResponse) wrappedResponse).setStatus(status);
        };

        restLoggingFilter.doFilterInternal(request, response, partialReadChain);

        ArgumentCaptor<BoundedRequestCaptureWrapper> requestCaptor =
                ArgumentCaptor.forClass(BoundedRequestCaptureWrapper.class);
        verify(restLoggingService).log(requestCaptor.capture(), any(BoundedContentCachingResponseWrapper.class),
                any(LocalDateTime.class), anyString(), eq(null));
        PayloadCapture capture = requestCaptor.getValue().getPayloadCapture();
        assertThat(capture.isComplete()).isTrue();
        assertThat(capture.openStream().readAllBytes()).isEqualTo(body);
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 302})
    void doFilterInternal_doesNotCompletePartiallyConsumedJsonForNonErrors(int status)
            throws IOException, ServletException {
        byte[] body = "{\"value\":\"remain-partial\"}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent(body);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(restLoggingProperties.isTotallyExcluded(REQUEST_URI)).thenReturn(false);
        when(restLoggingProperties.getMaxPayloadBytes()).thenReturn(1024);
        FilterChain partialReadChain = (wrappedRequest, wrappedResponse) -> {
            wrappedRequest.getInputStream().readNBytes(8);
            ((jakarta.servlet.http.HttpServletResponse) wrappedResponse).setStatus(status);
        };

        restLoggingFilter.doFilterInternal(request, response, partialReadChain);

        ArgumentCaptor<BoundedRequestCaptureWrapper> requestCaptor =
                ArgumentCaptor.forClass(BoundedRequestCaptureWrapper.class);
        verify(restLoggingService).log(requestCaptor.capture(), any(BoundedContentCachingResponseWrapper.class),
                any(LocalDateTime.class), anyString(), eq(null));
        PayloadCapture capture = requestCaptor.getValue().getPayloadCapture();
        assertThat(capture.isComplete()).isFalse();
        assertThat(capture.getSizeBytes()).isEqualTo(8);
    }
}
