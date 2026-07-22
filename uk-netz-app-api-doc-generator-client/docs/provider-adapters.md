# Provider Adapters

Provider adapters connect the provider-neutral core to concrete infrastructure. The current adapter module supports AWS through Spring Cloud AWS.

## Current AWS Adapter

Module:

```text
uk-netz-app-api-doc-generator-client-aws
```

The module depends on:

- `uk-netz-app-api-doc-generator-client-core`
- `spring-cloud-aws-starter-s3`
- `spring-cloud-aws-starter-sqs`
- `aws-msk-iam-auth`

The MSK IAM dependency is classpath support for AWS-backed applications that enable the optional Kafka result consumer. It does not add library-owned Kafka security properties; consumers continue to configure Kafka authentication with standard `spring.kafka.*` settings.

S3 and SQS credentials are resolved by Spring Cloud AWS and the AWS SDK. Cloud applications should normally rely on the default credential provider chain, including credentials injected as standard AWS environment variables. LocalStack development can still use explicit dummy credentials.

It contributes:

| Contract | Implementation | Backing API |
| --- | --- | --- |
| `DocumentStorageClient` | `S3DocumentStorageClient` | Spring Cloud AWS `S3Operations` |
| `JobQueuePublisher` | `SqsJobQueuePublisher` | Spring Cloud AWS `SqsOperations` |

## S3 Storage Behavior

`S3DocumentStorageClient` maps the storage contract to S3 objects.

| Contract method | S3 behavior |
| --- | --- |
| `uploadDocx` | Uploads `input/{jobId}.docx` with DOCX content type. |
| `writeStatusMarker` | Uploads zero-byte `status/{jobId}/{marker}` marker. |
| `listStatusMarkers` | Lists `status/{jobId}/` and parses known marker names. |
| `listOutputObjects` | Lists objects with `output/{jobId}` prefix. |
| `pdfExists` | Checks `output/{jobId}.pdf`. |
| `errorJsonExists` | Checks `output/{jobId}.error.json`. |
| `downloadPdf` | Downloads and returns PDF bytes. |
| `readErrorDetail` | Reads error JSON as `ErrorDetail`. |
| `createDownloadUrl` | Creates a signed GET URL for the PDF object. |

Storage failures are wrapped in domain exceptions:

- `DocGenUploadException` for uploads
- `DocGenDownloadException` for PDF downloads, error-detail downloads, and signed URL creation
- `DocGenClientException` for general storage inspection failures

## SQS Queue Behavior

`SqsJobQueuePublisher` serializes `JobMessage` with the configured `ObjectMapper` and sends the JSON string to the destination through `SqsOperations.send(options -> ...)`. It sets the configured transport `messageGroupId` on the send options. Empty metadata and `normalize=false` are omitted from the JSON body.

Serialization and send failures are wrapped in `DocGenSubmitException`.

The destination is the configured queue destination value. The adapter does not create queues; queue provisioning is an environment concern.

## Adding A Provider Adapter

Add a new Maven module when another provider is needed. The module should depend on core and the provider SDK or Spring integration library.

Required implementation steps:

- Implement `DocumentStorageClient`.
- Implement `JobQueuePublisher`.
- Add provider auto-configuration that creates those generic beans with `@ConditionalOnMissingBean`.
- Add `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Ensure the provider auto-configuration runs before `DocGenClientAutoConfiguration`.
- Add provider-specific configuration properties only for this library's own adapter toggles or behavior.
- Leave provider credentials, endpoints, and low-level client tuning to the provider framework where possible.

Do not add provider SDK dependencies to the core module.

## Adapter Contract Expectations

A provider adapter must preserve the shared runtime contract:

- use `DocumentObjectKeys` for every object key
- write status markers with names from `StatusMarker`
- return object keys that match the keys used by `DocumentObjectKeys`
- return `Optional.empty()` from `createDownloadUrl` only when signed URLs are not supported
- throw library domain exceptions or provider exceptions that callers can distinguish from conversion results
- serialize `JobMessage` with field `jobId`, optional `normalize=true`, and optional non-empty `metadata`
- pass the provided message group ID as transport metadata, not as a JSON field

The current worker contract derives object keys from `jobId`. Do not force `normalize=false`, empty metadata, or message group IDs into the JSON body.

## Required Tests For A New Adapter

Add focused tests equivalent to the AWS adapter coverage:

- auto-configuration creates adapter beans when provider operations are present
- adapter beans are not created when disabled
- generic contract beans can be overridden
- storage adapter uploads input, writes/list markers, lists outputs, reads errors, downloads PDFs, and handles signed URL behavior
- queue adapter sends the expected JSON message
- integration test against a realistic local provider or emulator when available

The existing core architecture test must continue to pass.
