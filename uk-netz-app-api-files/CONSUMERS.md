# Consuming the UK NETZ file API library

This guide covers dependency setup, storage-provider configuration, and file storage from consuming applications. For architecture and developer build instructions, see [README.md](README.md).

## Dependency

Add the files library to the consuming application. The current development version is:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-files</artifactId>
    <version>1.15.0-SNAPSHOT</version>
</dependency>
```

For object storage, choose a provider for `FileStorageService`: use the [S3 implementation](#s3-provider) or supply
[another provider](#other-providers). The AWS starter is optional and must be included explicitly when selecting S3.
[Database-backed services](#database-backed-file-services) can be used without an object-storage provider.
The existing transitive `software.amazon.awssdk:s3` dependency is retained for compatibility, including its
`netty-nio-client` and `apache-client` exclusions. It does not supply Spring Cloud AWS auto-configuration or create a client.

## File storage API

Inject `uk.gov.netz.api.files.storage.FileStorageService` and supply a destination per call:

```java
List<StoredFile> stored = storage.storeFiles(
    new StorageDestination("documents", "reports/annual"),
    List.of(FileUpload.builder()
        .fileName("report.pdf")
        .content(pdfBytes)
        .contentType("application/pdf")
        .build()));

storage.deleteFile(stored.getFirst().getReference());
```

A destination contains a container and a nonblank prefix. Prefix normalization strips surrounding Java-recognized Unicode whitespace and ASCII characters previously handled by `trim()` from each segment, removes empty segments, and rejects normalized `.` and `..` segments. Internal characters are preserved. The library assigns a UUID to each file and stores it at `{normalized-prefix}/{uuid}`. The S3 implementation maps containers to bucket names.

The service validates and prepares every upload before storing any file. Results are immutable, follow input order, and contain the UUID, exact `FileReference(container, path)`, filename, content type, and size. Empty input returns an empty list without storage calls. Delete uses the reference exactly as supplied.

`FileUpload` accepts optional content type, content disposition, and cache control. Missing content types are detected, with `application/octet-stream` as fallback. The service imposes no download disposition or cache policy. `FileNameSanitizer.sanitize` is available for callers preparing safe filenames: it strips paths and unsafe characters, strips surrounding whitespace using Java's `String.strip()`, and rejects empty or all-dot results. Callers must not modify upload bytes during storage.

Storage operation failures throw `FileStorageException`, retaining the provider failure as the cause. A failed S3 batch triggers best-effort deletion of every attempted upload, including the upload that threw: the provider may have stored an object before the client received an error. Unattempted uploads are excluded. Cleanup continues after individual deletion failures, which are suppressed on the storage exception. Validation failures occur before upload. The batch is not a storage transaction, and cleanup is not guaranteed after network failures.

UUIDs and stored paths may serve as bearer credentials. File/reference DTOs and builders use standard Lombok string representations; identifiers and upload-builder content are not redacted.

## S3 provider

Applications selecting S3 explicitly add the optional starter, with its version managed by the NETZ parent:

```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-s3</artifactId>
</dependency>
```

Boot auto-configuration registers `S3FileStorageService` when an `S3Operations` bean exists and no `FileStorageService` has been supplied. It shares the application's configured operations and creates no separate SDK client or credentials provider. Auto-configuration is loaded through Boot's imports mechanism; do not component-scan `uk.gov.netz.autoconfigure`.

Configure AWS through standard Spring Cloud AWS properties and workload credentials:

```properties
spring.cloud.aws.region.static=eu-west-2
```

For LocalStack:

```properties
spring.cloud.aws.endpoint=http://localhost:4566
spring.cloud.aws.credentials.access-key=test
spring.cloud.aws.credentials.secret-key=test
spring.cloud.aws.region.static=eu-west-2
spring.cloud.aws.s3.path-style-access-enabled=true
```

The S3 provider needs `s3:PutObject` and `s3:DeleteObject` on the selected destinations. Applications and infrastructure own provisioning, access controls, encryption, expiry, and lifecycle cleanup. This library does not create public URLs or change object ACLs.

## Other providers

Supply a `FileStorageService` bean implementing the same contract. A custom implementation needs no AWS APIs or configuration; consumers that do not use the retained S3 SDK can exclude that dependency. Endpoint, account, credentials, and provider naming restrictions belong to that implementation's configuration. One provider is selected per application; destinations vary within that provider.

## Database-backed file services

Applications using the attachment, document, or note services configure their datasource and include the relevant service, entity, and repository packages in Spring/JPA scanning. Include the required entity changelogs from [db/migration/changelogs](src/main/resources/db/migration/changelogs) in the application's Liquibase master changelog.

Register [FileTypesProperties](src/main/java/uk/gov/netz/api/files/common/FileTypesProperties.java) and
[ClamAVProperties](src/main/java/uk/gov/netz/api/files/common/ClamAVProperties.java) through configuration-property scanning
or `@EnableConfigurationProperties`. Configure MIME allowlists through `files.allowed-mime-types` and
`files.zip.allowed-mime-types`, the extracted ZIP size through `files.zip.extracted-max-size-mb`, and scanning through
`clamav.host` and `clamav.port`. The per-file size bounds are defined by
[FileConstants](src/main/java/uk/gov/netz/api/files/common/FileConstants.java), not by the object-storage configuration.

For an uploaded attachment, supply the complete `FileDTO` to the validated domain service:

```java
FileDTO input = FileDTO.builder()
    .fileName("report.pdf")
    .fileType("application/pdf")
    .fileContent(pdfBytes)
    .fileSize(pdfBytes.length)
    .createdBy(userId)
    .build();

