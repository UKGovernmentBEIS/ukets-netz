package uk.gov.netz.docgenerator.client.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import uk.gov.netz.docgenerator.client.exception.DocGenDownloadException;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.support.InMemoryDocumentStorageClient;

class DocumentStorageClientContractTest {

    @Test
    void reportsMissingObjectsWithoutErrorDetailsOrDownloadUrls() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();

        assertThat(storageClient.pdfExists("job-1")).isFalse();
        assertThat(storageClient.errorJsonExists("job-1")).isFalse();
        assertThat(storageClient.readErrorDetail("job-1")).isEmpty();
        assertThat(storageClient.createDownloadUrl("job-1", Duration.ofMinutes(1))).isEmpty();
        assertThatThrownBy(() -> storageClient.downloadPdf("job-1"))
            .isInstanceOf(DocGenDownloadException.class)
            .hasMessage("Missing PDF for job job-1");
    }

    @Test
    void readsErrorDetailsWhenErrorObjectExists() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        ErrorDetail errorDetail = ErrorDetail.builder()
            .jobId("job-1")
            .errorReason("template invalid")
            .timestamp("2026-05-12T00:00:00Z")
            .attemptCount(2)
            .build();

        storageClient.putError("job-1", errorDetail);

        assertThat(storageClient.errorJsonExists("job-1")).isTrue();
        assertThat(storageClient.readErrorDetail("job-1")).contains(errorDetail);
    }

    @Test
    void downloadsPdfBytesAndCreatesUrlWhenPresent() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.putPdf("job-1", new byte[] {1, 2, 3});
        storageClient.setDownloadUrl(URI.create("https://example.test/job-1.pdf"));

        assertThat(storageClient.pdfExists("job-1")).isTrue();
        assertThat(storageClient.downloadPdf("job-1")).containsExactly(1, 2, 3);
        assertThat(storageClient.createDownloadUrl("job-1", Duration.ofMinutes(5)))
            .contains(URI.create("https://example.test/job-1.pdf"));
    }
}
