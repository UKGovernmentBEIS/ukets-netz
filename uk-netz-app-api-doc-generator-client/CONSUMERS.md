# Integrating the Document Generator Client

This guide shows how a Spring Boot backend application can integrate the document generator client library for new document-generation flows.

The examples use concrete Spring service snippets. They are not migration instructions for an existing REST-based document-generator client.

## Choose A Dependency

Use the AWS adapter when the backend uses Spring Cloud AWS S3 and SQS:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-doc-generator-client-aws</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

This brings in the core client and auto-configures:

| Bean | Backing implementation |
| --- | --- |
| `DocumentStorageClient` | S3 through Spring Cloud AWS `S3Operations` |
| `JobQueuePublisher` | SQS through Spring Cloud AWS `SqsOperations` |
| `DocumentGeneratorClient` | core client facade |

It also brings `software.amazon.msk:aws-msk-iam-auth` onto the application classpath so AWS-backed backends can use Amazon MSK IAM authentication for the optional Kafka result consumer without declaring that dependency separately.

Consuming backend applications should use one of this repository's concrete provider adapter artifacts. Do not implement provider-specific `DocumentStorageClient` or `JobQueuePublisher` beans in backend application code. Additional provider adapters will be added to this repository as needed.

## Configure The Backend

The client defaults to the standard document-generator storage and queue names used by `../uk-netz-env-development` and cloud infrastructure:

```properties
docgen.client.storage-container=doc-gen
docgen.client.sync-queue-destination=doc-gen-sync-jobs
docgen.client.async-queue-destination=doc-gen-async-jobs
docgen.client.message-group-id=my-backend-docgen
docgen.client.sync-timeout=30s
docgen.client.sync-poll-interval=500ms
docgen.client.download-url-expiry=15m
docgen.client.max-handler-retries=3
kafka.docgen-consumer.topic=doc.converted
```

Only override the storage and queue values when a backend's infrastructure uses different names or timings. `docgen.client.message-group-id` is mandatory and should be a stable base identifier for the consuming application. The client appends `-high` or `-low` to it for the SQS `MessageGroupId`.

Example `application.properties` for an AWS-backed cloud application:

```properties
kafka.docgen-consumer.enabled=${DOCGEN_KAFKA_CONSUMER_ENABLED:true}
kafka.docgen-consumer.group=${DOCGEN_KAFKA_RESULT_GROUP:my-backend-docgen-results}

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}
spring.kafka.security.protocol=${KAFKA_SECURITY_PROTOCOL:SASL_SSL}
spring.kafka.properties.sasl.mechanism=${KAFKA_SASL_MECHANISM:AWS_MSK_IAM}
spring.kafka.properties.sasl.jaas.config=${KAFKA_JAAS_SASL_CONFIG:software.amazon.msk.auth.iam.IAMLoginModule required;}
spring.kafka.properties.sasl.client.callback.handler.class=${KAFKA_SASL_CLIENT_CALLBACK_HANDLER:software.amazon.msk.auth.iam.IAMClientCallbackHandler}

spring.cloud.aws.region.static=${AWS_REGION:eu-west-2}
spring.cloud.aws.s3.path-style-access-enabled=${AWS_S3_PATH_STYLE_ACCESS_ENABLED:false}
```

In cloud environments, do not hard-code `spring.cloud.aws.credentials.access-key` or `spring.cloud.aws.credentials.secret-key` in application configuration. Spring Cloud AWS falls back to the AWS SDK `DefaultCredentialsProvider` when static credential properties are not set; that provider reads standard injected credentials such as `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`.

For local development with `../uk-netz-env-development`, LocalStack and Kafka use local-only settings:

```properties
kafka.docgen-consumer.enabled=${DOCGEN_KAFKA_CONSUMER_ENABLED:true}
kafka.docgen-consumer.group=${DOCGEN_KAFKA_RESULT_GROUP:my-backend-docgen-results}

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.security.protocol=${KAFKA_SECURITY_PROTOCOL:PLAINTEXT}

spring.cloud.aws.endpoint=${SPRING_CLOUD_AWS_ENDPOINT:http://localhost:4566}
spring.cloud.aws.region.static=${SPRING_CLOUD_AWS_REGION_STATIC:eu-west-2}
spring.cloud.aws.credentials.access-key=${SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY:test}
spring.cloud.aws.credentials.secret-key=${SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY:test}
spring.cloud.aws.s3.path-style-access-enabled=${SPRING_CLOUD_AWS_S3_PATH_STYLE_ACCESS_ENABLED:true}
```

