package uk.gov.netz.docgenerator.client.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.support.InMemoryDocumentStorageClient;
import uk.gov.netz.docgenerator.client.support.RecordingJobQueuePublisher;

class DefaultAsyncJobManagerTest {

    @Test
    void submitUploadsInputMarksStatusPublishesMessageAndReturnsReceipt() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        AsyncJobReceipt receipt = manager.submit(request(new byte[] {1, 2, 3}));

        assertThat(receipt.getJobId()).isEqualTo("job-1");
        assertThat(storageClient.docx("job-1")).containsExactly(1, 2, 3);
        assertThat(queuePublisher.publishedMessages()).singleElement().satisfies(published -> {
            assertThat(published.destination()).isEqualTo("async-docgen");
            assertThat(published.message().getJobId()).isEqualTo("job-1");
            assertThat(published.messageGroupId()).isEqualTo("app-docgen-high");
        });
        assertThat(storageClient.operations()).containsExactly(
            "uploadDocx:job-1",
            "writeStatusMarker:job-1:UPLOADED",
            "writeStatusMarker:job-1:SUBMITTED"
        );
    }

    @Test
    void publishFailureMarksSubmissionFailedAndRethrows() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        IllegalStateException publishException = new IllegalStateException("sqs unavailable");
        queuePublisher.failWith(publishException);
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isSameAs(publishException);
        assertThat(storageClient.operations()).containsExactly(
            "uploadDocx:job-1",
            "writeStatusMarker:job-1:UPLOADED",
            "writeStatusMarker:job-1:SUBMISSION_FAILED"
        );
    }

    @Test
    void submitWithMetadataPublishesMetadataInMessage() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            Map.of("requestId", "request-1"),
            false
        ));

        assertThat(queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.message().getMetadata())
                .containsEntry("requestId", "request-1"));
    }

    @Test
    void submitWithNormalizePublishesNormalizeInMessage() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            null,
            true
        ));

        assertThat(queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.message().isNormalize()).isTrue());
    }

    @Test
    void submitWithMetadataAndNormalizePublishesBothInMessage() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            Map.of("requestId", "request-1"),
            true
        ));

        assertThat(queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> {
                assertThat(published.message().getMetadata()).containsEntry("requestId", "request-1");
                assertThat(published.message().isNormalize()).isTrue();
            });
    }

    @Test
    void submittedMarkerFailureDoesNotFailSubmission() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        storageClient.failMarker(StatusMarker.SUBMITTED);
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        AsyncJobReceipt receipt = manager.submit(request(new byte[] {1}));

        assertThat(receipt.getJobId()).isEqualTo("job-1");
        assertThat(queuePublisher.publishedMessages()).hasSize(1);
    }

    @Test
    void submitWithLowPriorityPublishesLowMessageGroupId() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties(),
            () -> "job-1"
        );

        manager.submit(new DocumentGenerationRequest(new byte[] {1}, DocumentGenerationPriority.LOW));

        assertThat(queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.messageGroupId()).isEqualTo("app-docgen-low"));
    }

    @Test
    void submitRejectsMissingMessageGroupIdBeforeUpload() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        DocGenClientProperties properties = properties();
        properties.setMessageGroupId(null);
        DefaultAsyncJobManager manager = new DefaultAsyncJobManager(
            storageClient,
            queuePublisher,
            properties,
            () -> "job-1"
        );

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("messageGroupId must not be blank");
        assertThat(storageClient.operations()).isEmpty();
    }

    private static DocGenClientProperties properties() {
        DocGenClientProperties properties = new DocGenClientProperties();
        properties.setAsyncQueueDestination("async-docgen");
        properties.setMessageGroupId("app-docgen");
        return properties;
    }

    private static DocumentGenerationRequest request(byte[] docxBytes) {
        return new DocumentGenerationRequest(docxBytes);
    }
}
