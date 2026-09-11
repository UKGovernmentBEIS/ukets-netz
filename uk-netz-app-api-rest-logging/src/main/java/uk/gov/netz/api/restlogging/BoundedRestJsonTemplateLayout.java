package uk.gov.netz.api.restlogging;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.LocationAware;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;
import org.apache.logging.log4j.message.ObjectMessage;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON Template Layout decorator that keeps REST exchange entries within a configured byte limit.
 */
@Plugin(name = "BoundedRestJsonTemplateLayout", category = Node.CATEGORY,
        elementType = Layout.ELEMENT_TYPE, printObject = true)
public final class BoundedRestJsonTemplateLayout implements StringLayout, LocationAware {

    private static final String EVENT_SIZE_REASON = "LOG_EVENT_SIZE_LIMIT";
    private static final byte[] MINIMAL_FALLBACK =
            "{\"message\":\"[REST LOG OMITTED: EVENT SIZE LIMIT]\"}\n".getBytes(StandardCharsets.UTF_8);

    private final JsonTemplateLayout delegate;
    private final int maxEventBytes;

    private BoundedRestJsonTemplateLayout(JsonTemplateLayout delegate, int maxEventBytes) {
        this.delegate = delegate;
        this.maxEventBytes = maxEventBytes;
    }

    @PluginFactory
    public static BoundedRestJsonTemplateLayout create(
            @PluginConfiguration Configuration configuration,
            @PluginAttribute(value = "eventTemplateUri",
                    defaultString = "classpath:ApplicationLogJsonLayout.json") String eventTemplateUri,
            @PluginAttribute(value = "stackTraceEnabled", defaultBoolean = true) boolean stackTraceEnabled,
            @PluginAttribute(value = "maxStringLength", defaultInt = 32768) int maxStringLength,
            @PluginAttribute(value = "maxEventBytes",
                    defaultInt = RestLoggingSizeLimits.DEFAULT_MAX_RENDERED_EVENT_BYTES) int maxEventBytes) {
        if (maxEventBytes < MINIMAL_FALLBACK.length) {
            throw new IllegalArgumentException("maxEventBytes must be at least " + MINIMAL_FALLBACK.length);
        }
        JsonTemplateLayout layout = JsonTemplateLayout.newBuilder()
                .setConfiguration(configuration)
                .setCharset(StandardCharsets.UTF_8)
                .setEventTemplateUri(eventTemplateUri)
                .setStackTraceEnabled(stackTraceEnabled)
                .setMaxStringLength(maxStringLength)
                .build();
        return new BoundedRestJsonTemplateLayout(layout, maxEventBytes);
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        byte[] rendered = delegate.toByteArray(event);
        if (rendered.length <= maxEventBytes || !isRestEntry(event)) {
            return rendered;
        }

        LogEvent compactEvent = compact(event, rendered.length);
        byte[] compact = delegate.toByteArray(compactEvent);
        return compact.length <= maxEventBytes ? compact : MINIMAL_FALLBACK.clone();
    }

    @Override
    public String toSerializable(LogEvent event) {
        return new String(toByteArray(event), StandardCharsets.UTF_8);
    }

    @Override
    public void encode(LogEvent event, ByteBufferDestination destination) {
        byte[] rendered = toByteArray(event);
        destination.writeBytes(rendered, 0, rendered.length);
    }

    @Override
    public byte[] getFooter() {
        return delegate.getFooter();
    }

    @Override
    public byte[] getHeader() {
        return delegate.getHeader();
    }

    @Override
    public Charset getCharset() {
        return delegate.getCharset();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public Map<String, String> getContentFormat() {
        return delegate.getContentFormat();
    }

    @Override
    public boolean requiresLocation() {
        return delegate.requiresLocation();
    }

    int getMaxEventBytes() {
        return maxEventBytes;
    }

    private static boolean isRestEntry(LogEvent event) {
        if (!(event.getMessage() instanceof ObjectMessage objectMessage)
                || !(objectMessage.getParameter() instanceof Map<?, ?> message)) {
            return false;
        }
        String type = String.valueOf(message.get("type"));
        return message.containsKey("payloadCapture")
                && ("REQUEST".equals(type) || "RESPONSE".equals(type));
    }

    private LogEvent compact(LogEvent source, int originalRenderedSizeBytes) {
        Map<?, ?> original = (Map<?, ?>) ((ObjectMessage) source.getMessage()).getParameter();
        Map<String, Object> compact = new LinkedHashMap<>();
        copy(compact, original, "type");
        copyLimited(compact, original, "correlationId", RestLoggingSizeLimits.MAX_ID_BYTES);
        copyLimited(compact, original, "correlationParentId", RestLoggingSizeLimits.MAX_ID_BYTES);
        compact.put("headers", Map.of());
        compact.put("headersCapture", compactHeadersCapture(
                original.get("headers"), original.get("headersCapture")));
        compact.put("payload", Map.of());
        copy(compact, original, "payloadCapture");
        copyLimited(compact, original, "uri", RestLoggingSizeLimits.MAX_URI_BYTES);
        copyLimited(compact, original, "userId", RestLoggingSizeLimits.MAX_ID_BYTES);
        copy(compact, original, "httpMethod");
        copy(compact, original, "httpStatus");
        copy(compact, original, "timestamp");
        copy(compact, original, "responseTimeInMillis");
        compact.put("logEventCapture", Map.of(
                "status", "TRUNCATED",
                "reason", EVENT_SIZE_REASON,
                "originalSizeBytes", originalRenderedSizeBytes,
                "limitBytes", maxEventBytes));
        return new Log4jLogEvent.Builder(source).setMessage(new ObjectMessage(compact)).build();
    }

    private static Map<String, Object> compactHeadersCapture(Object originalHeaders, Object originalCapture) {
        int omittedCount = originalHeaders instanceof Map<?, ?> headers ? headers.size() : 0;
        if (originalCapture instanceof Map<?, ?> capture
                && capture.get("omittedCount") instanceof Number previousOmittedCount) {
            omittedCount += previousOmittedCount.intValue();
        }
        return Map.of(
                "status", "TRUNCATED",
                "reason", EVENT_SIZE_REASON,
                "limitBytes", 0,
                "retainedCount", 0,
                "omittedCount", omittedCount);
    }

    private static void copy(Map<String, Object> target, Map<?, ?> source, String key) {
        Object value = source.get(key);
        if (value != null) {
            target.put(key, value);
        }
    }

    private static void copyLimited(Map<String, Object> target, Map<?, ?> source, String key, int maxBytes) {
        Object value = source.get(key);
        if (value != null) {
            target.put(key, RestLoggingSizeLimits.limitUtf8(String.valueOf(value), maxBytes).value());
        }
    }
}
