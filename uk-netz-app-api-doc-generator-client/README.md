# UK NETZ Document Generator Client

Spring Boot client libraries for submitting DOCX files to the document generator service and reading generated PDF results.

This repository builds Maven library artifacts. It does not build or publish an application image.

## Start Here

| Audience | Entry point |
| --- | --- |
| Backend application developers integrating the library | [CONSUMERS.md](CONSUMERS.md) |
| Library maintainers and contributors | [docs/README.md](docs/README.md) |
| Architecture and design details | [docs/architecture.md](docs/architecture.md) |
| Runtime flow details | [docs/runtime-flows.md](docs/runtime-flows.md) |
| Spring Boot auto-configuration details | [docs/auto-configuration.md](docs/auto-configuration.md) |
| Kafka result-consumer details | [docs/kafka-result-consumer.md](docs/kafka-result-consumer.md) |
| Provider adapter guidance | [docs/provider-adapters.md](docs/provider-adapters.md) |
| Testing and CI guidance | [docs/testing-and-ci.md](docs/testing-and-ci.md) |

## Modules

| Module | Purpose |
| --- | --- |
| `uk-netz-app-api-doc-generator-client-core` | Provider-neutral API, orchestration, Spring Boot auto-configuration, Kafka result consumer, and storage/queue contracts. |
| `uk-netz-app-api-doc-generator-client-aws` | AWS adapter backed by Spring Cloud AWS S3 and SQS operations. |

Backend applications should depend on a concrete provider adapter artifact. The core module is the provider-neutral foundation used by those adapters.

Use the AWS Spring Cloud adapter when the application uses Spring Cloud AWS S3 and SQS:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-doc-generator-client-aws</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Architecture At A Glance

The core module keeps orchestration independent from concrete storage and queue providers.

| Component | Responsibility |
| --- | --- |
| `DocumentGeneratorClient` | Consumer facade for blocking sync submission, async submission, status resolution, and PDF download helpers. |
| `ConversionResultHandler` | Application extension point invoked by the Kafka result consumer for async conversion events. |
| `DocumentStorageClient` | Provider-neutral storage contract. |
| `JobQueuePublisher` | Provider-neutral queue contract. |

The AWS module contributes implementations of `DocumentStorageClient` and `JobQueuePublisher` when Spring Cloud AWS S3/SQS classes and operations are available. AWS SDK and Spring Cloud AWS types stay inside the AWS adapter module.

Consuming applications use the `DocumentGeneratorClient` facade directly. Lower-level sync, async, status, and download components are internal implementation details and are not separate consumer extension points.

The shared worker contract uses deterministic object keys:

| Object | Key |
| --- | --- |
| Input DOCX | `input/{jobId}.docx` |
| Status marker | `status/{jobId}/{marker}` |
| Output PDF | `output/{jobId}.pdf` |
| Error detail | `output/{jobId}.error.json` |

Submissions use `DocumentGenerationRequest`. Requests default to `HIGH` priority unless a caller sets `LOW`. The SQS adapter publishes every job with `MessageGroupId` `{docgen.client.message-group-id}-high` or `{docgen.client.message-group-id}-low`; the message group ID is transport metadata and is not serialized into the worker JSON body.

Queue messages can carry optional `normalize=true` to request DOCX normalization before conversion. Queue messages and Kafka result events can also carry optional `metadata` as `Map<String, String>` for backend business context. Empty metadata and `normalize=false` are omitted from JSON.

For implementation detail, see [Developer Documentation](docs/README.md).

## Minimal Configuration

The standard document-generator bucket and queues default to the `../uk-netz-env-development` contract:

- `docgen.client.storage-container=doc-gen`
- `docgen.client.sync-queue-destination=doc-gen-sync-jobs`
- `docgen.client.async-queue-destination=doc-gen-async-jobs`
- `docgen.client.message-group-id=<consuming-application-base-id>` (mandatory for submissions and validated before upload)

```yaml
docgen:
  client:
    sync-timeout: 30s
    sync-poll-interval: 500ms
    message-group-id: my-service-docgen
    download-url-expiry: 15m
    max-handler-retries: 3
    aws:
      enabled: true

kafka:
  docgen-consumer:
    enabled: false
    topic: doc.converted
    group: my-service-docgen-results
```

Spring Cloud AWS properties control region, endpoints, S3 path-style access, credentials, and the underlying S3/SQS clients. When static credential properties are omitted, Spring Cloud AWS uses the AWS SDK default credential provider chain, including credentials injected through `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`. LocalStack development can still set dummy `test` credentials explicitly. Kafka bootstrap servers, security, and shared client defaults are configured with standard `spring.kafka.*` properties. The AWS adapter includes Amazon MSK IAM auth on the classpath, but consumers still configure IAM authentication through Kafka security properties.

Full consumer integration examples are in [CONSUMERS.md](CONSUMERS.md).

## Development

Use Java 21 and local Maven.

```bash
mvn clean verify
```

Useful narrower commands:

```bash
mvn -pl uk-netz-app-api-doc-generator-client-core test
mvn -pl uk-netz-app-api-doc-generator-client-aws test
mvn -pl <module> -Dtest=ClassNameTest test
```

Integration tests use Testcontainers. The AWS adapter integration tests use LocalStack for S3 and SQS. A working container runtime is required for the full verification command.

## CI

The Jenkins pipeline runs Maven verification for PRs. On `master` and `release` branches it also runs code quality, dependency track checks, build, and Maven artifact deployment.
