package uk.gov.netz.api.notificationapi.mail.service;

import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;

import java.util.List;
import java.util.Map;

/**
 * Storage contract for files that are rendered as download links in emails.
 *
 * <p>The API defines no expiry, revocation, authorization, or automatic attachment-size threshold.</p>
 */
@FunctionalInterface
public interface EmailLinkedFileStorageService {

    /**
     * Stores all supplied files as one bulk operation.
     *
     * <p>An empty input must return an empty list. A successful call must return exactly one link per input entry,
     * ordered by {@link EmailFileLink#getFileName() file name}. If storage fails, the provider must throw a runtime
     * exception and is responsible for best-effort cleanup of files already stored by that call.</p>
     *
     * @param files complete file contents, keyed by logical filename
     * @return one link per file, ordered by filename
     */
    List<EmailFileLink> storeFiles(Map<String, byte[]> files);
}
