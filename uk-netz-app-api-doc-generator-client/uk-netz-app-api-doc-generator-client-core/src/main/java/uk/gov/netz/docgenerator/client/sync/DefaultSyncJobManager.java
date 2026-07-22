package uk.gov.netz.docgenerator.client.sync;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.exception.DocGenSubmitException;
import uk.gov.netz.docgenerator.client.model.ConversionResult;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.queue.SubmissionFailureMarkingJobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;

@Log4j2
public class DefaultSyncJobManager {

    private final DocumentStorageClient documentStorageClient;
    private final SubmissionFailureMarkingJobQueuePublisher jobQueuePublisher;
    private final String syncQueueDestination;
    private final String messageGroupId;
    private final Duration syncTimeout;
    private final Duration syncPollInterval;
    private final Clock clock;
    private final Supplier<String> jobIdSupplier;
    private final Sleeper sleeper;

    public DefaultSyncJobManager(
        @NonNull DocumentStorageClient documentStorageClient,
        @NonNull JobQueuePublisher jobQueuePublisher,
        @NonNull DocGenClientProperties properties
    ) {
        this(
            documentStorageClient,
            jobQueuePublisher,
            properties,
            Clock.systemUTC(),
            () -> UUID.randomUUID().toString(),
            Thread::sleep
        );
    }

    DefaultSyncJobManager(
        @NonNull DocumentStorageClient documentStorageClient,
        @NonNull JobQueuePublisher jobQueuePublisher,
        @NonNull DocGenClientProperties properties,
        @NonNull Clock clock,
        @NonNull Supplier<String> jobIdSupplier,
        @NonNull Sleeper sleeper
    ) {
        this.documentStorageClient = documentStorageClient;
        this.jobQueuePublisher = new SubmissionFailureMarkingJobQueuePublisher(
            jobQueuePublisher,
            documentStorageClient
        );
        this.syncQueueDestination = requiredText(properties.getSyncQueueDestination(), "syncQueueDestination");
        this.messageGroupId = properties.getMessageGroupId();
        this.syncTimeout = Objects.requireNonNull(properties.getSyncTimeout(), "syncTimeout");
        this.syncPollInterval = Objects.requireNonNull(properties.getSyncPollInterval(), "syncPollInterval");
        this.clock = clock;
        this.jobIdSupplier = jobIdSupplier;
        this.sleeper = sleeper;
    }

    public ConversionResult submit(DocumentGenerationRequest request) {
        ValidatedRequest validatedRequest = validate(request);
        String jobId = jobIdSupplier.get();
        documentStorageClient.uploadDocx(jobId, validatedRequest.docxBytes());
        documentStorageClient.writeStatusMarker(jobId, StatusMarker.UPLOADED);

        publishJob(jobId, validatedRequest);
        writeSubmittedMarker(jobId);
        return waitForResult(jobId);
    }

    private void publishJob(String jobId, ValidatedRequest request) {
        JobMessage message = new JobMessage(jobId, request.metadata(), request.normalize());
        jobQueuePublisher.publish(syncQueueDestination, message, request.messageGroupId());
    }

    private void writeSubmittedMarker(String jobId) {
        try {
            documentStorageClient.writeStatusMarker(jobId, StatusMarker.SUBMITTED);
        } catch (RuntimeException ex) {
            log.warn("Failed to write submitted marker for document generation job {}", jobId, ex);
        }
    }

    private ConversionResult waitForResult(String jobId) {
        Instant deadline = Instant.now(clock).plus(syncTimeout);
        while (true) {
            if (documentStorageClient.pdfExists(jobId)) {
                return new ConversionResult.Success(jobId, DocumentObjectKeys.outputPdf(jobId));
            }

            if (documentStorageClient.errorJsonExists(jobId)) {
                return new ConversionResult.Failed(jobId, errorReason(jobId));
            }

            Instant now = Instant.now(clock);
            if (!now.isBefore(deadline)) {
                return new ConversionResult.Timeout(jobId);
            }

            try {
                sleeper.sleep(delayMillis(shorterOf(syncPollInterval, Duration.between(now, deadline))));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new DocGenSubmitException("Interrupted while waiting for document generation job " + jobId, ex);
            }
        }
    }

    private String errorReason(String jobId) {
        return documentStorageClient.readErrorDetail(jobId)
            .map(ErrorDetail::getErrorReason)
            .filter(reason -> !reason.isBlank())
            .orElse("Document generation failed");
    }

    private static Duration shorterOf(Duration first, Duration second) {
        return first.compareTo(second) <= 0 ? first : second;
    }

    private static long delayMillis(Duration duration) {
        return Math.max(1L, duration.toMillis());
    }

    private static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private ValidatedRequest validate(DocumentGenerationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getDocxBytes() == null) {
            throw new IllegalArgumentException("docxBytes must not be null");
        }
        DocumentGenerationPriority priority = request.getPriority() == null
            ? DocumentGenerationPriority.HIGH
            : request.getPriority();
        return new ValidatedRequest(
            request.getDocxBytes(),
            request.getMetadata(),
            request.isNormalize(),
            messageGroupId(priority)
        );
    }

    private String messageGroupId(DocumentGenerationPriority priority) {
        String baseMessageGroupId = requiredText(messageGroupId, "messageGroupId");
        return switch (priority) {
            case HIGH -> baseMessageGroupId + "-high";
            case LOW -> baseMessageGroupId + "-low";
        };
    }

    private static final class ValidatedRequest {

        private final byte[] docxBytes;
        private final Map<String, String> metadata;
        private final boolean normalize;
        private final String messageGroupId;

        private ValidatedRequest(
            byte[] docxBytes,
            Map<String, String> metadata,
            boolean normalize,
            String messageGroupId
        ) {
            this.docxBytes = docxBytes;
            this.metadata = metadata;
            this.normalize = normalize;
            this.messageGroupId = messageGroupId;
        }

        private byte[] docxBytes() {
            return docxBytes;
        }

        private Map<String, String> metadata() {
            return metadata;
        }

        private boolean normalize() {
            return normalize;
        }

        private String messageGroupId() {
            return messageGroupId;
        }
    }

    @FunctionalInterface
    interface Sleeper {

        void sleep(long millis) throws InterruptedException;
    }
}
