package uk.gov.netz.api.files.common.utils;

public final class FileNameSanitizer {

    private FileNameSanitizer() {
    }

    public static String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw invalidFileName();
        }
        int separatorIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String baseName = fileName.substring(separatorIndex + 1);
        StringBuilder sanitized = new StringBuilder();
        baseName.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .filter(codePoint -> codePoint != '"' && codePoint != '\\' && codePoint != ';')
                .forEach(sanitized::appendCodePoint);
        String result = sanitized.toString().strip();
        if (result.isBlank() || result.codePoints().allMatch(codePoint -> codePoint == '.')) {
            throw invalidFileName();
        }
        return result;
    }

    private static IllegalArgumentException invalidFileName() {
        return new IllegalArgumentException("file name must contain a safe, meaningful value");
    }
}
