package uk.gov.netz.api.restlogging;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

        when(restLoggingProperties.getExcludedTotallyUriPatterns()).thenReturn(List.of("/api/test"));
        
        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNull();
        
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
        verify(restLoggingProperties, times(1)).getExcludedTotallyUriPatterns();
        verifyNoInteractions(restLoggingService);
    }

    @Test
    void doFilterInternal_correlation_id_new() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());

        when(restLoggingProperties.getExcludedTotallyUriPatterns()).thenReturn(List.of());
        
        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNull();
        
		Mockito.verify(restLoggingService, Mockito.times(1)).log(any(MultiReadHttpServletRequestWrapper.class),
				any(ContentCachingResponseWrapper.class), any(LocalDateTime.class), anyString(), eq(null));
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).getExcludedTotallyUriPatterns();
    }
    
    @Test
    void doFilterInternal_correlation_id_in_request() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);
        request.addHeader(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
        request.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());
        
        when(restLoggingProperties.getExcludedTotallyUriPatterns()).thenReturn(List.of());

        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNotNull();
        
        Mockito.verify(restLoggingService, Mockito.times(1)).log(any(MultiReadHttpServletRequestWrapper.class),
        		any(ContentCachingResponseWrapper.class),
                any(LocalDateTime.class), eq("1234"), eq("5678"));
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).getExcludedTotallyUriPatterns();
    }
    
    @Test
    void doFilterInternal_correlation_id_in_reponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.POST.name(), REQUEST_URI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.ACCEPTED.value());
        response.setHeader(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
        request.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
        
        when(restLoggingProperties.getExcludedTotallyUriPatterns()).thenReturn(List.of());

        restLoggingFilter.doFilterInternal(request, response, filterChain);
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).isNotNull();
        assertThat(response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isNotNull();
        
        Mockito.verify(restLoggingService, Mockito.times(1)).log(any(MultiReadHttpServletRequestWrapper.class),
        		any(ContentCachingResponseWrapper.class), any(LocalDateTime.class), eq("1234"), eq("5678"));
        Mockito.verify(filterChain, Mockito.times(1)).doFilter(any(), any());
        verify(restLoggingProperties, times(1)).getExcludedTotallyUriPatterns();
    }
}