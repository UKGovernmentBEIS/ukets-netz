package uk.gov.netz.docgenerator.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import uk.gov.netz.docgenerator.client.async.DefaultAsyncJobManager;
import uk.gov.netz.docgenerator.client.download.DefaultDocumentDownloadClient;
import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.ConversionResult;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.JobState;
import uk.gov.netz.docgenerator.client.model.JobStatus;
import uk.gov.netz.docgenerator.client.status.DefaultJobStatusResolver;
import uk.gov.netz.docgenerator.client.sync.DefaultSyncJobManager;

class DocumentGeneratorClientTest {

    private final DefaultSyncJobManager syncJobManager = mock(DefaultSyncJobManager.class);
    private final DefaultAsyncJobManager asyncJobManager = mock(DefaultAsyncJobManager.class);
    private final DefaultJobStatusResolver jobStatusResolver = mock(DefaultJobStatusResolver.class);
    private final DefaultDocumentDownloadClient documentDownloadClient = mock(DefaultDocumentDownloadClient.class);
    private final DocumentGeneratorClient client = new DocumentGeneratorClient(
        syncJobManager,
        asyncJobManager,
        jobStatusResolver,
        documentDownloadClient
    );

    @Test
    void submitDelegatesToSyncSubmission() {
        DocumentGenerationRequest request = new DocumentGenerationRequest(new byte[] {1});
        ConversionResult result = new ConversionResult.Success("job-1", "output/job-1.pdf");
        when(syncJobManager.submit(same(request))).thenReturn(result);

        assertThat(client.submit(request)).isSameAs(result);
    }

    @Test
    void submitAsyncDelegatesToAsyncSubmission() {
        DocumentGenerationRequest request = new DocumentGenerationRequest(new byte[] {1});
        AsyncJobReceipt receipt = new AsyncJobReceipt("job-1");
        when(asyncJobManager.submit(same(request))).thenReturn(receipt);

        assertThat(client.submitAsync(request)).isSameAs(receipt);
    }

    @Test
    void getStatusDelegatesToStatusResolver() {
        JobStatus status = new JobStatus("job-1", JobState.COMPLETE, "output/job-1.pdf", null, null);
        when(jobStatusResolver.getStatus("job-1")).thenReturn(status);

        assertThat(client.getStatus("job-1")).isSameAs(status);
    }

    @Test
    void downloadBytesDelegatesToDownloadClient() {
        byte[] pdfBytes = new byte[] {2};
        when(documentDownloadClient.downloadBytes("job-1")).thenReturn(pdfBytes);

        assertThat(client.downloadBytes("job-1")).isSameAs(pdfBytes);
    }

    @Test
    void createDownloadUrlDelegatesToDownloadClient() {
        Duration expiry = Duration.ofMinutes(10);
        URI uri = URI.create("https://example.test/document.pdf");
        when(documentDownloadClient.createDownloadUrl("job-1", expiry)).thenReturn(uri);

        assertThat(client.createDownloadUrl("job-1", expiry)).isSameAs(uri);
        verify(documentDownloadClient).createDownloadUrl("job-1", expiry);
    }
}
