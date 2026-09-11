package uk.gov.netz.api.restlogging;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RestLoggingPropertiesTest {

    @Test
    void payloadLimitDefaultsToOneMillionBytes() {
        RestLoggingProperties properties = new RestLoggingProperties();

        assertThat(properties.getMaxPayloadBytes()).isEqualTo(1_000_000);
    }

    @Test
    void payloadLimitCanDisableCapture() {
        RestLoggingProperties properties = new RestLoggingProperties();

        properties.setMaxPayloadSize(DataSize.ofBytes(0));

        assertThat(properties.getMaxPayloadBytes()).isZero();
    }

    @Test
    void payloadLimitAboveDefaultIsAccepted() {
        RestLoggingProperties properties = new RestLoggingProperties();

        properties.setMaxPayloadSize(DataSize.ofBytes(2_000_000));

        assertThat(properties.getMaxPayloadBytes()).isEqualTo(2_000_000);
    }

    @Test
    void payloadLimitAboveSupportedIntegerRangeIsRejected() {
        RestLoggingProperties properties = new RestLoggingProperties();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> properties.setMaxPayloadSize(DataSize.ofBytes((long) Integer.MAX_VALUE + 1)))
                .withMessageContaining("between 0B and " + Integer.MAX_VALUE + "B");
    }

    @Test
    void negativePayloadLimitIsRejected() {
        RestLoggingProperties properties = new RestLoggingProperties();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> properties.setMaxPayloadSize(DataSize.ofBytes(-1)))
                .withMessageContaining("between 0B and " + Integer.MAX_VALUE + "B");
    }

    @Test
    void uriPatternsAreCompiledWhenConfigured() {
        RestLoggingProperties properties = new RestLoggingProperties();
        properties.setExcludedUriPatterns(List.of("^/actuator(?:/.*)?$"));
        properties.setExcludedTotallyUriPatterns(List.of("^/downloads/"));

        assertThat(properties.isExcluded("/actuator/health")).isTrue();
        assertThat(properties.isTotallyExcluded("/downloads/report")).isTrue();
        assertThat(properties.isExcluded("/api/test")).isFalse();
    }

    @Test
    void payloadLimitBindsFromApplicationProperties() {
        RestLoggingProperties properties = new RestLoggingProperties();
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "rest.logging.max-payload-size", "750KB")));

        binder.bind("rest.logging", Bindable.ofInstance(properties));

        assertThat(properties.getMaxPayloadBytes()).isEqualTo(750 * 1024);
    }

    @Test
    void payloadLimitBindsFromEnvironmentVariable() {
        RestLoggingProperties properties = new RestLoggingProperties();
        SystemEnvironmentPropertySource environment = new SystemEnvironmentPropertySource(
                "test", Map.of("REST_LOGGING_MAX_PAYLOAD_SIZE", "1250000B"));
        Binder binder = new Binder(ConfigurationPropertySources.from(environment));

        binder.bind("rest.logging", Bindable.ofInstance(properties));

        assertThat(properties.getMaxPayloadBytes()).isEqualTo(1_250_000);
    }
}
