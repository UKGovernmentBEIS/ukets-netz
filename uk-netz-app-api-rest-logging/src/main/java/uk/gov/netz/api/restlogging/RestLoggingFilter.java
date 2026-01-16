package uk.gov.netz.api.restlogging;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filter used to log Rest API requests/responses.
 */
@Component
@RequiredArgsConstructor
public class RestLoggingFilter extends OncePerRequestFilter {
    private final RestLoggingService restLoggingService;
    private final RestLoggingProperties restLoggingProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
		if (RestLoggingUtils.isUriContainedInList(request.getRequestURI(), restLoggingProperties.getExcludedTotallyUriPatterns())) {
			filterChain.doFilter(request, response);
			return;
		} else {
			addCorrelationIdToResponseHeader(request, response);
			addCorrelationParentIdToResponseHeader(request, response);

			LocalDateTime requestTimestamp = LocalDateTime.now();

			MultiReadHttpServletRequestWrapper wrappedRequest = new MultiReadHttpServletRequestWrapper(request);

			ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
			if (isAsyncDispatch(request)) {
				filterChain.doFilter(request, response);
				return;
			}

			filterChain.doFilter(wrappedRequest, wrappedResponse);

			restLoggingService.log(wrappedRequest, wrappedResponse, requestTimestamp,
					response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER),
					response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER));

			wrappedResponse.copyBodyToResponse();
		}
    }
    
	private void addCorrelationIdToResponseHeader(HttpServletRequest request, HttpServletResponse response) {
		final String correlationId = resolveCorrelationId(request, response,
				RestLoggingUtils.CORRELATION_ID_HEADER);
		response.addHeader(RestLoggingUtils.CORRELATION_ID_HEADER,
				correlationId != null ? correlationId : UUID.randomUUID().toString());
	}
	
	private void addCorrelationParentIdToResponseHeader(HttpServletRequest request, HttpServletResponse response) {
		final String correlationId = resolveCorrelationId(request, response,
				RestLoggingUtils.CORRELATION_PARENT_ID_HEADER);
		if (correlationId != null) {
			response.addHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER, correlationId);
		}
	}
    
	private String resolveCorrelationId(HttpServletRequest request, HttpServletResponse response,
			String correlationIdHeader) {
		final String requestCorrelationId = request.getHeader(correlationIdHeader);
		if (requestCorrelationId != null) {
			return requestCorrelationId;
		}

		final String responseCorrelationId = response.getHeader(correlationIdHeader);
		if (responseCorrelationId != null) {
			return responseCorrelationId;
		}

		return null;
	}
}
