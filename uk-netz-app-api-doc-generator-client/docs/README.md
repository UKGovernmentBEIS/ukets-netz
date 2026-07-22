# Developer Documentation

This folder is the maintainer-facing documentation for the UK NETZ Document Generator Client. It explains how the library is structured, how the runtime flows work, and how to extend the implementation without breaking the public contract used by backend services.

For backend application integration, start with [CONSUMERS.md](../CONSUMERS.md). For a repository overview, start with the [root README](../README.md).

## Reading Path

| Topic | Document |
| --- | --- |
| Module boundaries, contracts, and design rules | [Architecture](architecture.md) |
| Sync, async, status, download, and object-key flows | [Runtime Flows](runtime-flows.md) |
| Spring Boot bean wiring and configuration properties | [Auto-Configuration](auto-configuration.md) |
| Kafka listener behavior and handler failure policy | [Kafka Result Consumer](kafka-result-consumer.md) |
| AWS adapter internals and future provider adapter guidance | [Provider Adapters](provider-adapters.md) |
| Test strategy, local verification, and CI pipeline | [Testing And CI](testing-and-ci.md) |

## Maintainer Principles

- Keep provider-neutral orchestration in the core module.
- Keep AWS SDK and Spring Cloud AWS types out of the core module.
- Treat storage object keys, status markers, queue message JSON, and Kafka event JSON as shared contracts with the document generator worker and backend consumers.
- Prefer adding provider adapters in this repository over asking backend applications to implement storage or queue contracts.
- Add focused tests at the same boundary as the behavior being changed.

## Repository Shape

```text
.
|-- README.md
|-- CONSUMERS.md
|-- docs/
|-- uk-netz-app-api-doc-generator-client-core/
`-- uk-netz-app-api-doc-generator-client-aws/
```

The project is a Java 21 Maven multi-module library. It does not build a runnable application or publish an application image.

## Main Runtime Components

| Component | Module | Responsibility |
| --- | --- | --- |
| `DocumentGeneratorClient` | core | Consumer facade for blocking sync submission, async submission, status lookup, and download helpers. |
| `DocGenKafkaResultConsumer` | core | Dispatches async conversion events to application handlers with manual offset commits. |
| `DocumentStorageClient` | core contract | Provider-neutral storage API used by all orchestration. |
| `JobQueuePublisher` | core contract | Provider-neutral queue API used by sync and async submission. |
| `S3DocumentStorageClient` | AWS adapter | S3-backed `DocumentStorageClient` using Spring Cloud AWS `S3Operations`. |
| `SqsJobQueuePublisher` | AWS adapter | SQS-backed `JobQueuePublisher` using Spring Cloud AWS `SqsOperations`. |
