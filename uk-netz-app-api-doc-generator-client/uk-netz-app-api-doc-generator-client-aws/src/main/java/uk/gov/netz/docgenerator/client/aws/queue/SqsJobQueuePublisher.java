package uk.gov.netz.docgenerator.client.aws.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsOperations;
import lombok.RequiredArgsConstructor;
import uk.gov.netz.docgenerator.client.exception.DocGenSubmitException;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;

@RequiredArgsConstructor
public class SqsJobQueuePublisher implements JobQueuePublisher {

    private final SqsOperations sqsOperations;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String destination, JobMessage message, String messageGroupId) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            sqsOperations.send(options -> options
                .queue(destination)
                .payload(payload)
                .messageGroupId(messageGroupId));
        } catch (JsonProcessingException ex) {
            throw new DocGenSubmitException("Failed to serialize document generation job " + message.getJobId(), ex);
        } catch (RuntimeException ex) {
            throw new DocGenSubmitException("Failed to publish document generation job " + message.getJobId() + " to " + destination, ex);
        }
    }
}
