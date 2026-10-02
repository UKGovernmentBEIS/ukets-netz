# UK NETZ notification API library

`uk-netz-app-api-notificationapi` is the shared Java contract library for email and system notifications. It builds a
JAR for use inside backend applications and other NETZ libraries. It provides DTOs, service interfaces, template
parameter names, and a configuration-properties model; it provides no REST endpoints or notification delivery implementation.

This README covers the design and development of the library. For dependencies, wiring, and calling examples, see
[CONSUMERS.md](CONSUMERS.md).

## Architecture

| Area | Main types | Responsibility |
| --- | --- | --- |
| Template input | `EmailData<T>`, `EmailNotificationTemplateData` | Template selection and parameters, with separate maps for MIME attachments and linked-file bytes. |
| Rendered email | `Email`, `EmailRecipients` | Sender, TO/CC/BCC recipients, subject, body, and MIME attachments passed to the transport. |
| Email interfaces | `NotificationEmailService<T>`, `SendEmailService` | Separate template-based notification requests from sending an already-rendered email. |
| File-link contract | `EmailFileLink`, `EmailLinkedFileStorageService` | Anonymous download metadata and a bulk storage service-provider interface (SPI). |
| Template vocabulary | `EmailFileTemplateConstants` | Reserved names for generated file links, attachment names, and the link-rendering helper. |
| System notifications | `SystemNotificationInfo`, `SendSystemNotificationService`, `NotificationContent` | Routing/template input and rendered notification content. |
| Configuration | `NotificationProperties` | Shared `notification.email.*` and `notification.smtp-headers.*` properties. |

### Separation of responsibilities

```text
Caller supplies EmailData + recipients
  -> NotificationEmailService implementation
  -> optional linked-file storage + template rendering
  -> Email
  -> SendEmailService implementation
```

The implementation of this flow belongs to
[uk-netz-app-api-notification](https://github.com/UKGovernment-IDET/uk-netz-app-api-notification). Its built-in file-link
adapter uses [uk-netz-app-api-files](https://github.com/UKGovernment-IDET/uk-netz-app-api-files), which owns provider-neutral
object storage and optional S3 support. The request library prepares official-notice documents using these contracts.
Neither the files library nor this API library depends on the notification implementation.

This artifact contains no FreeMarker renderer, AWS client, database schema, or SMTP orchestration. Its POM includes
Spring Mail, Log4j2, NETZ common, and competent-authority dependencies, so it is not a dependency-free DTO artifact.

## Contract behavior

- `EmailData` builder defaults create independent, non-null `attachments` and `linkedFiles` maps. Each maps logical
  filenames to complete byte arrays. Both may be populated; no size threshold selects a delivery mode.
- `EmailNotificationTemplateData` carries a template name, optional competent authority, and a default empty parameter
  map. Its extensible builder supports application-specific subclasses and corresponding service implementations.
- `Email` has only MIME attachments. A template-based sender must resolve linked-file bytes before creating this transport DTO.
- `EmailLinkedFileStorageService.storeFiles` returns one link per input entry, ordered by filename. Empty input returns
  an empty list. A failed storage batch throws a runtime exception and performs best-effort cleanup inside the provider.
- `EmailFileLink` is immutable value metadata containing a UUID, logical filename, and public URI. Its string representation includes these values.
- The interfaces return `void` for notification delivery. Threading, retry, persistence, and delivery-error handling are
  implementation choices; the interfaces do not themselves guarantee asynchronous delivery.
- `NotificationProperties` validates fields inside configured email and SMTP-header blocks. The consuming application
  registers the properties bean. This JAR does not register sender implementations or a default system-notification sender.

The standard NETZ implementation supplies file metadata and a rendering helper under `fileLinks`, `attachmentNames`,
and `downloadLink`. With linked files present, generated values take precedence. Otherwise, existing caller values
(including explicit nulls) are preserved and only absent keys receive defaults. See the consumer guide for the
distinction between API guarantees and NETZ's delivery behavior.

## Development and verification

The current artifact is `1.4.0-SNAPSHOT`, using NETZ parent `1.12.0` and its Java 21 / Spring Boot 3.5.14 baseline.
See [pom.xml](pom.xml) for dependencies.

| Command | Purpose |
| --- | --- |
| `mvn test` | Run collection-default, value-object, and storage-contract tests. |
| `mvn clean verify` | Run the complete build and configured quality checks. |
| `mvn clean install` | Verify and install the snapshot for downstream builds. |

This repository has no Docker integration-test suite. Install this snapshot and the independent files snapshot before
building `api-notification` or `api-request`. Both downstream libraries use this API; neither needs the other to compile.

Keep changes additive where possible, preserve the distinction between input and rendered-email DTOs, and keep provider
and rendering implementations out of this module. Update [CONSUMERS.md](CONSUMERS.md) when public contracts change.
