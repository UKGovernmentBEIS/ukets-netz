package uk.gov.netz.api.restlogging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(name = "MaskRewritePolicy", category = Core.CATEGORY_NAME, elementType = "rewritePolicy", printObject = true)
public class MaskRewritePolicy implements RewritePolicy {
	
	private static final Logger noMaskLogger = LogManager.getLogger("NoMaskLogger");
	
	
    private final ObjectMapper objectMapper;
    
    private final List<String> regularExpressionsForObjectMessages;
    private final List<String> regularExpressionsForSimpleMessages;

    private static final String MASKING_CHARACTER = "*";
    private static final String PAYLOAD_PROPERTY_PATTERN_PREFIX = "\"";
    private static final String PAYLOAD_PROPERTY_PATTERN_SUFFIX_JSON = "\"\\s*:\\s*\"([^\"]*)\"";
    private static final String PAYLOAD_PROPERTY_PATTERN_SUFFIX_JAVA = "\\s*=\\s*([^,\\)]*)(,|\\))";

    private MaskRewritePolicy(final List<Property> properties) {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.regularExpressionsForObjectMessages = new ArrayList<>();
        this.regularExpressionsForSimpleMessages = new ArrayList<>();
        
        properties.forEach(property -> {
            String propertyValue = property.getValue();
            //Form the same regex pattern for all payload properties, with only one capturing group
            if ("payloadProperty".equals(property.getName()) && !propertyValue.isBlank()) {
            	regularExpressionsForObjectMessages.add(
						PAYLOAD_PROPERTY_PATTERN_PREFIX + propertyValue.trim() + PAYLOAD_PROPERTY_PATTERN_SUFFIX_JSON);
            	
            	regularExpressionsForSimpleMessages.add(
						PAYLOAD_PROPERTY_PATTERN_PREFIX + propertyValue.trim() + PAYLOAD_PROPERTY_PATTERN_SUFFIX_JSON);
            	regularExpressionsForSimpleMessages.add(propertyValue.trim() + PAYLOAD_PROPERTY_PATTERN_SUFFIX_JAVA);
            }
        });
    }

    @PluginFactory
    public static MaskRewritePolicy create(@PluginElement("Properties") final Property[] properties) {
        if (properties == null || properties.length == 0) {
            return null;
        }
        return new MaskRewritePolicy(Arrays.asList(properties));
    }

    @Override
    public LogEvent rewrite(LogEvent source) {
        final Message msg = source.getMessage();

        if (msg instanceof ObjectMessage objectMessage) {
            String msgString;
            try {
                msgString = objectMapper.writeValueAsString(objectMessage.getParameter());
            } catch (JsonProcessingException e) {
				noMaskLogger.warn("Failed to serialize ObjectMessage to json: {} . Message: {}",
						objectMessage.getParameter(), e.getOriginalMessage());
                return rewriteAsSimpleMessage(source, msg);
            }
			final String maskedMessage = maskLogMessage(msgString, this.regularExpressionsForObjectMessages);
            try {
				return new Log4jLogEvent.Builder(source).setMessage(new ObjectMessage(
						objectMapper.readValue(maskedMessage, new TypeReference<Map<String, Object>>() {
						}))).build();
            } catch (JsonProcessingException e) {
            	noMaskLogger.warn("Failed to deserialize ObjectMessage from json: {} . Message: {}",
            			maskedMessage, e.getOriginalMessage());
            	return rewriteAsSimpleMessage(source, msg);
            }
        } else {
        	return rewriteAsSimpleMessage(source, msg);
        }
    }
    
    private LogEvent rewriteAsSimpleMessage(LogEvent source, Message msg) {
    	final SimpleMessage maskedMsg = new SimpleMessage(
				maskLogMessage(msg.getFormattedMessage(), this.regularExpressionsForSimpleMessages));
        return new Log4jLogEvent.Builder(source).setMessage(maskedMsg).build();
    }

    private String maskLogMessage(String message, final List<String> regularExpressions) {
        StringBuilder sb = new StringBuilder(message);

        regularExpressions.forEach(regex -> {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sb);

            while (matcher.find()) {
                sb.replace(matcher.start(1), matcher.end(1), maskString(matcher.group(1)));
            }
        });

        return sb.toString();
    }

    private String maskString(String value) {
        StringBuilder maskedValue = new StringBuilder(value);
        int maskedValueLength = maskedValue.length();
        return maskedValue.replace(0, maskedValueLength, StringUtils.repeat(MASKING_CHARACTER, maskedValueLength)).toString();
    }
}