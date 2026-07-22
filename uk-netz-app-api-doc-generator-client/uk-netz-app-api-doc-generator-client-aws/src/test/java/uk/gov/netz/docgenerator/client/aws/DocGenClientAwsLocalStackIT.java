package uk.gov.netz.docgenerator.client.aws;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import io.awspring.cloud.autoconfigure.core.AwsProperties;
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.sqs.operations.SqsOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.BucketLocationConstraint;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import uk.gov.netz.docgenerator.client.aws.queue.SqsJobQueuePublisher;
import uk.gov.netz.docgenerator.client.aws.storage.S3DocumentStorageClient;
import uk.gov.netz.docgenerator.client.model.ErrorDetail;
import uk.gov.netz.docgenerator.client.model.JobMessage;
import uk.gov.netz.docgenerator.client.queue.JobQueuePublisher;
import uk.gov.netz.docgenerator.client.storage.DocumentObjectKeys;
import uk.gov.netz.docgenerator.client.storage.DocumentStorageClient;
import uk.gov.netz.docgenerator.client.storage.StatusMarker;

@Testcontainers
@SpringBootTest(
    classes = DocGenClientAwsLocalStackIT.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.cloud.aws.region.static=eu-west-2",
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test",
        "spring.cloud.aws.s3.path-style-access-enabled=true"
    }
)
class DocGenClientAwsLocalStackIT {

    private static final String BUCKET = "doc-gen";
    private static final String SYNC_QUEUE = "doc-gen-sync-jobs.fifo";
    private static final String ASYNC_QUEUE = "doc-gen-async-jobs.fifo";
    private static final String ASYNC_DLQ = "doc-gen-async-jobs-dlq.fifo";

