package uk.gov.netz.api.restlogging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

final class ServletCharsetResolver {

    private ServletCharsetResolver() {
    }

    static Charset resolve(String encoding) throws UnsupportedEncodingException {
        if (encoding == null) {
            return StandardCharsets.ISO_8859_1;
        }
        try {
            return Charset.forName(encoding);
        } catch (IllegalArgumentException ex) {
            UnsupportedEncodingException unsupported = new UnsupportedEncodingException(encoding);
            unsupported.initCause(ex);
            throw unsupported;
        }
    }
}
