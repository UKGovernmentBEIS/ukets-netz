package uk.gov.netz.api.restlogging;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
        addCorrelationIdToResponseHeader(request, response);
        addCorrelationParentIdToResponseHeader(request, response);
        if (restLoggingProperties.isTotallyExcluded(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        final LocalDateTime requestTimestamp = LocalDateTime.now();
        BoundedRequestCaptureWrapper wrappedRequest = new BoundedRequestCaptureWrapper(
                request, restLoggingProperties.getMaxPayloadBytes());
        BoundedContentCachingResponseWrapper wrappedResponse = new BoundedContentCachingResponseWrapper(
                response, restLoggingProperties.getMaxPayloadBytes());

        filterChain.doFilter(wrappedRequest, wrappedResponse);
        if (response.getStatus() >= 400 && response.getStatus() <= 599) {
            wrappedRequest.completeCapture();
        }
        wrappedResponse.finishCapture();

        restLoggingService.log(wrappedRequest, wrappedResponse, requestTimestamp,
                response.getHeader(RestLoggingUtils.CORRELATION_ID_HEADER),
                response.getHeader(RestLoggingUtils.CORRELATION_PARENT_ID_HEADER));
    }

    private void addCorrelationIdToResponseHeader(HttpServletRequest request, HttpServletResponse response) {
        final String correlationId = resolveCorrelationId(request, response, RestLoggingUtils.CORRELATION_ID_HEADER);
        response.addHeader(RestLoggingUtils.CORRELATION_ID_HEADER,
                correlationId != null ? correlationId : UUID.randomUUID().toString());
    }

    private void addCorrelationParentIdToResponseHeader(HttpServletRequest request, HttpServletResponse response) {
        final String correlationId = resolveCorrelationId(
                request, response, RestLoggingUtils.CORRELATION_PARENT_ID_HEADER);
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
