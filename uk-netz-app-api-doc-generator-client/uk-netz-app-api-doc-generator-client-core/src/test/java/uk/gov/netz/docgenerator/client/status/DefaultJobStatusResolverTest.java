package uk.gov.netz.docgenerator.client.status;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.model.JobState;
import uk.gov.netz.docgenerator.client.model.JobStatus;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.OutputObject;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.storage.StatusMarkerObject;
import uk.gov.netz.docgenerator.client.support.InMemoryDocumentStorageClient;

class DefaultJobStatusResolverTest {

    @Test
    void returnsCompleteBeforeOtherStatesWhenPdfExists() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMITTED);
        storageClient.putError("job-1", errorDetail("failed"));
        storageClient.putPdf("job-1", new byte[] {1});

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.COMPLETE);
        assertThat(status.getOutputObjectKey()).isEqualTo(DocumentObjectKeys.outputPdf("job-1"));
        assertThat(status.getError()).isNull();
        assertThat(status.getSubmittedAt()).isNotNull();
    }

    @Test
    void returnsFailedBeforeMarkerStatesWhenErrorJsonExists() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        ErrorDetail errorDetail = errorDetail("failed");
        storageClient.writeStatusMarker("job-1", StatusMarker.PROCESSING);
        storageClient.putError("job-1", errorDetail);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.FAILED);
        assertThat(status.getError()).isEqualTo(errorDetail);
    }

    @Test
    void returnsProcessingBeforeQueuedPendingAndSubmissionFailed() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.writeStatusMarker("job-1", StatusMarker.UPLOADED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMISSION_FAILED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMITTED);
        storageClient.writeStatusMarker("job-1", StatusMarker.PROCESSING);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.PROCESSING);
    }

    @Test
    void returnsQueuedBeforePendingAndSubmissionFailed() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.writeStatusMarker("job-1", StatusMarker.UPLOADED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMISSION_FAILED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMITTED);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.QUEUED);
    }

    @Test
    void returnsSubmissionFailedBeforePendingAndIncludesErrorIfPresent() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        ErrorDetail errorDetail = errorDetail("submission failed");
        storageClient.writeStatusMarker("job-1", StatusMarker.UPLOADED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMISSION_FAILED);
        storageClient.putError("job-1", errorDetail);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.FAILED);
        assertThat(status.getError()).isEqualTo(errorDetail);
    }

    @Test
    void returnsSubmissionFailedWhenOnlySubmissionFailureMarkerExists() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.writeStatusMarker("job-1", StatusMarker.UPLOADED);
        storageClient.writeStatusMarker("job-1", StatusMarker.SUBMISSION_FAILED);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.SUBMISSION_FAILED);
    }

    @Test
    void returnsPendingWhenOnlyUploadedMarkerExists() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.writeStatusMarker("job-1", StatusMarker.UPLOADED);

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.PENDING);
    }

    @Test
    void returnsNotFoundWhenNoObjectsOrMarkersExist() {
        JobStatus status = new DefaultJobStatusResolver(new InMemoryDocumentStorageClient()).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.NOT_FOUND);
        assertThat(status.getSubmittedAt()).isNull();
    }

    @Test
    void prefersTimestampedMarkerWhenDuplicateMarkerHasNullLastModified() {
        Instant submittedAt = Instant.parse("2026-05-12T10:15:30Z");
        DocumentStorageClient storageClient = new FixedMarkerStorageClient(
            new StatusMarkerObject(
                StatusMarker.SUBMITTED,
                DocumentObjectKeys.statusMarker("job-1", StatusMarker.SUBMITTED),
                submittedAt
            ),
            new StatusMarkerObject(
                StatusMarker.SUBMITTED,
                DocumentObjectKeys.statusMarker("job-1", StatusMarker.SUBMITTED),
                null
            )
        );

        JobStatus status = new DefaultJobStatusResolver(storageClient).getStatus("job-1");

        assertThat(status.getState()).isEqualTo(JobState.QUEUED);
        assertThat(status.getSubmittedAt()).isEqualTo(submittedAt);
    }

    private static ErrorDetail errorDetail(String reason) {
        return ErrorDetail.builder()
            .jobId("job-1")
            .errorReason(reason)
            .build();
    }

    private record FixedMarkerStorageClient(StatusMarkerObject... markers) implements DocumentStorageClient {

        @Override
        public void uploadDocx(String jobId, byte[] docxBytes) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeStatusMarker(String jobId, StatusMarker marker) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<StatusMarkerObject> listStatusMarkers(String jobId) {
            return List.of(markers);
        }

        @Override
        public List<OutputObject> listOutputObjects(String jobId) {
            return List.of();
        }

        @Override
        public boolean pdfExists(String jobId) {
            return false;
        }

        @Override
        public boolean errorJsonExists(String jobId) {
            return false;
        }

        @Override
        public byte[] downloadPdf(String jobId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ErrorDetail> readErrorDetail(String jobId) {
            return Optional.empty();
        }

        @Override
        public Optional<URI> createDownloadUrl(String jobId, Duration expiry) {
            return Optional.empty();
        }
    }
}
