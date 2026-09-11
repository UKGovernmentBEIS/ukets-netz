package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
public class RestLoggingService {

    private static final String REDACTED = "[REDACTED]";
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ROOT),
            HttpHeaders.COOKIE.toLowerCase(Locale.ROOT),
            HttpHeaders.SET_COOKIE.toLowerCase(Locale.ROOT),
            HttpHeaders.PROXY_AUTHENTICATE.toLowerCase(Locale.ROOT),
            HttpHeaders.PROXY_AUTHORIZATION.toLowerCase(Locale.ROOT));

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
        log(request, response, requestTimestamp, correlationIdHeader, correlationParentIdHeader, null, null);
    }

    public void log(MultiReadHttpServletRequestWrapper request, ContentCachingResponseWrapper response,
            LocalDateTime requestTimestamp, String correlationIdHeader, String correlationParentIdHeader,
            String originalRequestUri, HttpStatus originalHttpStatus) {
        PayloadCapture requestCapture = captureLegacyRequest(request);
        PayloadCapture responseCapture = captureLegacyResponse(response);
        logCaptured(request, response, requestCapture, responseCapture, requestTimestamp,
                correlationIdHeader, correlationParentIdHeader, originalRequestUri, originalHttpStatus);
    }

    void log(BoundedRequestCaptureWrapper request, BoundedContentCachingResponseWrapper response,
            LocalDateTime requestTimestamp, String correlationIdHeader, String correlationParentIdHeader) {
        logCaptured(request, response, request.getPayloadCapture(), response.getPayloadCapture(), requestTimestamp,
                correlationIdHeader, correlationParentIdHeader, null, null);
    }

    private void logCaptured(HttpServletRequest request, HttpServletResponse response,
            PayloadCapture requestCapture, PayloadCapture responseCapture, LocalDateTime requestTimestamp,
            String correlationId, String correlationParentId, String originalRequestUri, HttpStatus originalHttpStatus) {
        String matchUri = originalRequestUri != null ? originalRequestUri : request.getRequestURI();
        String requestUri = originalRequestUri != null
                ? limit(originalRequestUri, RestLoggingSizeLimits.MAX_URI_BYTES)
                : getRequestUri(request);
        HttpStatus httpResponseStatus = originalHttpStatus != null
                ? originalHttpStatus
                : HttpStatus.valueOf(response.getStatus());

        Level eventLevel;
        if (httpResponseStatus.isError()) {
            eventLevel = Level.ERROR;
        } else if (log.isEnabled(logLevel) && !restLoggingProperties.isExcluded(matchUri)) {
            eventLevel = logLevel;
        } else {
            requestCapture.releaseContent();
            responseCapture.releaseContent();
            return;
        }

        emitRequest(request, requestCapture, requestUri,
                limit(correlationId, RestLoggingSizeLimits.MAX_ID_BYTES),
                limit(correlationParentId, RestLoggingSizeLimits.MAX_ID_BYTES),
                requestTimestamp, eventLevel, httpResponseStatus.isError());
        emitResponse(response, responseCapture, httpResponseStatus,
                limit(correlationId, RestLoggingSizeLimits.MAX_ID_BYTES),
                limit(correlationParentId, RestLoggingSizeLimits.MAX_ID_BYTES),
                request, requestUri, requestTimestamp, eventLevel);
    }

    private void emitRequest(HttpServletRequest request, PayloadCapture capture, String requestUri,
            String correlationId, String correlationParentId, LocalDateTime requestTimestamp, Level eventLevel,
            boolean includeRawInvalidPayload) {
        CaptureResult captureResult = extractPayload(capture, request.getContentType(), includeRawInvalidPayload);
        HeadersResult headersResult = getRequestHeaders(request);
        RestLoggingEntry requestLog = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.REQUEST)
                .headers(headersResult.headers())
                .headersCapture(headersResult.metadata())
                .payload(captureResult.payload())
                .payloadCapture(captureResult.metadata())
                .uri(requestUri)
                .userId(limit(request.getRemoteUser(), RestLoggingSizeLimits.MAX_ID_BYTES))
                .httpMethod(HttpMethod.valueOf(request.getMethod()).name())
                .correlationId(correlationId)
                .correlationParentId(correlationParentId)
                .timestamp(requestTimestamp)
                .build();
        log.log(eventLevel, requestLog);
    }

    private void emitResponse(HttpServletResponse response, PayloadCapture capture, HttpStatus httpStatus,
            String correlationId, String correlationParentId, HttpServletRequest request, String requestUri,
            LocalDateTime requestTimestamp, Level eventLevel) {
        CaptureResult captureResult = extractPayload(capture, response.getContentType(), false);
        HeadersResult headersResult = getResponseHeaders(response);
        RestLoggingEntry responseLog = RestLoggingEntry.builder()
                .type(RestLoggingEntry.RestLoggingEntryType.RESPONSE)
                .headers(headersResult.headers())
                .headersCapture(headersResult.metadata())
                .payload(captureResult.payload())
                .payloadCapture(captureResult.metadata())
                .uri(requestUri)
                .userId(limit(request.getRemoteUser(), RestLoggingSizeLimits.MAX_ID_BYTES))
                .httpStatus(httpStatus.value())
                .correlationId(correlationId)
                .correlationParentId(correlationParentId)
                .responseTimeInMillis(ChronoUnit.MILLIS.between(requestTimestamp, LocalDateTime.now()))
                .build();
        log.log(eventLevel, responseLog);
    }

    private CaptureResult extractPayload(PayloadCapture capture, String contentType, boolean includeRawInvalidPayload) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("sizeBytes", capture.getSizeBytes());
        metadata.put("limitBytes", capture.getLimitBytes());
        if (contentType != null) {
            metadata.put("contentType", limit(contentType, RestLoggingSizeLimits.MAX_HEADER_VALUE_BYTES));
        }

        try {
            if (capture.isSkipped()) {
                metadata.put("status", "SKIPPED");
                metadata.put("reason", capture.getSkipReason());
                return new CaptureResult(Map.of(), metadata);
            }
            if (capture.isTruncated()) {
                metadata.put("status", "TRUNCATED");
                return new CaptureResult(Map.of(), metadata);
            }
            if (capture.getSizeBytes() == 0) {
                metadata.put("status", "EMPTY");
                return new CaptureResult(Map.of(), metadata);
            }
            if (!capture.isComplete()) {
                metadata.put("status", "SKIPPED");
                metadata.put("reason", "NOT_FULLY_CONSUMED");
                return new CaptureResult(Map.of(), metadata);
            }

            Map<String, Object> payload;
            try (InputStream inputStream = capture.openStream()) {
                payload = objectMapper.readValue(inputStream, new TypeReference<>() {
                });
            }
            metadata.put("status", "COMPLETE");
            return new CaptureResult(payload, metadata);
        } catch (IOException | RuntimeException ex) {
            metadata.put("status", "FAILED");
            metadata.put("reason", "INVALID_JSON");
            Map<String, Object> payload = includeRawInvalidPayload
                    ? Map.of("rawBody", capture.contentAsString(resolveCharset(contentType)))
                    : Map.of();
            return new CaptureResult(payload, metadata);
        } finally {
            capture.releaseContent();
        }
    }

    private HeadersResult getRequestHeaders(HttpServletRequest request) {
        return sanitizeHeaders(Collections.list(request.getHeaderNames()).stream()
                .map(header -> new AbstractMap.SimpleImmutableEntry<>(header, request.getHeader(header)))
                .toList());
    }

    private HeadersResult getResponseHeaders(HttpServletResponse response) {
        return sanitizeHeaders(response.getHeaderNames().stream()
                .map(header -> new AbstractMap.SimpleImmutableEntry<>(header, response.getHeader(header)))
                .toList());
    }

    private HeadersResult sanitizeHeaders(java.util.List<? extends Map.Entry<String, String>> sourceHeaders) {
        Map<String, String> retained = new LinkedHashMap<>();
        int serializedBytes = 2;
        int omittedCount = 0;
        int truncatedValueCount = 0;

        for (Map.Entry<String, String> header : sourceHeaders) {
            SanitizedHeader sanitized = sanitizeHeader(header.getKey(), header.getValue());
            int entryBytes = (retained.isEmpty() ? 0 : 1)
                    + RestLoggingSizeLimits.jsonStringBytes(header.getKey())
                    + 1
                    + RestLoggingSizeLimits.jsonStringBytes(sanitized.value());
            if (serializedBytes + entryBytes > RestLoggingSizeLimits.MAX_HEADERS_BYTES) {
                omittedCount++;
                continue;
            }
            retained.put(header.getKey(), sanitized.value());
            serializedBytes += entryBytes;
            if (sanitized.truncated()) {
                truncatedValueCount++;
            }
        }

        if (omittedCount == 0 && truncatedValueCount == 0) {
            return new HeadersResult(retained, Map.of());
        }
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("status", "TRUNCATED");
        metadata.put("limitBytes", RestLoggingSizeLimits.MAX_HEADERS_BYTES);
        metadata.put("retainedCount", retained.size());
        metadata.put("omittedCount", omittedCount);
        metadata.put("truncatedValueCount", truncatedValueCount);
        return new HeadersResult(retained, metadata);
    }

    private SanitizedHeader sanitizeHeader(String name, String value) {
        if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return new SanitizedHeader(REDACTED, false);
        }
        RestLoggingSizeLimits.LimitedString limited = RestLoggingSizeLimits.limitUtf8(
                value, RestLoggingSizeLimits.MAX_HEADER_VALUE_BYTES);
        return new SanitizedHeader(limited.value(), limited.truncated());
    }

    private static String limit(String value, int maxBytes) {
        return RestLoggingSizeLimits.limitUtf8(value, maxBytes).value();
    }

    private static Charset resolveCharset(String contentType) {
        if (contentType != null) {
            try {
                Charset charset = MediaType.parseMediaType(contentType).getCharset();
                if (charset != null) {
                    return charset;
                }
            } catch (IllegalArgumentException ex) {
                // Invalid or unsupported charset declarations fall back to JSON's UTF-8 default.
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static String getRequestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return limit(request.getRequestURI(), RestLoggingSizeLimits.MAX_URI_BYTES);
        }
        return RestLoggingSizeLimits.joinAndLimitUtf8(
                RestLoggingSizeLimits.MAX_URI_BYTES, request.getRequestURI(), "?", queryString).value();
    }

    private PayloadCapture captureLegacyRequest(HttpServletRequest request) {
        if (RestLoggingUtils.isMultipart(request.getContentType())) {
            return captureMultipartJsonPart(request);
        }
        PayloadCapture existingCapture = findBoundedCapture(request);
        if (existingCapture != null) {
            return existingCapture;
        }
        PayloadCapture capture = new PayloadCapture(
                restLoggingProperties.getMaxPayloadBytes(), request.getContentLengthLong());
        if (request.getContentType() != null && !RestLoggingUtils.isJsonContentType(request.getContentType())) {
            capture.skip("NON_JSON_CONTENT_TYPE");
        }
        try (InputStream inputStream = request.getInputStream()) {
            transfer(inputStream, capture);
        } catch (IOException ex) {
            capture.skip("READ_ERROR");
        }
        return capture;
    }

    private static PayloadCapture findBoundedCapture(HttpServletRequest request) {
        HttpServletRequest current = request;
        while (current instanceof HttpServletRequestWrapper wrapper) {
            if (current instanceof BoundedRequestCaptureWrapper boundedRequest) {
                return boundedRequest.getPayloadCapture();
            }
            if (!(wrapper.getRequest() instanceof HttpServletRequest nestedRequest)) {
                return null;
            }
            current = nestedRequest;
        }
        return null;
    }

    private PayloadCapture captureLegacyResponse(ContentCachingResponseWrapper response) {
        PayloadCapture capture = new PayloadCapture(
                restLoggingProperties.getMaxPayloadBytes(), response.getContentSize());
        if (response.getHeader(HttpHeaders.CONTENT_DISPOSITION) != null) {
            capture.skip("FILE_CONTENT");
        } else if (response.getContentType() != null && !RestLoggingUtils.isJsonContentType(response.getContentType())) {
            capture.skip("NON_JSON_CONTENT_TYPE");
        }
        try (InputStream inputStream = response.getContentInputStream()) {
            transfer(inputStream, capture);
        } catch (IOException ex) {
            capture.skip("READ_ERROR");
        }
        return capture;
    }

    private PayloadCapture captureMultipartJsonPart(HttpServletRequest request) {
        try {
            Optional<Part> jsonPart = request.getParts().stream()
                    .filter(part -> RestLoggingUtils.isJsonContentType(part.getContentType()))
                    .findFirst();
            if (jsonPart.isEmpty()) {
                return skippedCapture(restLoggingProperties.getMaxPayloadBytes(), "NO_JSON_PART");
            }
            Part part = jsonPart.get();
            PayloadCapture capture = new PayloadCapture(restLoggingProperties.getMaxPayloadBytes(), part.getSize());
            try (InputStream inputStream = part.getInputStream()) {
                transfer(inputStream, capture);
            }
            return capture;
        } catch (IOException | ServletException | IllegalStateException ex) {
            return skippedCapture(restLoggingProperties.getMaxPayloadBytes(), "READ_ERROR");
        }
    }

    private static PayloadCapture skippedCapture(int limitBytes, String reason) {
        PayloadCapture capture = new PayloadCapture(limitBytes, -1);
        capture.skip(reason);
        capture.markComplete();
        return capture;
    }

    private static void transfer(InputStream inputStream, PayloadCapture capture) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            capture.accept(buffer, 0, read);
        }
        capture.markComplete();
    }

    private record CaptureResult(Map<String, Object> payload, Map<String, Object> metadata) {
    }

    private record HeadersResult(Map<String, String> headers, Map<String, Object> metadata) {
    }

    private record SanitizedHeader(String value, boolean truncated) {
    }
}
