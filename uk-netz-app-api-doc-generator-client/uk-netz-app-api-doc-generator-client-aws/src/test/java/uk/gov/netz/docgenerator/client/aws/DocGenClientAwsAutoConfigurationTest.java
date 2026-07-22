package uk.gov.netz.docgenerator.client.aws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.sqs.operations.SqsOperations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import uk.gov.netz.docgenerator.client.DocumentGeneratorClient;
import uk.gov.netz.docgenerator.client.DocGenClientAutoConfiguration;
import uk.gov.netz.docgenerator.client.aws.queue.SqsJobQueuePublisher;
import uk.gov.netz.docgenerator.client.aws.storage.S3DocumentStorageClient;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;

class DocGenClientAwsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DocGenClientAwsAutoConfiguration.class))
        .withBean(S3Operations.class, () -> mock(S3Operations.class))
        .withBean(SqsOperations.class, () -> mock(SqsOperations.class))
        .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void awsAdaptersAreCreatedFromSpringCloudAwsOperations() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(DocumentStorageClient.class);
            assertThat(context).hasSingleBean(JobQueuePublisher.class);
            assertThat(context).getBean(DocumentStorageClient.class).isInstanceOf(S3DocumentStorageClient.class);
            assertThat(context).getBean(JobQueuePublisher.class).isInstanceOf(SqsJobQueuePublisher.class);
        });
    }

    @Test
    void awsStarterCreatesCoreClientWhenCombinedWithCoreAutoConfiguration() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                DocGenClientAutoConfiguration.class,
                DocGenClientAwsAutoConfiguration.class
            ))
            .withBean(S3Operations.class, () -> mock(S3Operations.class))
            .withBean(SqsOperations.class, () -> mock(SqsOperations.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .run(context -> {
                assertThat(context).hasSingleBean(DocumentStorageClient.class);
                assertThat(context).hasSingleBean(JobQueuePublisher.class);
                assertThat(context).hasSingleBean(DocumentGeneratorClient.class);
            });
    }

    @Test
    void awsAdaptersAreNotCreatedWhenDisabled() {
        contextRunner
            .withPropertyValues("docgen.client.aws.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(DocumentStorageClient.class);
                assertThat(context).doesNotHaveBean(JobQueuePublisher.class);
            });
    }

    @Test
    void genericAdapterBeansCanBeOverriddenIndividually() {
        DocumentStorageClient customStorageClient = mock(DocumentStorageClient.class);
        JobQueuePublisher customJobQueuePublisher = mock(JobQueuePublisher.class);

        contextRunner
            .withBean(DocumentStorageClient.class, () -> customStorageClient)
            .withBean(JobQueuePublisher.class, () -> customJobQueuePublisher)
            .run(context -> {
                assertThat(context).hasSingleBean(DocumentStorageClient.class);
                assertThat(context).hasSingleBean(JobQueuePublisher.class);
                assertThat(context).getBean(DocumentStorageClient.class).isSameAs(customStorageClient);
                assertThat(context).getBean(JobQueuePublisher.class).isSameAs(customJobQueuePublisher);
            });
    }
}
