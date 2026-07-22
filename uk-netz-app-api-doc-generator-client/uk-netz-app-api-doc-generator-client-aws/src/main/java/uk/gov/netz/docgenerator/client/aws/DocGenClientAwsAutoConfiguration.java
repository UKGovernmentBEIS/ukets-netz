package uk.gov.netz.docgenerator.client.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.sqs.operations.SqsOperations;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import uk.gov.netz.docgenerator.client.DocGenClientAutoConfiguration;
import uk.gov.netz.docgenerator.client.aws.config.DocGenClientAwsProperties;
import uk.gov.netz.docgenerator.client.aws.queue.SqsJobQueuePublisher;
import uk.gov.netz.docgenerator.client.aws.storage.S3DocumentStorageClient;
import uk.gov.netz.docgenerator.client.config.DocGenClientProperties;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;

@AutoConfiguration(
    after = {S3AutoConfiguration.class, SqsAutoConfiguration.class},
    before = DocGenClientAutoConfiguration.class
)
@ConditionalOnClass({S3Operations.class, SqsOperations.class})
@ConditionalOnProperty(prefix = DocGenClientAwsProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({DocGenClientAwsProperties.class, DocGenClientProperties.class})
public class DocGenClientAwsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DocumentStorageClient.class)
    @ConditionalOnBean(S3Operations.class)
    public DocumentStorageClient docGenS3DocumentStorageClient(
        S3Operations s3Operations,
        DocGenClientProperties properties
    ) {
        return new S3DocumentStorageClient(
            s3Operations,
            requiredText(properties.getStorageContainer(), "storageContainer")
        );
    }

    @Bean
    @ConditionalOnMissingBean(JobQueuePublisher.class)
    @ConditionalOnBean({SqsOperations.class, ObjectMapper.class})
    public JobQueuePublisher docGenSqsJobQueuePublisher(SqsOperations sqsOperations, ObjectMapper objectMapper) {
        return new SqsJobQueuePublisher(sqsOperations, objectMapper);
    }

    private static String requiredText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