String uuid = fileAttachmentService.createFileAttachment(input, FileStatus.PENDING);
FileDTO saved = fileAttachmentService.getFileDTO(uuid);
fileAttachmentService.updateFileAttachmentStatus(uuid, FileStatus.SUBMITTED);
```

The snippet assumes an injected `FileAttachmentService`, a valid application user ID, allowed file content, and configured
validators. `FileDTO` is in `uk.gov.netz.api.files.common.domain.dto`; `FileStatus` is in `uk.gov.netz.api.files.common.domain`.
Applications control status transitions and may use the service's pending-file cleanup and bulk ZIP operations as needed.

For generated documents, inject `FileDocumentStorageService` from `uk.gov.netz.api.files.documents.service.storage` and
run document operations within the application's transaction when lazy file content is needed:

```java
FileInfoDTO document = fileDocumentStorageService.createFileDocument(pdfBytes, "notice.pdf");
FileDTO saved = fileDocumentStorageService.getFileDTO(document.getUuid());
```

This storage path assigns a UUID, infers MIME metadata, marks the document submitted, and records the system creator.
It does not invoke the attachment/note/document-template validator chain. Use appropriate application validation before
storing generated bytes. Object-storage upload preparation likewise checks filename/content presence and resolves MIME
types without running that database validator chain.

`FileAttachmentStorageService`, `FileDocumentStorageService`, and the note/document-template token services offer
token-based retrieval through `api-token`. Configure that library and enforce access checks in the consuming application's
controllers. These application download tokens are independent of public S3/CloudFront URLs.

## Email-linked files

Use the adapter provided by
[uk-netz-app-api-notification](https://github.com/UKGovernment-IDET/uk-netz-app-api-notification) when files should become
public download links in email. Its `CONSUMERS.md` describes `notification.email-link.*`, SMTP setup, and template authoring.
The `api-notificationapi` contract keeps `EmailData.attachments` and `EmailData.linkedFiles` independent, so they can coexist.

The notification implementation uploads linked files before rendering and exposes `fileLinks`, `attachmentNames`, and
`downloadLink(file, label?)` to templates. Templates own the text and placement; there is no appended fallback list.
Successful uploads remain after later template or SMTP/MIME failures. Storage and link-construction failures retain
their own best-effort cleanup. This library supplies the underlying storage operations and does not orchestrate email delivery.
