package uk.gov.netz.api.restlogging;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RestLoggingService {
    private final Level logLevel;
    private final RestLoggingProperties restLoggingProperties;
    private final ObjectMapper objectMapper;

    public RestLoggingService(RestLoggingProperties restLoggingProperties, ObjectMapper objectMapper) {
        this.restLoggingProperties = restLoggingProperties;
        this.objectMapper = objectMapper;
        logLevel = Level.valueOf(restLoggingProperties.getLevel().name());
    }
    
	public void log(MultiReadHttpServletRequestWrapper request, ContentCachingResponseWrapper response,
			LocalDateTime requestTimestamp, String correlationIdHeader, String correlationParentIdHeader) {
		this.log(request, response, requestTimestamp, correlationIdHeader, correlationParentIdHeader, null, null);
	}
    
	public void log(MultiReadHttpServletRequestWrapper request, ContentCachingResponseWrapper response,
			LocalDateTime requestTimestamp, String correlationIdHeader, String correlationParentIdHeader,
			String originalRequestUri, HttpStatus originalHttpStatus) {
		final String requestUri = originalRequestUri != null ? originalRequestUri
				: ObjectUtils.isEmpty(request.getQueryString()) ? request.getRequestURI()
						: request.getRequestURI().concat(request.getQueryString());

		final HttpStatus httpResponseStatus = originalHttpStatus != null ? originalHttpStatus
				: HttpStatus.valueOf(response.getStatus());

		final RestLoggingEntry requestLog = getRequestRestLoggingEntry(request, requestUri, correlationIdHeader,
				correlationParentIdHeader, requestTimestamp);

		final RestLoggingEntry responseLog = getResponseRestLoggingEntry(response, httpResponseStatus,
				correlationIdHeader, correlationParentIdHeader, request, requestUri, requestTimestamp);

		if (httpResponseStatus.isError()) {
			log.log(Level.ERROR, requestLog);
			log.log(Level.ERROR, responseLog);
		} else if (log.isEnabled(logLevel)
				&& !RestLoggingUtils.isUriContainedInList(requestUri, this.restLoggingProperties.getExcludedUriPatterns())) {
			log.log(logLevel, requestLog);
			log.log(logLevel, responseLog);
		}
	}
    
	private RestLoggingEntry getRequestRestLoggingEntry(HttpServletRequest request, String requestUri,
			String correlationId, String correlationParentId, LocalDateTime requestTimestamp) {
        String user = request.getRemoteUser();

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

        Map<String, Object> payload = getRequestPayloadAsMap(request);

        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames()).stream()
                .filter(header -> !header.equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
                .forEach(header -> headers.put(header, request.getHeader(header)));


        return RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .headers(headers)
                .payload(payload)
                .uri(requestUri)
                .userId(user)
                .httpMethod(httpMethod.name())
                .correlationId(correlationId)
                .correlationParentId(correlationParentId)
                .timestamp(requestTimestamp)
                .build();
    }

    private RestLoggingEntry getResponseRestLoggingEntry(ContentCachingResponseWrapper response,
                                                         HttpStatus httpStatus,
                                                         String correlationId,
                                                         String correlationParentId,
                                                         MultiReadHttpServletRequestWrapper request,
                                                         String requestUri,
                                                         LocalDateTime requestTimestamp) {
        String user = request.getRemoteUser();

        Map<String, Object> payload = getResponsePayloadAsMap(response);

        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames()
                .forEach(header -> headers.put(header, response.getHeader(header)));

        return RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .headers(headers)
                .payload(payload)
                .uri(requestUri)
                .userId(user)
                .httpStatus(httpStatus.value())
                .correlationId(correlationId)
                .correlationParentId(correlationParentId)
                .responseTimeInMillis(ChronoUnit.MILLIS.between(requestTimestamp, LocalDateTime.now()))
                .build();
    }

    private Map<String, Object> getRequestPayloadAsMap(HttpServletRequest request) {
        try {
            if (request.getContentType() != null && request.getContentType().contains(MULTIPART_FORM_DATA_VALUE)) {
                Optional<Part> requestJsonPart = request.getParts().stream()
                        .filter(part -> part.getContentType().equals(APPLICATION_JSON_VALUE))
                        .findFirst();

                if (requestJsonPart.isPresent()) {
                    Part part = requestJsonPart.get();
                    if (part.getSize() > 0) {
                        return RestLoggingUtils.getPayloadAsMap(part.getInputStream().readAllBytes(), this.objectMapper);
                    }
                }

            } else {
                return RestLoggingUtils.getPayloadAsMap(request.getInputStream().readAllBytes(), this.objectMapper);
            }
        } catch (IOException | ServletException ex) {
            return Map.of("body", "[unknownContent]");
        }
        return Map.of("body", "[unknownContent]");
    }

    private Map<String, Object> getResponsePayloadAsMap(ContentCachingResponseWrapper response) {
        if (response.getHeader(CONTENT_DISPOSITION) != null) {
            return Map.of("body", "[fileContent]");
        }
        return RestLoggingUtils.getPayloadAsMap(response.getContentAsByteArray(), this.objectMapper);
    }

}
