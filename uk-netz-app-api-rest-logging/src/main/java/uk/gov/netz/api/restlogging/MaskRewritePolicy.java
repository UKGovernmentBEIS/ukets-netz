package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(name = "MaskRewritePolicy", category = Core.CATEGORY_NAME, elementType = "rewritePolicy", printObject = true)
public class MaskRewritePolicy implements RewritePolicy {

    private static final Logger NO_MASK_LOGGER = LogManager.getLogger("NoMaskLogger");
    private static final int MAX_GENERIC_MESSAGE_LENGTH = 768 * 1024;
    private static final String MASKED_VALUE = "[REDACTED]";
    private static final String MASKING_FAILED = "[MESSAGE OMITTED: MASKING FAILED]";
    private static final String OVERSIZED_MESSAGE = "[MESSAGE OMITTED: OVERSIZED]";

    private final ObjectMapper objectMapper;
    private final Set<String> sensitiveKeys;
    private final Pattern jsonTextPattern;
    private final Pattern javaTextPattern;

    private MaskRewritePolicy(List<Property> properties) {
        objectMapper = new ObjectMapper().findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        sensitiveKeys = properties.stream()
                .filter(java.util.Objects::nonNull)
                .filter(property -> "payloadProperty".equals(property.getName()))
                .map(Property::getValue)
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());

