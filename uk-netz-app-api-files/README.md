# UK NETZ file API library

`uk-netz-app-api-files` is a library JAR providing PostgreSQL-backed file services and provider-neutral object storage with an optional S3 implementation. It runs inside consuming applications and exposes no REST controllers.

This README covers the design and development of the library. For dependencies, application configuration, and API usage,
see [CONSUMERS.md](CONSUMERS.md).

## Architecture

The database file services and object-storage API have separate persistence paths:

| Area | Responsibility |
| --- | --- |
| `attachments`, `documents`, `notes` | Database-backed file domains, with entities, repositories, services, token services, and MapStruct mappers. |
| `common` | Shared JPA base, DTOs, validators, ClamAV scanning, MIME detection, and reusable utilities. |
| `storage` | Provider-neutral `FileStorageService`, upload/result types, destinations, references, and exceptions. |
| `storage.s3` | S3 implementation using the application's shared Spring Cloud AWS `S3Operations`. |
| `uk.gov.netz.autoconfigure.files` | Conditional Boot registration of the S3 implementation. |

Email adapters, public download URLs, and email-link configuration belong to
[uk-netz-app-api-notification](https://github.com/UKGovernment-IDET/uk-netz-app-api-notification). Notification contracts
belong to `api-notificationapi`; `api-request` loads official-notice documents and passes their bytes to the notification
implementation. This files library has no dependency on either notification library.

## Implementation details

- Database entities inherit from `FileEntity`. File bytes use lazy binary content loading; retain the Hibernate bytecode enhancement plugin that enables it.
- Attachment, note, and document-template creation services use the validator chain for file type, size, archive-content checks, and virus scanning. Low-level generated-document storage has its own persistence path. Liquibase changelogs live under [src/main/resources/db/migration/changelogs](src/main/resources/db/migration/changelogs).
- Object storage prepares the complete batch before uploading and assigns UUIDs inside the library. On failure, S3 attempts cleanup of all attempted uploads, including the one that threw because it may already have reached storage. Each call carries its own destination; the S3 implementation retains no per-call state.
- Public storage types remain independent of provider SDKs. The optional S3 implementation translates provider errors into `FileStorageException` and adds cleanup failures as suppressed exceptions.
- Destination prefixes and sanitized filenames strip surrounding Unicode whitespace before validation. Prefixes retain legacy ASCII trimming; meaningful internal characters are preserved.
- The S3 SDK remains a transitive dependency for existing consumers, with the previous HTTP-client exclusions. The Spring Cloud AWS S3 starter remains optional; the SDK alone does not register a client or storage provider.
- Boot discovers `S3FileStorageAutoConfiguration` through its imports file. It runs after Spring Cloud AWS S3 configuration and supplies a default only when `S3Operations` exists and no custom `FileStorageService` is registered.
- DTOs and internal data holders use Lombok annotations and standard string representations, with no feature-specific redaction. Keep validation in the constructors used by builders and follow the root `lombok.config`.
- QueryDSL and MapStruct generate sources during the Maven build. Compile after changing entity fields.

Database download-token services integrate with `api-token`; they are separate from anonymous object-storage links.
`FileAttachmentService` also supports streaming bulk ZIP output. Consuming applications own the controllers and the
authorization rules for these operations.

The current artifact is `1.15.0-SNAPSHOT`, using NETZ parent `1.12.0` and its Java 21 / Spring Boot 3.5.14 baseline.
See [pom.xml](pom.xml) for module dependencies.

## Development and verification

| Command | Purpose |
| --- | --- |
| `mvn compile` | Compile and generate QueryDSL/MapStruct sources. |
| `mvn test` | Run unit tests and ArchUnit checks. |
| `mvn test -Dtest=S3FileStorageServiceTest` | Run the S3 implementation's unit tests. |
| `mvn clean verify` | Run the complete build, Checkstyle, and integration tests. |
| `mvn checkstyle:check` | Run the project Checkstyle checks. |
| `mvn clean install` | Verify and install the library in the local Maven repository. |

Integration tests require Docker: repository tests use PostgreSQL, and S3 tests use LocalStack. Architecture tests enforce the provider-neutral API and separation from notification code. The full suite also covers filename sanitization, optional dependency wiring, metadata, destination handling, and rollback behavior.

Build this library independently of `api-notificationapi`. Install both snapshots before building `api-notification`
or `api-request`. Keep [CONSUMERS.md](CONSUMERS.md) aligned when storage contracts, metadata, or configuration change.
