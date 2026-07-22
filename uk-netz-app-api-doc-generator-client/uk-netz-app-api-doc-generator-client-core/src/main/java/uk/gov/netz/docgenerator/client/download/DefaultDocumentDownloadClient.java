package uk.gov.netz.docgenerator.client.download;

import java.net.URI;
import java.time.Duration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.exception.DocGenDownloadException;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;

@RequiredArgsConstructor
public class DefaultDocumentDownloadClient {

    private final DocumentStorageClient documentStorageClient;
    private final DocGenClientProperties properties;

    public byte[] downloadBytes(@NonNull String jobId) {
        return documentStorageClient.downloadPdf(jobId);
    }

    public URI createDownloadUrl(@NonNull String jobId, Duration expiry) {
        Duration resolvedExpiry = expiry == null ? properties.getDownloadUrlExpiry() : expiry;
        return documentStorageClient.createDownloadUrl(jobId, resolvedExpiry)
            .orElseThrow(() -> new DocGenDownloadException("Signed download URLs are not supported by the configured document storage provider"));
    }
}
