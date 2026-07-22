package uk.gov.netz.docgenerator.client.queue;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;

@RequiredArgsConstructor
public class SubmissionFailureMarkingJobQueuePublisher {

    private final JobQueuePublisher delegate;
    private final DocumentStorageClient documentStorageClient;

    public void publish(String destination, JobMessage message, String messageGroupId) {
        try {
            delegate.publish(destination, message, messageGroupId);
        } catch (RuntimeException ex) {
            markSubmissionFailed(message, ex);
            throw ex;
        }
    }

    private void markSubmissionFailed(JobMessage message, RuntimeException publishException) {
        try {
            documentStorageClient.writeStatusMarker(message.getJobId(), StatusMarker.SUBMISSION_FAILED);
        } catch (RuntimeException markerException) {
            publishException.addSuppressed(markerException);
        }
    }
}
