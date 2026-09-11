package uk.gov.netz.api.restlogging;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RestLoggingSizeLimitsTest {

    @Test
    void limitUtf8DoesNotSplitUnicodeCodePoint() {
        String value = "😀".repeat(100);

        RestLoggingSizeLimits.LimitedString limited = RestLoggingSizeLimits.limitUtf8(value, 64);

        assertThat(limited.truncated()).isTrue();
        assertThat(limited.value()).endsWith(RestLoggingSizeLimits.TRUNCATED_SUFFIX);
        assertThat(limited.value().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
        assertThat(limited.value()).doesNotContain("�");
    }

    @Test
    void joinAndLimitUtf8BoundsPartsWithoutSplittingUnicodeCodePoint() {
        RestLoggingSizeLimits.LimitedString limited = RestLoggingSizeLimits.joinAndLimitUtf8(
                64, "/api/test", "?", "requestId=", "😀".repeat(100));

        assertThat(limited.truncated()).isTrue();
        assertThat(limited.value()).startsWith("/api/test?requestId=");
        assertThat(limited.value()).endsWith(RestLoggingSizeLimits.TRUNCATED_SUFFIX);
        assertThat(limited.value().getBytes(StandardCharsets.UTF_8).length).isLessThanOrEqualTo(64);
        assertThat(limited.value()).doesNotContain("�");
    }

    @Test
    void joinAndLimitUtf8ReturnsAllPartsWhenWithinLimit() {
        RestLoggingSizeLimits.LimitedString limited = RestLoggingSizeLimits.joinAndLimitUtf8(
                64, "/api/test", "?", "requestId=123&mode=test");

        assertThat(limited.truncated()).isFalse();
        assertThat(limited.value()).isEqualTo("/api/test?requestId=123&mode=test");
    }

    @Test
    void truncationHelpersHonorLimitsNoLargerThanTheSuffix() {
        String oversized = "x".repeat(100);
        int[] limits = {0, 1, RestLoggingSizeLimits.TRUNCATED_SUFFIX.length() - 1,
                RestLoggingSizeLimits.TRUNCATED_SUFFIX.length()};

        for (int limit : limits) {
            String expected = RestLoggingSizeLimits.TRUNCATED_SUFFIX.substring(0, limit);
            RestLoggingSizeLimits.LimitedString limited = RestLoggingSizeLimits.limitUtf8(oversized, limit);
            RestLoggingSizeLimits.LimitedString joined =
                    RestLoggingSizeLimits.joinAndLimitUtf8(limit, oversized);

            assertThat(limited.truncated()).isTrue();
            assertThat(limited.value()).isEqualTo(expected);
            assertThat(limited.value().getBytes(StandardCharsets.UTF_8)).hasSizeLessThanOrEqualTo(limit);
            assertThat(joined.truncated()).isTrue();
            assertThat(joined.value()).isEqualTo(expected);
            assertThat(joined.value().getBytes(StandardCharsets.UTF_8)).hasSizeLessThanOrEqualTo(limit);
        }
    }

    @Test
    void jsonStringSizeAccountsForEscapingAndUtf8() {
        String value = "quote=\" slash=\\ control=\n emoji=😀";

        int estimatedBytes = RestLoggingSizeLimits.jsonStringBytes(value);
        int actualBytes = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(value).toString()
                .getBytes(StandardCharsets.UTF_8).length;

        assertThat(estimatedBytes).isGreaterThanOrEqualTo(actualBytes);
    }
}
