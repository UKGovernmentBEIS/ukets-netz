package uk.gov.netz.docgenerator.client.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.Test;

import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.exception.DocGenSubmitException;
import uk.gov.netz.docgenerator.client.model.ConversionResult;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.support.InMemoryDocumentStorageClient;
import uk.gov.netz.docgenerator.client.support.RecordingJobQueuePublisher;

class DefaultSyncJobManagerTest {

    @Test
    void submitUploadsInputPublishesMessageAndReturnsSuccessWhenPdfAppears() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {4, 5}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        ConversionResult result = manager.submit(request(new byte[] {1, 2, 3}));

        assertThat(result).isEqualTo(new ConversionResult.Success("job-1", "output/job-1.pdf"));
        assertThat(testContext.storageClient.docx("job-1")).containsExactly(1, 2, 3);
        assertThat(testContext.queuePublisher.publishedMessages()).singleElement().satisfies(published -> {
            assertThat(published.destination()).isEqualTo("sync-docgen");
            assertThat(published.message().getJobId()).isEqualTo("job-1");
            assertThat(published.messageGroupId()).isEqualTo("app-docgen-high");
        });
        assertThat(testContext.storageClient.operations()).contains(
            "uploadDocx:job-1",
            "writeStatusMarker:job-1:UPLOADED",
            "writeStatusMarker:job-1:SUBMITTED"
        );
    }

    @Test
    void publishFailureMarksSubmissionFailedAndThrows() {
        TestContext testContext = new TestContext();
        RuntimeException publishException = new IllegalStateException("sqs unavailable");
        testContext.queuePublisher.failWith(publishException);
        DefaultSyncJobManager manager = testContext.manager("job-1");

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isSameAs(publishException);
        assertThat(testContext.storageClient.operations()).containsExactly(
            "uploadDocx:job-1",
            "writeStatusMarker:job-1:UPLOADED",
            "writeStatusMarker:job-1:SUBMISSION_FAILED"
        );
    }

    @Test
    void submittedMarkerFailureDoesNotFailSubmission() {
        TestContext testContext = new TestContext();
        testContext.storageClient.failMarker(StatusMarker.SUBMITTED);
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        ConversionResult result = manager.submit(request(new byte[] {1}));

        assertThat(result).isEqualTo(new ConversionResult.Success("job-1", "output/job-1.pdf"));
        assertThat(testContext.queuePublisher.publishedMessages()).hasSize(1);
    }

    @Test
    void submitWithMetadataPublishesMetadataInMessage() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            Map.of("requestId", "request-1"),
            false
        ));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.message().getMetadata())
                .containsEntry("requestId", "request-1"));
    }

    @Test
    void submitWithNormalizePublishesNormalizeInMessage() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            null,
            true
        ));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.message().isNormalize()).isTrue());
    }

    @Test
    void submitWithMetadataAndNormalizePublishesBothInMessage() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(
            new byte[] {1},
            DocumentGenerationPriority.HIGH,
            Map.of("requestId", "request-1"),
            true
        ));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> {
                assertThat(published.message().getMetadata()).containsEntry("requestId", "request-1");
                assertThat(published.message().isNormalize()).isTrue();
            });
    }

    @Test
    void submitReturnsFailureWhenErrorJsonAppears() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putError("job-1", ErrorDetail.builder()
            .jobId("job-1")
            .errorReason("template invalid")
            .build()));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        ConversionResult result = manager.submit(request(new byte[] {1}));

        assertThat(result).isEqualTo(new ConversionResult.Failed("job-1", "template invalid"));
    }

    @Test
    void submitReturnsFailureWithDefaultReasonWhenErrorReasonIsBlank() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putError("job-1", ErrorDetail.builder()
            .jobId("job-1")
            .errorReason(" ")
            .build()));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        ConversionResult result = manager.submit(request(new byte[] {1}));

        assertThat(result).isEqualTo(new ConversionResult.Failed("job-1", "Document generation failed"));
    }

    @Test
    void submitReturnsTimeoutAfterSyncTimeout() {
        TestContext testContext = new TestContext();
        DefaultSyncJobManager manager = testContext.manager("job-1");

        ConversionResult result = manager.submit(request(new byte[] {1}));

        assertThat(result).isEqualTo(new ConversionResult.Timeout("job-1"));
        assertThat(testContext.sleeper.sleeps()).isEqualTo(10);
    }

    @Test
    void submitThrowsWhenInterruptedWhileWaiting() {
        TestContext testContext = new TestContext();
        testContext.sleeper.interruptOnNextSleep();
        DefaultSyncJobManager manager = testContext.manager("job-1");

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isInstanceOf(DocGenSubmitException.class)
            .hasMessage("Interrupted while waiting for document generation job job-1");
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    @Test
    void submitWithLowPriorityPublishesLowMessageGroupId() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(new byte[] {1}, DocumentGenerationPriority.LOW));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.messageGroupId()).isEqualTo("app-docgen-low"));
    }

    @Test
    void submitWithExplicitHighPriorityPublishesHighMessageGroupId() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(new byte[] {1}, DocumentGenerationPriority.HIGH));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.messageGroupId()).isEqualTo("app-docgen-high"));
    }

    @Test
    void submitWithNullPriorityDefaultsToHighMessageGroupId() {
        TestContext testContext = new TestContext();
        testContext.sleeper.afterSleeps(1, () -> testContext.storageClient.putPdf("job-1", new byte[] {2}));
        DefaultSyncJobManager manager = testContext.manager("job-1");

        manager.submit(new DocumentGenerationRequest(new byte[] {1}, null));

        assertThat(testContext.queuePublisher.publishedMessages())
            .singleElement()
            .satisfies(published -> assertThat(published.messageGroupId()).isEqualTo("app-docgen-high"));
    }

    @Test
    void submitRejectsMissingRequestBeforeUpload() {
        TestContext testContext = new TestContext();
        DefaultSyncJobManager manager = testContext.manager("job-1");

        assertThatThrownBy(() -> manager.submit(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("request must not be null");
        assertThat(testContext.storageClient.operations()).isEmpty();
    }

    @Test
    void submitRejectsMissingDocxBytesBeforeUpload() {
        TestContext testContext = new TestContext();
        DefaultSyncJobManager manager = testContext.manager("job-1");

        assertThatThrownBy(() -> manager.submit(new DocumentGenerationRequest()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("docxBytes must not be null");
        assertThat(testContext.storageClient.operations()).isEmpty();
    }

    @Test
    void submitRejectsMissingMessageGroupIdBeforeUpload() {
        TestContext testContext = new TestContext();
        DocGenClientProperties properties = properties();
        properties.setMessageGroupId(null);
        DefaultSyncJobManager manager = testContext.manager("job-1", properties);

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("messageGroupId must not be blank");
        assertThat(testContext.storageClient.operations()).isEmpty();
    }

    @Test
    void submitRejectsBlankMessageGroupIdBeforeUpload() {
        TestContext testContext = new TestContext();
        DocGenClientProperties properties = properties();
        properties.setMessageGroupId(" ");
        DefaultSyncJobManager manager = testContext.manager("job-1", properties);

        assertThatThrownBy(() -> manager.submit(request(new byte[] {1})))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("messageGroupId must not be blank");
        assertThat(testContext.storageClient.operations()).isEmpty();
    }

    private static DocGenClientProperties properties() {
        DocGenClientProperties properties = new DocGenClientProperties();
        properties.setSyncQueueDestination("sync-docgen");
        properties.setMessageGroupId("app-docgen");
        properties.setSyncTimeout(Duration.ofMillis(100));
        properties.setSyncPollInterval(Duration.ofMillis(10));
        return properties;
    }

    private static DocumentGenerationRequest request(byte[] docxBytes) {
        return new DocumentGenerationRequest(docxBytes);
    }

    private static final class TestContext {

        private final InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        private final RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();
        private final MutableClock clock = new MutableClock(Instant.parse("2026-05-12T00:00:00Z"));
        private final AdvancingSleeper sleeper = new AdvancingSleeper(clock);

        private DefaultSyncJobManager manager(String jobId) {
            return manager(jobId, properties());
        }

        private DefaultSyncJobManager manager(String jobId, DocGenClientProperties properties) {
            return new DefaultSyncJobManager(
                storageClient,
                queuePublisher,
                properties,
                clock,
                () -> jobId,
                sleeper
            );
        }
    }

    private static final class AdvancingSleeper implements DefaultSyncJobManager.Sleeper {

        private final MutableClock clock;
        private int sleeps;
        private int runAfterSleeps = -1;
        private Runnable afterSleep = () -> {
        };
        private boolean interruptOnNextSleep;

        private AdvancingSleeper(MutableClock clock) {
            this.clock = clock;
        }

        @Override
        public void sleep(long millis) throws InterruptedException {
            if (interruptOnNextSleep) {
                throw new InterruptedException("interrupted");
            }
            sleeps++;
            clock.advance(Duration.ofMillis(millis));
            if (sleeps == runAfterSleeps) {
                afterSleep.run();
            }
        }

        private void afterSleeps(int sleeps, Runnable action) {
            this.runAfterSleeps = sleeps;
            this.afterSleep = action;
        }

        private void interruptOnNextSleep() {
            this.interruptOnNextSleep = true;
        }

        private int sleeps() {
            return sleeps;
        }
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
