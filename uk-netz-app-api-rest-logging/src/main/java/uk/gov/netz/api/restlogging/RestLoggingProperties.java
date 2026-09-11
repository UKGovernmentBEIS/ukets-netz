package uk.gov.netz.api.restlogging;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.logging.log4j.spi.StandardLevel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class representing application properties with prefix rest.logging.
 */
@ConfigurationProperties(prefix = "rest.logging")
@Getter
public class RestLoggingProperties {

    private static final DataSize DEFAULT_PAYLOAD_SIZE =
            DataSize.ofBytes(RestLoggingSizeLimits.DEFAULT_MAX_PAYLOAD_BYTES);
    private static final DataSize MAX_SUPPORTED_SIZE = DataSize.ofBytes(Integer.MAX_VALUE);

    /* List of uri patterns to be excluded from logging in successful actions (error cases are always logged). */
    private List<String> excludedUriPatterns = Collections.emptyList();

    /* List of urls that excluded at all during logging (neither successful nor error cases are logged) */
    private List<String> excludedTotallyUriPatterns = Collections.emptyList();

    private StandardLevel level = StandardLevel.INFO;

    private DataSize maxPayloadSize = DEFAULT_PAYLOAD_SIZE;

    @Getter(AccessLevel.NONE)
    private List<Pattern> excludedUriPatternsCompiled = Collections.emptyList();
    @Getter(AccessLevel.NONE)
    private List<Pattern> excludedTotallyUriPatternsCompiled = Collections.emptyList();

    public void setExcludedUriPatterns(List<String> excludedUriPatterns) {
        this.excludedUriPatterns = excludedUriPatterns != null ? List.copyOf(excludedUriPatterns) : Collections.emptyList();
        this.excludedUriPatternsCompiled = compilePatterns(this.excludedUriPatterns);
    }

    public void setExcludedTotallyUriPatterns(List<String> excludedTotallyUriPatterns) {
        this.excludedTotallyUriPatterns = excludedTotallyUriPatterns != null
                ? List.copyOf(excludedTotallyUriPatterns)
                : Collections.emptyList();
        this.excludedTotallyUriPatternsCompiled = compilePatterns(this.excludedTotallyUriPatterns);
    }

    public void setLevel(StandardLevel level) {
        this.level = level != null ? level : StandardLevel.INFO;
    }

    public void setMaxPayloadSize(DataSize maxPayloadSize) {
        this.maxPayloadSize = validatePayloadSize(maxPayloadSize);
    }

    boolean isExcluded(String uri) {
        return matches(uri, excludedUriPatternsCompiled);
    }

    boolean isTotallyExcluded(String uri) {
        return matches(uri, excludedTotallyUriPatternsCompiled);
    }

    int getMaxPayloadBytes() {
        return Math.toIntExact(maxPayloadSize.toBytes());
    }

    private static List<Pattern> compilePatterns(List<String> expressions) {
        return expressions.stream().map(Pattern::compile).toList();
    }

    private static boolean matches(String uri, List<Pattern> patterns) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(uri).find());
    }

    private static DataSize validatePayloadSize(DataSize value) {
        if (value == null || value.isNegative() || value.compareTo(MAX_SUPPORTED_SIZE) > 0) {
            throw new IllegalArgumentException(
                    "rest.logging.max-payload-size must be between 0B and " + Integer.MAX_VALUE + "B");
        }
        return value;
    }
}