The `spring.cloud.aws.*` properties configure Spring Cloud AWS clients for S3 and SQS. Kafka bootstrap servers, protocol, SASL mechanism, JAAS config, and callback handler remain standard `spring.kafka.*` settings. For MSK IAM, the AWS adapter provides the auth library, but the backend still needs the appropriate Kafka security properties for its environment. The client's queue destinations are queue names by default; Spring Cloud AWS resolves them for SQS.

If the backend does not need async result handling, leave `kafka.docgen-consumer.enabled=false` or omit it. Sync submission and status resolution do not require the result consumer.

## Submit Documents

The application creates the DOCX bytes. This library uploads those bytes, publishes the conversion job, and reads the generated PDF result.

### Synchronous Conversion

Use sync conversion when the request can wait for the PDF result up to `docgen.client.sync-timeout`.
The default timeout is 30 seconds and remains configurable.

```java
@Service
@RequiredArgsConstructor
class OfficialNoticeDocumentService {

    private final DocumentGeneratorClient docGenClient;

    byte[] generatePdf(byte[] docxBytes) {
        DocumentGenerationRequest request = new DocumentGenerationRequest(docxBytes);
        ConversionResult result = docGenClient.submit(request);

        if (result instanceof ConversionResult.Success success) {
            return docGenClient.downloadBytes(success.getJobId());
        }
        if (result instanceof ConversionResult.Failed failed) {
            throw new IllegalStateException("Document generation failed: " + failed.getErrorReason());
        }
        if (result instanceof ConversionResult.Timeout timeout) {
            throw new IllegalStateException("Document generation timed out for job " + timeout.getJobId());
        }

        throw new IllegalStateException("Unsupported document generation result " + result.getClass().getName());
    }
}
```

Use `new DocumentGenerationRequest(docxBytes, DocumentGenerationPriority.LOW)` for low-priority work. Priority defaults to `HIGH` when omitted or set to `null`.
Set `request.setMetadata(metadata)` when the document generator worker should receive business metadata and copy it to the result event.
Set `request.setNormalize(true)` when the worker should normalize DOCX content before conversion. Normalization is off by default.

Sync submission returns:

| Result | Meaning |
| --- | --- |
| `ConversionResult.Success` | `output/{jobId}.pdf` exists. |
| `ConversionResult.Failed` | `output/{jobId}.error.json` exists and contains an error reason, or a default failure reason is used. |
| `ConversionResult.Timeout` | No terminal output object appeared before `sync-timeout`. |

Infrastructure failures such as storage upload, queue publish, storage inspection failures, or thread interruption throw runtime exceptions. Consuming backends should map these exceptions to a soft user-facing error that asks the user to retry the operation.

### Asynchronous Conversion

Use async conversion when the backend should persist a job ID and continue later.

```java
@Service
@RequiredArgsConstructor
class AsyncOfficialNoticeDocumentService {

    private final DocumentGeneratorClient docGenClient;
    private final DocumentJobRepository documentJobRepository;

    String submitPdfJob(String requestId, byte[] docxBytes) {
        DocumentGenerationRequest request = new DocumentGenerationRequest(docxBytes);
        request.setMetadata(Map.of("requestId", requestId));
        AsyncJobReceipt receipt = docGenClient.submitAsync(request);

        documentJobRepository.save(requestId, receipt.getJobId());
        return receipt.getJobId();
    }
}
```

The backend is responsible for storing the returned `jobId` and mapping it to the relevant domain record.
`submitAsync` means the document generator worker runs asynchronously; the method itself returns an `AsyncJobReceipt` immediately and does not return a Java `CompletableFuture`.
Set `request.setPriority(DocumentGenerationPriority.LOW)` for low-priority work. Set `request.setNormalize(true)` when the worker should normalize DOCX content before conversion. Normalization is off by default.
Metadata is optional business context for the consuming backend. It is serialized on the queue message and should be copied by the worker to the Kafka result event.

## Track Results

### Poll Job Status

The status resolver reads storage markers and output objects.
Status is resolved by precedence, so terminal output objects win over lifecycle markers and a PDF wins over an error object.

```java
@Service
@RequiredArgsConstructor
class DocumentJobStatusService {

    private final DocumentGeneratorClient docGenClient;

    JobStatus getStatus(String jobId) {
        return docGenClient.getStatus(jobId);
    }
}
```

Possible states:

