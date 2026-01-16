package uk.gov.netz.api.restclient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;
import uk.gov.netz.api.restlogging.RestLoggingEntry;
import uk.gov.netz.api.restlogging.RestLoggingProperties;
import uk.gov.netz.api.restlogging.RestLoggingUtils;

@Log4j2
@Service
class RestClientLoggingService {

	private final Level logLevel;
	private final RestLoggingProperties restClientLoggingProperties;
	private final ObjectMapper objectMapper;

	public RestClientLoggingService(RestLoggingProperties restClientLoggingProperties, ObjectMapper objectMapper) {
		this.restClientLoggingProperties = restClientLoggingProperties;
		this.objectMapper = objectMapper;
		logLevel = Level.valueOf(restClientLoggingProperties.getLevel().name());
	}
	
	public void log(HttpRequest request, byte[] requestBody, ClientHttpResponse response, byte[] responseBody, LocalDateTime requestTimestamp,
			String correlationIdHeader, String correlationParentIdHeader) throws IOException {
		final RestLoggingEntry requestLog = getRequestLoggingEntry(request, requestBody, correlationIdHeader,
				correlationParentIdHeader, requestTimestamp);

		final RestLoggingEntry responseLog = getResponseLoggingEntry(request, response, responseBody,
				correlationIdHeader, correlationParentIdHeader, requestTimestamp);

		if (response.getStatusCode().isError()) {
			log.log(Level.ERROR, requestLog);
			log.log(Level.ERROR, responseLog);
		} else if (log.isEnabled(logLevel)
				&& !RestLoggingUtils.isUriContainedInList(request.getURI().toString(), this.restClientLoggingProperties.getExcludedUriPatterns())) {
			log.log(logLevel, requestLog);
			log.log(logLevel, responseLog);
		}
	}
	
	private RestLoggingEntry getRequestLoggingEntry(final HttpRequest request, final byte[] body,
			final String correlationId, final String correlationParentId, final LocalDateTime requestTimestamp) {
		return RestLoggingEntry.builder()
		        .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
		        .headers(request.getHeaders().toSingleValueMap()
		        		.entrySet()
		        		.stream()
		        		.filter(e -> !e.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
		        		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
		        .payload(RestLoggingUtils.getPayloadAsMap(body, this.objectMapper))
		        .uri(request.getURI().getPath())
		        .userId(request.getURI().getUserInfo())
		        .httpMethod(request.getMethod().name())
		        .correlationId(correlationId)
		        .correlationParentId(correlationParentId)
		        .timestamp(requestTimestamp)
		        .build();
	}
	
	private RestLoggingEntry getResponseLoggingEntry(final HttpRequest request, final ClientHttpResponse response,
			final byte[] responseBody, final String correlationId, final String correlationParentId,
			final LocalDateTime requestTimestamp) throws IOException {
		return RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .headers(response.getHeaders().toSingleValueMap())
                .payload(getResponsePayloadAsMap(response.getHeaders(), responseBody))
                .uri(request.getURI().getPath())
                .userId(request.getURI().getUserInfo())
                .httpStatus(response.getStatusCode().value())
                .correlationId(correlationId)
                .correlationParentId(correlationParentId)
                .responseTimeInMillis(ChronoUnit.MILLIS.between(requestTimestamp, LocalDateTime.now()))
                .build();
	}
	
	private Map<String, Object> getResponsePayloadAsMap(HttpHeaders httpHeaders, byte[] responseBody) {
		if (httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION) != null) {
			return Map.of("body", "[fileContent]");
		}

		return RestLoggingUtils.getPayloadAsMap(responseBody, this.objectMapper);
	}

}
