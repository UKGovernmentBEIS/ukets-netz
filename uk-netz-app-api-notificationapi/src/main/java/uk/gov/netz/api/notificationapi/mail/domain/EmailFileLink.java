package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.Builder;
import lombok.Value;

import java.net.URI;
import java.util.UUID;

/**
 * Describes an anonymously downloadable file linked from an email.
 *
 * <p>The UUID and URL identify the anonymous download. String representations include these values.</p>
 */
@Value
@Builder
public class EmailFileLink {

    /**
     * Generated bearer identifier.
     */
    UUID uuid;

    /**
     * Safe logical filename used for display and download.
     */
    String fileName;

    /**
     * Complete anonymous public download URL.
     */
    URI url;
}
