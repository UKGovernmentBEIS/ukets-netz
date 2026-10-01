package uk.gov.netz.api.files.storage;

import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

/**
 * A container and a path prefix within the configured provider.
 */
@Value
public class StorageDestination {

    String container;
    String prefix;

    @Builder
    public StorageDestination(String container, String prefix) {
        if (container == null || container.isBlank()) {
            throw new IllegalArgumentException("storage container must not be blank");
        }
        if (prefix == null) {
            throw new IllegalArgumentException("storage prefix must not be blank");
        }
        List<String> segments = Arrays.stream(prefix.split("/+"))
                .map(StorageDestination::stripPrefixWhitespace)
                .filter(segment -> !segment.isEmpty())
                .toList();
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("storage prefix must not be blank");
        }
        if (segments.stream().anyMatch(segment -> segment.equals(".") || segment.equals(".."))) {
            throw new IllegalArgumentException("storage prefix must not contain dot segments");
        }
        this.container = container;
        this.prefix = String.join("/", segments);
    }

    private static String stripPrefixWhitespace(String segment) {
        int start = 0;
        int end = segment.length();
        while (start < end && isPrefixWhitespace(segment.codePointAt(start))) {
            start += Character.charCount(segment.codePointAt(start));
        }
        while (start < end && isPrefixWhitespace(segment.codePointBefore(end))) {
            end -= Character.charCount(segment.codePointBefore(end));
        }
        return segment.substring(start, end);
    }

    private static boolean isPrefixWhitespace(int codePoint) {
        // Preserve trim() handling of ASCII controls while also recognizing Unicode whitespace.
        return codePoint <= 0x20 || Character.isWhitespace(codePoint);
    }
}
