package uk.gov.netz.docgenerator.client.support;

import java.util.ArrayList;
import java.util.List;

import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;

public class RecordingJobQueuePublisher implements JobQueuePublisher {

    private final List<PublishedMessage> publishedMessages = new ArrayList<>();
    private RuntimeException publishException;

    @Override
    public void publish(String destination, JobMessage message, String messageGroupId) {
        publishedMessages.add(new PublishedMessage(destination, message, messageGroupId));
        if (publishException != null) {
            throw publishException;
        }
    }

    public List<PublishedMessage> publishedMessages() {
        return List.copyOf(publishedMessages);
    }

    public void failWith(RuntimeException publishException) {
        this.publishException = publishException;
    }

    public record PublishedMessage(String destination, JobMessage message, String messageGroupId) {
    }
}