| State | Meaning |
| --- | --- |
| `COMPLETE` | The generated PDF exists. |
| `FAILED` | An error JSON object exists. |
| `PROCESSING` | The worker has written the `processing` marker. |
| `QUEUED` | The job has been submitted to the queue. |
| `SUBMISSION_FAILED` | Queue publication failed and a failure marker was written, with no terminal output present. |
| `PENDING` | The DOCX was uploaded, with no submitted, submission-failed, or terminal output signal present. |
| `NOT_FOUND` | No known markers or outputs exist for the job. |

### Handle Kafka Result Events

Enable `kafka.docgen-consumer.enabled=true` and register one or more `ConversionResultHandler` beans to process async result events.
Handlers are invoked in Spring order. Every handler is invoked on each delivery even if an earlier handler fails, so handlers must be idempotent.

```java
@Component
@Order(100)
@RequiredArgsConstructor
class PersistGeneratedDocumentHandler implements ConversionResultHandler {

    private final DocumentGeneratorClient docGenClient;
    private final DocumentJobRepository documentJobRepository;
    private final FileDocumentService fileDocumentService;

    @Override
    public void handle(ConversionEvent event) {
        if (!"COMPLETE".equals(event.getStatus())) {
            documentJobRepository.markFailed(event.getJobId(), event.getErrorReason());
            return;
        }

        String requestId = event.getMetadata().get("requestId");
        byte[] pdfBytes = docGenClient.downloadBytes(event.getJobId());
        fileDocumentService.createFileDocument(pdfBytes, "generated-document.pdf");
        documentJobRepository.markComplete(requestId, event.getOutputS3Key());
    }
}
```

Kafka offsets are committed only after every handler succeeds. Individual handler failures are logged at `WARN` and retried by leaving the record unacknowledged. If a handler keeps failing for the same topic, partition, offset, and job ID, the event is logged at `ERROR` as a poison event and skipped when it reaches `docgen.client.max-handler-retries`.

A conversion event has this shape:

```json
{
  "jobId": "job-id",
  "status": "COMPLETE",
  "outputS3Key": "output/job-id.pdf",
  "errorReason": null,
  "durationMs": 1250,
  "metadata": {
    "requestId": "request-123"
  }
}
```

The application should treat `outputS3Key` as the generated PDF storage object key. `metadata` is omitted when empty and deserializes to an empty map in `ConversionEvent`.

## Download PDFs

Download bytes when the backend needs to persist the generated PDF in its own file store:

```java
byte[] pdfBytes = docGenClient.downloadBytes(jobId);
```

Create a signed URL when the configured adapter supports it:

```java
URI url = docGenClient.createDownloadUrl(jobId, Duration.ofMinutes(10));
```

If the configured adapter does not support signed URLs, `createDownloadUrl` throws `DocGenDownloadException`.

## Runtime Contract

The document generator worker and backend must share the same storage container and queue/topic configuration.

The library writes and reads these keys:

| Object | Key |
| --- | --- |
| Input DOCX | `input/{jobId}.docx` |
| Status marker | `status/{jobId}/{marker}` |
| Output PDF | `output/{jobId}.pdf` |
| Error detail | `output/{jobId}.error.json` |

The library publishes this queue message:

```json
{
  "jobId": "job-id",
  "normalize": true,
  "metadata": {
    "requestId": "request-123"
  }
}
```

The document generator worker derives the input and output object keys from `jobId`. `normalize` is optional, defaults to false, and is omitted when false. `metadata` is omitted when empty. The SQS `MessageGroupId` is set from `docgen.client.message-group-id` plus the priority suffix (`-high` or `-low`) and is not part of this JSON body. The document generator worker must preserve supplied metadata from the queue message in the Kafka result event.

Status markers currently understood by the client are:

| Marker | Meaning |
| --- | --- |
| `uploaded` | Input DOCX was uploaded. |
| `submitted` | Job message was published. |
| `submission_failed` | Job message publication failed. |
| `processing` | Worker has started processing. |

## Provider Support

Provider-specific storage and queue implementations are owned by this repository, not by consuming backend applications. If a backend needs a non-AWS provider, add a concrete adapter module to this repository and have the backend depend on that adapter artifact.

## Backend Responsibilities

The consuming backend owns:

| Responsibility | Notes |
| --- | --- |
| DOCX creation | Render templates and any pre-processing before calling the client. |
| Domain persistence | Store `jobId`, request IDs, generated file metadata, and domain-specific status. |
| Idempotency | Ensure Kafka handlers can run more than once without duplicating files or corrupting state. |
| Error policy | Map infrastructure failures and timeouts to soft retryable user-facing errors unless the backend has a domain-specific recovery flow. |
| Infrastructure configuration | Provide matching storage, queue, Kafka, and AWS settings for the backend and document generator worker. |