    @Container
    private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:3")
    ).withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS)
        .withEnv("DEFAULT_REGION", "eu-west-2")
        .withEnv("AWS_DEFAULT_REGION", "eu-west-2");

    @Autowired
    private DocumentStorageClient documentStorageClient;
    @Autowired
    private JobQueuePublisher jobQueuePublisher;
    @Autowired
    private S3Operations s3Operations;
    @Autowired
    private SqsOperations sqsOperations;
    @Autowired
    private S3Client s3Client;
    private SqsClient sqsClient;
    @Autowired
    private AwsProperties awsProperties;
    @Autowired
    private S3Properties s3Properties;
    @Autowired
    private AwsRegionProvider awsRegionProvider;
    @Autowired
    private AwsCredentialsProvider awsCredentialsProvider;

    @DynamicPropertySource
    static void localStackProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.endpoint", () -> LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3));
    }

    @BeforeEach
    void createLocalStackResources() {
        sqsClient = SqsClient.builder()
            .endpointOverride(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.SQS))
            .region(Region.EU_WEST_2)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .build();
        createBucketIfNecessary();
        sqsClient.createQueue(builder -> builder.queueName(ASYNC_DLQ)
            .attributes(Map.of(
                QueueAttributeName.FIFO_QUEUE, "true",
                QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true",
                QueueAttributeName.MESSAGE_RETENTION_PERIOD, "1209600"
            )));
        String dlqUrl = queueUrl(ASYNC_DLQ);
        String dlqArn = sqsClient.getQueueAttributes(builder -> builder.queueUrl(dlqUrl)
                .attributeNames(QueueAttributeName.QUEUE_ARN))
            .attributes()
            .get(QueueAttributeName.QUEUE_ARN);
        sqsClient.createQueue(builder -> builder.queueName(SYNC_QUEUE)
            .attributes(Map.of(
                QueueAttributeName.FIFO_QUEUE, "true",
                QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true",
                QueueAttributeName.VISIBILITY_TIMEOUT, "30",
                QueueAttributeName.MESSAGE_RETENTION_PERIOD, "300"
            )));
        sqsClient.createQueue(builder -> builder.queueName(ASYNC_QUEUE)
            .attributes(Map.of(
                QueueAttributeName.FIFO_QUEUE, "true",
                QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true",
                QueueAttributeName.VISIBILITY_TIMEOUT, "1200",
                QueueAttributeName.MESSAGE_RETENTION_PERIOD, "345600",
                QueueAttributeName.REDRIVE_POLICY,
                "{\"deadLetterTargetArn\":\"" + dlqArn + "\",\"maxReceiveCount\":\"3\"}"
            )));
    }

    @AfterEach
    void closeSqsClient() {
        sqsClient.close();
    }

    @Test
    void autoConfigurationUsesSpringCloudAwsLocalStackPropertiesAndAdapters() {
        assertThat(documentStorageClient).isInstanceOf(S3DocumentStorageClient.class);
        assertThat(jobQueuePublisher).isInstanceOf(SqsJobQueuePublisher.class);
        assertThat(s3Operations).isNotNull();
        assertThat(sqsOperations).isNotNull();
        assertThat(awsProperties.getEndpoint()).isEqualTo(URI.create(LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3).toString()));
        assertThat(s3Properties.getPathStyleAccessEnabled()).isTrue();
        assertThat(awsRegionProvider.getRegion()).isEqualTo(Region.EU_WEST_2);
        assertThat(awsCredentialsProvider.resolveCredentials().accessKeyId()).isEqualTo("test");
        assertThat(awsCredentialsProvider.resolveCredentials().secretAccessKey()).isEqualTo("test");
    }

    @Test
    void s3AdapterReadsAndWritesObjectsWithRealS3Operations() {
        String jobId = "job-" + UUID.randomUUID();
        byte[] pdfBytes = new byte[] {4, 5, 6};
        ErrorDetail errorDetail = ErrorDetail.builder()
            .jobId(jobId)
            .errorReason("template failed")
            .build();

        documentStorageClient.uploadDocx(jobId, new byte[] {1, 2, 3});
        documentStorageClient.writeStatusMarker(jobId, StatusMarker.UPLOADED);
        s3Operations.upload(
            BUCKET,
            DocumentObjectKeys.outputPdf(jobId),
            new ByteArrayInputStream(pdfBytes),
            ObjectMetadata.builder().contentLength(3L).contentType("application/pdf").build()
        );
        s3Operations.store(BUCKET, DocumentObjectKeys.errorJson(jobId), errorDetail);

        ResponseBytes<GetObjectResponse> inputObject = s3Client.getObjectAsBytes(builder -> builder
            .bucket(BUCKET)
            .key(DocumentObjectKeys.inputDocx(jobId)));
        assertThat(inputObject.asByteArray()).containsExactly(1, 2, 3);
        assertThat(documentStorageClient.listStatusMarkers(jobId))
            .singleElement()
            .satisfies(marker -> {
                assertThat(marker.getMarker()).isEqualTo(StatusMarker.UPLOADED);
                assertThat(marker.getObjectKey()).isEqualTo(DocumentObjectKeys.statusMarker(jobId, StatusMarker.UPLOADED));
            });
        assertThat(documentStorageClient.listOutputObjects(jobId))
            .extracting(output -> output.getObjectKey())
            .contains(DocumentObjectKeys.outputPdf(jobId), DocumentObjectKeys.errorJson(jobId));
        assertThat(documentStorageClient.downloadPdf(jobId)).containsExactly(pdfBytes);
        assertThat(documentStorageClient.readErrorDetail(jobId)).contains(errorDetail);
    }

    @Test
    void sqsAdapterPublishesJobMessageJsonToLocalStackQueue() {
        JobMessage message = new JobMessage("job-1");

        jobQueuePublisher.publish(SYNC_QUEUE, message, "app-docgen-high");

        ReceiveMessageResponse response = sqsClient.receiveMessage(builder -> builder
            .queueUrl(queueUrl(SYNC_QUEUE))
            .maxNumberOfMessages(1)
            .waitTimeSeconds(5));
        assertThat(response.messages())
            .singleElement()
            .extracting(Message::body)
            .isEqualTo("{\"jobId\":\"job-1\"}");
    }

    private void createBucketIfNecessary() {
        try {
            s3Client.headBucket(builder -> builder.bucket(BUCKET));
        } catch (NoSuchBucketException ex) {
            createBucket();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                createBucket();
                return;
            }
            throw ex;
        }
    }

    private void createBucket() {
        try {
            s3Client.createBucket(builder -> builder
                .bucket(BUCKET)
                .createBucketConfiguration(configuration -> configuration.locationConstraint(BucketLocationConstraint.EU_WEST_2)));
        } catch (BucketAlreadyOwnedByYouException ignored) {
            // Bucket creation is idempotent for this shared LocalStack fixture.
        }
    }

    private String queueUrl(String queueName) {
        return sqsClient.getQueueUrl(builder -> builder.queueName(queueName)).queueUrl();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