        String keyAlternation = sensitiveKeys.isEmpty()
                ? "(?!)"
                : sensitiveKeys.stream().map(Pattern::quote).collect(Collectors.joining("|"));
        jsonTextPattern = Pattern.compile(
                "(\\\"(?:" + keyAlternation + ")\\\"\\s*:\\s*\\\")([^\\\"]*)(\\\")",
                Pattern.CASE_INSENSITIVE);
        javaTextPattern = Pattern.compile(
                "(\\b(?:" + keyAlternation + ")\\s*=\\s*)([^,\\)]*)()",
                Pattern.CASE_INSENSITIVE);
    }

    @PluginFactory
    public static MaskRewritePolicy create(@PluginElement("Properties") Property[] properties) {
        if (properties == null || properties.length == 0) {
            return null;
        }
        return new MaskRewritePolicy(Arrays.asList(properties));
    }

    @Override
    public LogEvent rewrite(LogEvent source) {
        Message message = source.getMessage();
        try {
            if (message instanceof ObjectMessage objectMessage) {
                return rewriteObjectMessage(source, objectMessage);
            }
            return rewriteTextMessage(source, message);
        } catch (RuntimeException ex) {
            NO_MASK_LOGGER.warn("Log message omitted because masking failed. Type: {}. Cause: {}",
                    message.getClass().getName(), ex.getClass().getSimpleName());
            return withMessage(source, new SimpleMessage(MASKING_FAILED));
        }
    }

    private LogEvent rewriteObjectMessage(LogEvent source, ObjectMessage message) {
        Object parameter = message.getParameter();
        if (parameter instanceof String stringParameter) {
            return rewriteText(source, stringParameter);
        }
        if (parameter instanceof RestLoggingEntry restLoggingEntry) {
            return withMessage(source, new ObjectMessage(toMaskedMap(restLoggingEntry)));
        }

        Object genericValue = objectMapper.convertValue(parameter, Object.class);
        maskMutableValue(genericValue);
        return withMessage(source, new ObjectMessage(genericValue));
    }

    private LogEvent rewriteTextMessage(LogEvent source, Message message) {
        return rewriteText(source, message.getFormattedMessage());
    }

    private LogEvent rewriteText(LogEvent source, String text) {
        if (sensitiveKeys.isEmpty()) {
            return source;
        }
        if (text.length() > MAX_GENERIC_MESSAGE_LENGTH) {
            return withMessage(source, new SimpleMessage(OVERSIZED_MESSAGE));
        }
        String maskedJson = maskJsonText(text);
        if (maskedJson != null) {
            return maskedJson.equals(text) ? source : withMessage(source, new SimpleMessage(maskedJson));
        }

        String maskedText = replaceMatches(text, jsonTextPattern);
        maskedText = replaceMatches(maskedText, javaTextPattern);
        return maskedText.equals(text) ? source : withMessage(source, new SimpleMessage(maskedText));
    }

    private String maskJsonText(String text) {
        String trimmed = text.stripLeading();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return null;
        }
        try {
            Object value = objectMapper.readValue(text, Object.class);
            return maskMutableValue(value) ? objectMapper.writeValueAsString(value) : text;
        } catch (Exception ex) {
            return MASKING_FAILED;
        }
    }

    private String replaceMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return text;
        }
        StringBuilder masked = new StringBuilder(text.length());
        do {
            matcher.appendReplacement(masked,
                    Matcher.quoteReplacement(matcher.group(1) + MASKED_VALUE + matcher.group(matcher.groupCount())));
        } while (matcher.find());
        matcher.appendTail(masked);
        return masked.toString();
    }

    private Map<String, Object> toMaskedMap(RestLoggingEntry entry) {
        Map<String, Object> result = new LinkedHashMap<>();
        putIfNotNull(result, "type", entry.getType());
        putIfNotNull(result, "correlationId", entry.getCorrelationId());
        putIfNotNull(result, "correlationParentId", entry.getCorrelationParentId());
        result.put("headers", maskOwnedValue(entry.getHeaders()));
        if (entry.getHeadersCapture() != null && !entry.getHeadersCapture().isEmpty()) {
            result.put("headersCapture", maskOwnedValue(entry.getHeadersCapture()));
        }
        result.put("payload", maskPayload(entry));
        result.put("payloadCapture", maskOwnedValue(entry.getPayloadCapture()));
        putIfNotNull(result, "uri", entry.getUri());
        putIfNotNull(result, "userId", entry.getUserId());
        putIfNotNull(result, "httpMethod", entry.getHttpMethod());
        result.put("httpStatus", entry.getHttpStatus());
        putIfNotNull(result, "timestamp", entry.getTimestamp());
        putIfNotNull(result, "responseTimeInMillis", entry.getResponseTimeInMillis());
        return result;
    }

    private Object maskOwnedValue(Object value) {
        try {
            maskMutableValue(value);
            return value;
        } catch (UnsupportedOperationException ex) {
            Object mutableCopy = objectMapper.convertValue(value, Object.class);
            maskMutableValue(mutableCopy);
            return mutableCopy;
        }
    }

    private Object maskPayload(RestLoggingEntry entry) {
        if (isInvalidRawPayload(entry.getPayload(), entry.getPayloadCapture())) {
            Map<String, Object> payload = new LinkedHashMap<>(entry.getPayload());
            String rawBody = (String) payload.get("rawBody");
            String masked = replaceMatches(rawBody, jsonTextPattern);
            payload.put("rawBody", replaceMatches(masked, javaTextPattern));
            return payload;
        }
        return maskOwnedValue(entry.getPayload());
    }

    private static boolean isInvalidRawPayload(Map<String, Object> payload, Map<String, Object> metadata) {
        return "FAILED".equals(metadata.get("status"))
                && "INVALID_JSON".equals(metadata.get("reason"))
                && payload.get("rawBody") instanceof String;
    }

    private boolean maskMutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return maskMap(map);
        } else if (value instanceof List<?> list) {
            return maskList(list);
        } else if (value instanceof Collection<?> collection) {
            boolean changed = false;
            for (Object element : collection) {
                changed |= maskMutableValue(element);
            }
            return changed;
        } else if (value instanceof Object[] array) {
            boolean changed = false;
            for (Object element : array) {
                changed |= maskMutableValue(element);
            }
            return changed;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean maskMap(Map<?, ?> rawMap) {
        Map<Object, Object> map = (Map<Object, Object>) rawMap;
        boolean changed = false;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getKey() instanceof String key && isSensitive(key)) {
                entry.setValue(MASKED_VALUE);
                changed = true;
            } else {
                changed |= maskMutableValue(entry.getValue());
            }
        }
        return changed;
    }

    @SuppressWarnings("unchecked")
    private boolean maskList(List<?> rawList) {
        List<Object> list = (List<Object>) rawList;
        boolean changed = false;
        for (int index = 0; index < list.size(); index++) {
            changed |= maskMutableValue(list.get(index));
        }
        return changed;
    }

    private boolean isSensitive(String key) {
        return sensitiveKeys.contains(key.toLowerCase(Locale.ROOT));
    }

    private static void putIfNotNull(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value instanceof LocalDateTime ? value.toString() : value);
        }
    }

    private static LogEvent withMessage(LogEvent source, Message message) {
        return new Log4jLogEvent.Builder(source).setMessage(message).build();
    }
}
