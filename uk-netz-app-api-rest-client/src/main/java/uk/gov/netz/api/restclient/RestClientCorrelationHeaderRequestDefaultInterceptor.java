package uk.gov.netz.api.restclient;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.netz.api.restlogging.RestLoggingUtils;

@Component
public class RestClientCorrelationHeaderRequestDefaultInterceptor
		implements RestClientCorrelationHeaderRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		addCorrelationIdToRequestFromRequestContext(request);
		return execution.execute(request, body);
	}

	private void addCorrelationIdToRequestFromRequestContext(HttpRequest request) {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if (servletRequestAttributes == null) {
			return;
		}

		HttpServletResponse response = servletRequestAttributes.getResponse();
		if (response == null) {
			return;
		}

		request.getHeaders().add(RestLoggingUtils.CORRELATION_ID_HEADER, UUID.randomUUID().toString());
	}

}
