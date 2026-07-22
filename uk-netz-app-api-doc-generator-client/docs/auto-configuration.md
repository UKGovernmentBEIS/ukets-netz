# Auto-Configuration

The library uses Spring Boot auto-configuration in both modules. The core module creates the `DocumentGeneratorClient` facade only when generic storage and queue beans are available. The AWS adapter creates those generic beans from Spring Cloud AWS operations.

## Core Auto-Configuration

`DocGenClientAutoConfiguration` is imported by:

```text
uk-netz-app-api-doc-generator-client-core/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

It enables:

- `DocGenClientProperties`
- `DocGenKafkaConsumerConfigProperties`
- Spring Boot `KafkaProperties`

## Core Beans

| Bean | Condition | Notes |
| --- | --- | --- |
| `DocumentGeneratorClient` | missing bean and `DocumentStorageClient` plus `JobQueuePublisher` exist | Facade over sync, async, status, and download behavior. |

The facade validates queue destinations when it is constructed. The AWS storage adapter validates `storage-container` when it is constructed. `message-group-id` is validated before upload when a sync or async submission is made, so applications that only bind the properties class without storage and queue adapters do not fail on startup.

## Core Properties

Prefix: `docgen.client`

| Property | Default | Required for | Description |
| --- | --- | --- | --- |
| `storage-container` | `doc-gen` | AWS storage adapter | Provider storage container. For AWS, this is the S3 bucket. |
| `sync-queue-destination` | `doc-gen-sync-jobs` | sync submissions | Queue destination for sync conversion jobs. |
| `async-queue-destination` | `doc-gen-async-jobs` | async submissions | Queue destination for async conversion jobs. |
| `message-group-id` | none | sync and async submissions | Mandatory base SQS message group ID for the consuming application. The client appends `-high` or `-low`. |
| `sync-timeout` | `30s` | sync submissions | Maximum wait for a terminal sync result. |
| `sync-poll-interval` | `500ms` | sync submissions | Delay between storage checks. |
| `download-url-expiry` | `15m` | signed URLs | Default signed URL duration when caller passes `null`. |
| `max-handler-retries` | `3` | Kafka consumer | Failed event deliveries allowed before an event is treated as poison and skipped. |

## AWS Auto-Configuration

`DocGenClientAwsAutoConfiguration` is imported by:

```text
uk-netz-app-api-doc-generator-client-aws/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

It runs:

- after Spring Cloud AWS `S3AutoConfiguration`
- after Spring Cloud AWS `SqsAutoConfiguration`
- before core `DocGenClientAutoConfiguration`

This order allows AWS adapter beans to satisfy the core module's generic bean conditions.

## AWS Beans

| Bean | Condition | Notes |
| --- | --- | --- |
| `DocumentStorageClient` | missing bean, `S3Operations` exists, AWS adapter enabled | Uses `S3DocumentStorageClient`. |
| `JobQueuePublisher` | missing bean, `SqsOperations` and `ObjectMapper` exist, AWS adapter enabled | Uses `SqsJobQueuePublisher`. |

Prefix: `docgen.client.aws`

| Property | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Disables AWS adapter bean creation when set to `false`. |

Region, endpoint, path-style access, credentials, and client-level AWS settings are owned by Spring Cloud AWS properties, not by this library. AWS credentials are resolved by Spring Cloud AWS and the AWS SDK credential provider chain when static `spring.cloud.aws.credentials.*` properties are not set. Local development with `../uk-netz-env-development` should provide LocalStack values through environment or application properties.

## Kafka Auto-Configuration

Kafka beans are nested under `DocGenClientAutoConfiguration.KafkaAutoConfiguration`.

They are created only when:

- Kafka listener support is on the classpath
- UK NETZ Kafka factory classes are on the classpath
- `kafka.docgen-consumer.enabled=true`

| Bean | Notes |
| --- | --- |
| `KafkaCorrelationHeaderProducerInterceptor<String, ConversionEvent>` | Default UK NETZ correlation header interceptor if missing. |
| `KafkaCorrelationParentHeaderProducerInterceptor<String, ConversionEvent>` | No-op doc-gen parent header interceptor if missing. |
| `NetzKafkaProducerFactory<String, ConversionEvent>` | Required by the UK NETZ consumer factory. |
| `NetzKafkaConsumerFactory<String, ConversionEvent>` | Creates the listener container factory. |
| `docGenKafkaListenerContainerFactory` | Manual immediate ack, synchronous commits, auto-commit forced off. |
| `DocGenKafkaPoisonEventTracker` | Tracks failures by topic, partition, offset, and job ID. |
| `DocGenKafkaResultConsumer` | Invokes ordered `ConversionResultHandler` beans. |

## Kafka Properties

Prefix: `kafka.docgen-consumer`

| Property | Default | Description |
| --- | --- | --- |
| `enabled` | `false` | Enables the result consumer. |
| `topic` | `doc.converted` | Result event topic. |
| `group` | `app-api-doc-result-consumer` | Consumer group for the doc-gen listener. |

The listener factory calls `setEnableAutoCommit(false)` on the doc-gen consumer properties even if global Kafka defaults enable auto-commit.
The result consumer logs individual handler failures at `WARN` before retrying the record, and logs poison-event skips at `ERROR` when the retry threshold is reached.

## Override Policy

Most beans use `@ConditionalOnMissingBean`, so tests and advanced consumers can override individual pieces. Preferred override points are:

- `ConversionResultHandler` beans for application async processing.
- `DocumentStorageClient` or `JobQueuePublisher` in provider adapter modules or tests.
- `DocumentGeneratorClient` only when replacing the complete consumer facade deliberately.

When adding new auto-configuration, keep the same pattern: create generic contracts first, then let core orchestration wire itself from those contracts.
