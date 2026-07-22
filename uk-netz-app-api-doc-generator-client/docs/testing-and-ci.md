# Testing And CI

The project is a Java 21 Maven multi-module library. Use local Maven; the repository does not include a Maven wrapper.

## Main Commands

Run the full verification suite:

```bash
mvn clean verify
```

Run only core tests:

```bash
mvn -pl uk-netz-app-api-doc-generator-client-core test
```

Run only AWS adapter tests:

```bash
mvn -pl uk-netz-app-api-doc-generator-client-aws test
```

Run a single test class:

```bash
mvn -pl <module> -Dtest=ClassNameTest test
```

## Test Types

| Type | Naming | Purpose |
| --- | --- | --- |
| Unit tests | `*Test.java` | Fast tests for manager behavior, storage contracts, status resolution, queue publishing, and auto-configuration conditions. |
| Integration tests | `*IT.java` | Tests that need Kafka, LocalStack, or other containerized dependencies. |
| Architecture tests | `ArchitectureTest` | Protect module boundaries such as keeping AWS dependencies out of core. |

Failsafe runs integration tests during `mvn verify`. Surefire runs unit tests during `mvn test`.

## Current Coverage Areas

| Area | Representative tests |
| --- | --- |
| Sync orchestration | `DefaultSyncJobManagerTest`, `DocumentGeneratorClientTest`, `DocGenClientIntegrationTest` |
| Async orchestration | `DefaultAsyncJobManagerTest`, `DocGenClientIntegrationTest` |
| Status and storage contract | `DefaultJobStatusResolverTest`, `DocumentObjectKeysTest`, `DocumentStorageClientContractTest` |
| Kafka consumer | `DocGenKafkaResultConsumerTest`, `DocGenKafkaResultConsumerIT` |
| Auto-configuration | `DocGenClientAutoConfigurationTest`, `DocGenClientAwsAutoConfigurationTest` |
| AWS adapter | `S3DocumentStorageClientTest`, `SqsJobQueuePublisherTest`, `DocGenClientAwsLocalStackIT` |
| Dependency boundaries | `ArchitectureTest` |

## Container Requirements

Integration tests require a working container runtime:

- Kafka coverage uses Testcontainers Kafka.
- AWS adapter integration coverage uses Testcontainers LocalStack with S3 and SQS.

If container runtime is unavailable, unit tests can still be run with module-level `test` commands, but `mvn clean verify` is the expected pre-merge command.

## Adding Tests

Use the same boundary as the behavior being changed:

- manager behavior belongs in core manager tests
- object key or marker changes belong in storage/key tests
- status precedence belongs in `DefaultJobStatusResolverTest`
- auto-configuration conditions belong in `ApplicationContextRunner` tests
- provider behavior belongs in the provider adapter module
- shared contract changes should include both core tests and adapter tests

Prefer deterministic tests. Existing manager tests inject clocks, sleepers, job ID suppliers, in-memory storage, and recording queue publishers to avoid real sleeps and external systems.

## Style And Quality

The root `pom.xml` points Maven Checkstyle at:

```text
checkstyle-uknetz.xml
checkstyle-uknetz-suppressed-checks.xml
```

The rules are based on Google Java Style with repository-specific settings:

- Java 21
- four-space indentation
- no tab characters
- no wildcard imports
- alphabetical import groups
- 200-character Java line limit
- Lombok is used consistently with existing classes

Keep unrelated refactors out of behavior changes. This library has a small public surface, so changes to model fields, object keys, markers, queue messages, or configuration names should be treated as compatibility-sensitive.

## Jenkins Pipeline

The Jenkins pipeline uses Java 21 and behaves by branch type:

| Branch type | Behavior |
| --- | --- |
| PR | Runs Maven verify with updated snapshots. |
| `master` or `release` | Runs code quality, dependency track checks, build, and artifact deployment. |

The pipeline publishes Maven library artifacts. It does not build or publish an application image.
