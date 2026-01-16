package uk.gov.netz.api.restclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.netz.api.restlogging.RestLoggingUtils;

@ExtendWith(MockitoExtension.class)
class RestClientCorrelationParentHeaderRequestDefaultInterceptorTest {

	@InjectMocks
	private RestClientCorrelationParentHeaderRequestDefaultInterceptor cut;

	@Mock
	private HttpRequest httpRequest;
	
	@Mock
    private ClientHttpRequestExecution execution;
	
	@Mock
    private HttpServletResponse servletResponse;
	
	@Test  
	void intercept_with_corellation() throws IOException {
		ServletRequestAttributes servletRequestAttributes = Mockito.mock(ServletRequestAttributes.class);
        when(servletRequestAttributes.getResponse()).thenReturn(servletResponse);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        
        HttpHeaders headers = new HttpHeaders();
        when(httpRequest.getHeaders()).thenReturn(headers);
        
        String correlationId = UUID.randomUUID().toString();
        when(servletResponse.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER)).thenReturn(correlationId);
        
        cut.intercept(httpRequest, null, execution);
        
        verify(servletResponse, times(1)).getHeader(RestLoggingUtils.CORRELATION_ID_HEADER);

        assertThat(headers).containsKey(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER);
        assertThat(headers.getFirst(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER)).isEqualTo(correlationId);
	}

}
