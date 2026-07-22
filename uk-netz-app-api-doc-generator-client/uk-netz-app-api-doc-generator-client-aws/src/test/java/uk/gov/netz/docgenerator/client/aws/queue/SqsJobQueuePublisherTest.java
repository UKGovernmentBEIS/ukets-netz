package uk.gov.netz.docgenerator.client.aws.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsOperations;
import io.awspring.cloud.sqs.operations.SqsSendOptions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import uk.gov.netz.docgenerator.client.model.JobMessage;

class SqsJobQueuePublisherTest {

    @Test
    void publishSerializesJobMessageAsJsonBody() {
        SqsOperations sqsOperations = mock(SqsOperations.class);
        SqsJobQueuePublisher publisher = new SqsJobQueuePublisher(sqsOperations, new ObjectMapper());

        publisher.publish("docgen-sync", new JobMessage("job-1"), "app-docgen-high");

        CapturedSendOptions captured = captureSendOptions(sqsOperations);
        assertThat(captured.queue()).isEqualTo("docgen-sync");
        assertThat(captured.payload()).isEqualTo("{\"jobId\":\"job-1\"}");
        assertThat(captured.messageGroupId()).isEqualTo("app-docgen-high");
    }

    @Test
    void publishSerializesMetadataWhenPresent() {
        SqsOperations sqsOperations = mock(SqsOperations.class);
        SqsJobQueuePublisher publisher = new SqsJobQueuePublisher(sqsOperations, new ObjectMapper());

        publisher.publish(
            "docgen-sync",
            new JobMessage("job-1", Map.of("requestId", "request-1")),
            "app-docgen-high"
        );

        assertThat(captureSendOptions(sqsOperations).payload())
            .isEqualTo("{\"jobId\":\"job-1\",\"metadata\":{\"requestId\":\"request-1\"}}");
    }

    @Test
    void publishSerializesNormalizeWhenTrue() {
        SqsOperations sqsOperations = mock(SqsOperations.class);
        SqsJobQueuePublisher publisher = new SqsJobQueuePublisher(sqsOperations, new ObjectMapper());

        publisher.publish("docgen-sync", new JobMessage("job-1", true), "app-docgen-high");

        assertThat(captureSendOptions(sqsOperations).payload())
            .isEqualTo("{\"jobId\":\"job-1\",\"normalize\":true}");
    }

    @Test
    void publishSerializesMetadataAndNormalizeWhenPresent() {
        SqsOperations sqsOperations = mock(SqsOperations.class);
        SqsJobQueuePublisher publisher = new SqsJobQueuePublisher(sqsOperations, new ObjectMapper());

        publisher.publish(
            "docgen-sync",
            new JobMessage("job-1", Map.of("requestId", "request-1"), true),
            "app-docgen-low"
        );

        CapturedSendOptions captured = captureSendOptions(sqsOperations);
        assertThat(captured.payload())
            .isEqualTo("{\"jobId\":\"job-1\",\"normalize\":true,\"metadata\":{\"requestId\":\"request-1\"}}");
        assertThat(captured.messageGroupId()).isEqualTo("app-docgen-low");
    }

    @SuppressWarnings("unchecked")
    private static CapturedSendOptions captureSendOptions(SqsOperations sqsOperations) {
        ArgumentCaptor<Consumer<SqsSendOptions<String>>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(sqsOperations).send(captor.capture());
        SqsSendOptions<String> options = mock(SqsSendOptions.class);
        when(options.queue(any())).thenReturn(options);
        when(options.payload(any())).thenReturn(options);
        when(options.messageGroupId(any())).thenReturn(options);

        captor.getValue().accept(options);

        ArgumentCaptor<String> queueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageGroupIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(options).queue(queueCaptor.capture());
        verify(options).payload(payloadCaptor.capture());
        verify(options).messageGroupId(messageGroupIdCaptor.capture());
        return new CapturedSendOptions(
            queueCaptor.getValue(),
            payloadCaptor.getValue(),
            messageGroupIdCaptor.getValue()
        );
    }

    private record CapturedSendOptions(String queue, String payload, String messageGroupId) {
    }
}
