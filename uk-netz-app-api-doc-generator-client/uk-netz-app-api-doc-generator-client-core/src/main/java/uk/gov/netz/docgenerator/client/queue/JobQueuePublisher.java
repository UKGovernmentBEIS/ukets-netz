package uk.gov.netz.docgenerator.client.queue;

import uk.gov.netz.docgenerator.client.model.JobMessage;

public interface JobQueuePublisher {

    void publish(String destination, JobMessage message, String messageGroupId);
}
