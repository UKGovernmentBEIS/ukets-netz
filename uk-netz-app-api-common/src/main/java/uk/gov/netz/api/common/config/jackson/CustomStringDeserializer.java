package uk.gov.netz.api.common.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class CustomStringDeserializer extends StringDeserializer {

    private static final long serialVersionUID = 1L;
    private static final int MAX_LENGTH_ALLOWED = 30000;
    private static final Pattern REGEX_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\\p{Space}]]");

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String str = super.deserialize(p, ctxt).trim();
        if (str.length() > MAX_LENGTH_ALLOWED) {
            throw new InvalidStringLengthException(p, "Invalid string length: " + str.length());
        }
        return ObjectUtils.isEmpty(str) ? null : REGEX_PATTERN.matcher(str).replaceAll("");
    }
    
    @Override
    public String deserializeWithType(JsonParser p, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }
}
