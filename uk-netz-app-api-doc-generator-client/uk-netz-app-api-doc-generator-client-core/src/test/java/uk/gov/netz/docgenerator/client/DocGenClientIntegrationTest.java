package uk.gov.netz.docgenerator.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import uk.gov.netz.docgenerator.client.model.AsyncJobReceipt;
import uk.gov.netz.docgenerator.client.model.ConversionResult;
import uk.gov.netz.docgenerator.client.model.DocumentGenerationRequest;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.model.JobState;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;
import uk.gov.netz.docgenerator.client.support.InMemoryDocumentStorageClient;
import uk.gov.netz.docgenerator.client.support.RecordingJobQueuePublisher;

class DocGenClientIntegrationTest {

    @Test
    void syncSubmitPublishesValidMessageAndCompletesWhenOutputPdfAppears() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher() {
            @Override
            public void publish(String destination, JobMessage message, String messageGroupId) {
                super.publish(destination, message, messageGroupId);
                storageClient.putPdf(message.getJobId(), new byte[] {2});
            }
        };

        contextRunner(storageClient, queuePublisher).run(context -> {
            DocumentGeneratorClient client = context.getBean(DocumentGeneratorClient.class);

            ConversionResult result = client.submit(new DocumentGenerationRequest(new byte[] {1}));
            String jobId = queuePublisher.publishedMessages().getFirst().message().getJobId();

            assertThat(result).isEqualTo(new ConversionResult.Success(jobId, DocumentObjectKeys.outputPdf(jobId)));
            assertThat(queuePublisher.publishedMessages()).singleElement().satisfies(published -> {
                assertThat(published.destination()).isEqualTo("sync-docgen");
                assertThat(published.message().getJobId()).isEqualTo(jobId);
                assertThat(published.messageGroupId()).isEqualTo("app-docgen-high");
            });
        });
    }

    @Test
    void asyncSubmitUploadsInputAndPublishesValidMessage() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();

        contextRunner(storageClient, queuePublisher).run(context -> {
            DocumentGeneratorClient client = context.getBean(DocumentGeneratorClient.class);

            AsyncJobReceipt receipt = client.submitAsync(new DocumentGenerationRequest(new byte[] {1}));

            assertThat(storageClient.docx(receipt.getJobId())).containsExactly(1);
            assertThat(queuePublisher.publishedMessages()).singleElement().satisfies(published -> {
                assertThat(published.destination()).isEqualTo("async-docgen");
                assertThat(published.message().getJobId()).isEqualTo(receipt.getJobId());
                assertThat(published.messageGroupId()).isEqualTo("app-docgen-high");
            });
        });
    }

    @Test
    void statusResolverReadsMarkersAndOutputsThroughGenericStorage() {
        InMemoryDocumentStorageClient storageClient = new InMemoryDocumentStorageClient();
        RecordingJobQueuePublisher queuePublisher = new RecordingJobQueuePublisher();

        contextRunner(storageClient, queuePublisher).run(context -> {
            DocumentGeneratorClient client = context.getBean(DocumentGeneratorClient.class);
            storageClient.writeStatusMarker("job-1", StatusMarker.SUBMITTED);
            storageClient.putPdf("job-1", new byte[] {1});

            assertThat(client.getStatus("job-1").getState()).isEqualTo(JobState.COMPLETE);
        });
    }

    private static ApplicationContextRunner contextRunner(
        InMemoryDocumentStorageClient storageClient,
        RecordingJobQueuePublisher queuePublisher
    ) {
        return new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DocGenClientAutoConfiguration.class))
            .withBean(InMemoryDocumentStorageClient.class, () -> storageClient)
            .withBean(RecordingJobQueuePublisher.class, () -> queuePublisher)
            .withPropertyValues(
                "docgen.client.sync-queue-destination=sync-docgen",
                "docgen.client.async-queue-destination=async-docgen",
                "docgen.client.message-group-id=app-docgen",
                "docgen.client.sync-timeout=2s",
                "docgen.client.sync-poll-interval=10ms"
            );
    }
}
