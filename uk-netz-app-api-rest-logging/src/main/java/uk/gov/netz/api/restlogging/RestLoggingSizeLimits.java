package uk.gov.netz.api.restlogging;

final class RestLoggingSizeLimits {

    static final int DEFAULT_MAX_PAYLOAD_BYTES = 1_000_000;
    static final int DEFAULT_MAX_RENDERED_EVENT_BYTES = 1_000_000;
    static final int MAX_HEADER_VALUE_BYTES = 8 * 1024;
    static final int MAX_HEADERS_BYTES = 16 * 1024;
    static final int MAX_URI_BYTES = 8 * 1024;
    static final int MAX_ID_BYTES = 1024;
    static final String TRUNCATED_SUFFIX = "[TRUNCATED]";

    private RestLoggingSizeLimits() {
    }

    static LimitedString limitUtf8(String value, int maxBytes) {
        if (value == null || utf8Length(value) <= maxBytes) {
            return new LimitedString(value, false);
        }

        String suffix = truncationSuffix(maxBytes);
        int suffixBytes = suffix.length();
        int contentBudget = Math.max(0, maxBytes - suffixBytes);
        StringBuilder limited = new StringBuilder(Math.min(value.length(), contentBudget));
        int retainedBytes = 0;
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            int codePointBytes = utf8Bytes(codePoint);
            if (retainedBytes + codePointBytes > contentBudget) {
                break;
            }
            limited.appendCodePoint(codePoint);
            retainedBytes += codePointBytes;
            offset += Character.charCount(codePoint);
        }
        limited.append(suffix);
        return new LimitedString(limited.toString(), true);
    }

    static LimitedString joinAndLimitUtf8(int maxBytes, String... parts) {
        StringBuilder limited = new StringBuilder(Math.min(maxBytes, 1024));
        int retainedBytes = 0;

        for (String part : parts) {
            if (part == null) {
                continue;
            }
            for (int offset = 0; offset < part.length();) {
                int codePoint = part.codePointAt(offset);
                int codePointBytes = utf8Bytes(codePoint);
                if (retainedBytes + codePointBytes > maxBytes) {
                    String suffix = truncationSuffix(maxBytes);
                    int contentBudget = Math.max(0, maxBytes - suffix.length());
                    while (retainedBytes > contentBudget && !limited.isEmpty()) {
                        int lastCodePoint = limited.codePointBefore(limited.length());
                        int lastCodePointChars = Character.charCount(lastCodePoint);
                        limited.delete(limited.length() - lastCodePointChars, limited.length());
                        retainedBytes -= utf8Bytes(lastCodePoint);
                    }
                    limited.append(suffix);
                    return new LimitedString(limited.toString(), true);
                }
                limited.appendCodePoint(codePoint);
                retainedBytes += codePointBytes;
                offset += Character.charCount(codePoint);
            }
        }
        return new LimitedString(limited.toString(), false);
    }

    private static String truncationSuffix(int maxBytes) {
        if (maxBytes <= 0) {
            return "";
        }
        return TRUNCATED_SUFFIX.substring(0, Math.min(maxBytes, TRUNCATED_SUFFIX.length()));
    }

    static int jsonStringBytes(String value) {
        if (value == null) {
            return 4;
        }
        int bytes = 2;
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            if (codePoint == '"' || codePoint == '\\') {
                bytes += 2;
            } else if (codePoint <= 0x1f) {
                bytes += 6;
            } else {
                bytes += utf8Bytes(codePoint);
            }
            offset += Character.charCount(codePoint);
        }
        return bytes;
    }

    private static int utf8Length(String value) {
        int bytes = 0;
        for (int offset = 0; offset < value.length();) {
            int codePoint = value.codePointAt(offset);
            bytes += utf8Bytes(codePoint);
            offset += Character.charCount(codePoint);
        }
        return bytes;
    }

    private static int utf8Bytes(int codePoint) {
        if (codePoint <= 0x7f) {
            return 1;
        }
        if (codePoint <= 0x7ff) {
            return 2;
        }
        if (codePoint <= 0xffff) {
            return 3;
        }
        return 4;
    }

    record LimitedString(String value, boolean truncated) {
    }
}
