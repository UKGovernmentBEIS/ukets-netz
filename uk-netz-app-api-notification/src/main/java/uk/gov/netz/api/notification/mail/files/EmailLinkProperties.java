package uk.gov.netz.api.notification.mail.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "notification.email-link")
public class EmailLinkProperties {

    private static final String DEFAULT_CONTAINER = "uk-ets-files";
    private static final String DEFAULT_PUBLIC_BASE_URL = "http://uk-ets-files.s3.localhost.localstack.cloud:4566";

    private boolean enabled;
    private String container = DEFAULT_CONTAINER;
    private String prefix;
    private URI publicBaseUrl = URI.create(DEFAULT_PUBLIC_BASE_URL);

    // Bind URL text directly because Spring's URI property editor re-encodes existing percent escapes.
    @ConstructorBinding
    EmailLinkProperties(@DefaultValue("false") boolean enabled,
                        @DefaultValue(DEFAULT_CONTAINER) String container,
                        String prefix,
                        @DefaultValue(DEFAULT_PUBLIC_BASE_URL) String publicBaseUrl) {
        this.enabled = enabled;
        this.container = container;
        this.prefix = prefix;
        this.publicBaseUrl = URI.create(publicBaseUrl.trim());
    }

    void validatePublicBaseUrl() {
        if (publicBaseUrl == null || !publicBaseUrl.isAbsolute() || publicBaseUrl.getHost() == null
                || !("http".equalsIgnoreCase(publicBaseUrl.getScheme()) || "https".equalsIgnoreCase(publicBaseUrl.getScheme()))
                || publicBaseUrl.getUserInfo() != null || publicBaseUrl.getQuery() != null || publicBaseUrl.getFragment() != null) {
            throw new IllegalArgumentException(
                    "notification.email-link.public-base-url must be an absolute HTTP(S) URI without user info, query, or fragment");
        }
    }
}
