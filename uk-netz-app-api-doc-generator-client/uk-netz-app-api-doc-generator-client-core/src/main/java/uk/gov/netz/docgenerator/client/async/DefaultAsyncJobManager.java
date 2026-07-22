package uk.gov.netz.docgenerator.client.async;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationPriority;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.queue.SubmissionFailureMarkingJobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;

@Log4j2
public class DefaultAsyncJobManager {

    private final DocumentStorageClient documentStorageClient;
    private final SubmissionFailureMarkingJobQueuePublisher jobQueuePublisher;
    private final String asyncQueueDestination;
    private final String messageGroupId;
    private final Supplier<String> jobIdSupplier;

    public DefaultAsyncJobManager(
        @NonNull DocumentStorageClient documentStorageClient,
        @NonNull JobQueuePublisher jobQueuePublisher,
        @NonNull DocGenClientProperties properties
    ) {
        this(documentStorageClient, jobQueuePublisher, properties, () -> UUID.randomUUID().toString());
    }

    public DefaultAsyncJobManager(
        @NonNull DocumentStorageClient documentStorageClient,
        @NonNull JobQueuePublisher jobQueuePublisher,
        @NonNull DocGenClientProperties properties,
        @NonNull Supplier<String> jobIdSupplier
    ) {
        this.documentStorageClient = documentStorageClient;
        this.jobQueuePublisher = new SubmissionFailureMarkingJobQueuePublisher(
            jobQueuePublisher,
            documentStorageClient
        );
        this.asyncQueueDestination = requiredText(properties.getAsyncQueueDestination(), "asyncQueueDestination");
        this.messageGroupId = properties.getMessageGroupId();
        this.jobIdSupplier = jobIdSupplier;
    }

    public AsyncJobReceipt submit(DocumentGenerationRequest request) {
        ValidatedRequest validatedRequest = validate(request);
        String jobId = jobIdSupplier.get();
        documentStorageClient.uploadDocx(jobId, validatedRequest.docxBytes());
        documentStorageClient.writeStatusMarker(jobId, StatusMarker.UPLOADED);
        publishJob(jobId, validatedRequest);
        writeSubmittedMarker(jobId);
        return new AsyncJobReceipt(jobId);
    }

    private void publishJob(String jobId, ValidatedRequest request) {
        JobMessage message = new JobMessage(jobId, request.metadata(), request.normalize());
        jobQueuePublisher.publish(asyncQueueDestination, message, request.messageGroupId());
    }

    private void writeSubmittedMarker(String jobId) {
        try {
            documentStorageClient.writeStatusMarker(jobId, StatusMarker.SUBMITTED);
        } catch (RuntimeException ex) {
            log.warn("Failed to write submitted marker for document generation job {}", jobId, ex);
        }
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
}
