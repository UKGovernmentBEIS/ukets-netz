package uk.gov.netz.api.restclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.restlogging.RestLoggingUtils;

@Log4j2
@Component
@RequiredArgsConstructor
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {
	
	private final RestClientLoggingService restClientLoggingService;
	
	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody, ClientHttpRequestExecution execution)
			throws IOException {
		final LocalDateTime requestTimestamp = LocalDateTime.now();
		
		final ClientHttpResponse response =  execution.execute(request, requestBody);
		
		try (final ByteArrayOutputStream buffer = new ByteArrayOutputStream();) {
			response.getBody().transferTo(buffer);
			final byte[] responseBody = buffer.toByteArray();

			restClientLoggingService.log(request, requestBody, response, responseBody, requestTimestamp,
					request.getHeaders().getFirst(RestLoggingUtils.CORRELATION_ID_HEADER),
					request.getHeaders().getFirst(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER));

			return new BufferedClientHttpResponse(response, responseBody);
		}
	}

	private static class BufferedClientHttpResponse implements ClientHttpResponse {
		private final ClientHttpResponse originalResponse;
		private final byte[] body;

		public BufferedClientHttpResponse(ClientHttpResponse originalResponse, byte[] body) {
			this.originalResponse = originalResponse;
			this.body = body;
		}

		@Override
		public InputStream getBody() throws IOException {
			return new ByteArrayInputStream(body);
		}

		@Override
		public HttpHeaders getHeaders() {
			return originalResponse.getHeaders();
		}

		@Override
		public String getStatusText() throws IOException {
			return originalResponse.getStatusText();
		}

		@Override
		public void close() {
			originalResponse.close();
		}

		@Override
		public HttpStatusCode getStatusCode() throws IOException {
			return originalResponse.getStatusCode();
		}
	}
	
}
