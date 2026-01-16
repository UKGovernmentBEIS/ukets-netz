package uk.gov.netz.api.restclient;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import uk.gov.netz.api.restlogging.RestLoggingUtils;

@ExtendWith(MockitoExtension.class)
class RestClientLoggingInterceptorTest {

	@InjectMocks
	private RestClientLoggingInterceptor cut;
	
	@Mock
	private RestClientLoggingService restClientLoggingService;

	@Mock
    private HttpRequest request;
	
	@Mock
	private ClientHttpRequestExecution execution;
	
    @Test
    void intercept() throws IOException {
    	byte[] requestBody = new byte[0];
    	HttpHeaders requestHttpHeaders = new HttpHeaders();
    	requestHttpHeaders.add(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
    	requestHttpHeaders.add(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
    	when(request.getHeaders()).thenReturn(requestHttpHeaders);
    	
    	ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
    	when(execution.execute(request, requestBody)).thenReturn(response);
    	when(response.getBody()).thenReturn(Mockito.mock(InputStream.class));
    	HttpHeaders responseHttpHeaders = new HttpHeaders();
    	responseHttpHeaders.add(RestLoggingUtils.CORRELATION_ID_HEADER, "1234");
    	responseHttpHeaders.add(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, "5678");
    	
        cut.intercept(request, new byte[0], execution);
        
		verify(restClientLoggingService, times(1)).log(Mockito.eq(request), Mockito.eq(requestBody),
				Mockito.eq(response), Mockito.any(byte[].class), Mockito.any(LocalDateTime.class), Mockito.eq("1234"), Mockito.eq("5678"));
    }
	
}
