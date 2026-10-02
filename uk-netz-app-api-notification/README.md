# UK NETZ notification library

`uk-netz-app-api-notification` is a reusable Spring Boot library for database-backed notification templates, direct SMTP email delivery, and pluggable in-application system notifications.

The repository builds a JAR. It is not a deployable notification service and does not expose HTTP endpoints. Email is rendered and dispatched from the consuming application's JVM.

This README covers architecture and development. For application setup and calling examples, see [CONSUMERS.md](CONSUMERS.md).

## Responsibilities

The library provides:

- a JPA model and repositories for notification templates;
- FreeMarker substitution for template subjects and bodies;
- Markdown-to-HTML conversion for email bodies;
- synchronous storage of explicitly linked email files through a pluggable SPI and template-controlled link rendering;
- asynchronous, in-process email dispatch through Spring `JavaMailSender`;
- query and update services for managed templates;
- a system-notification orchestration service with a replaceable sender;
- Liquibase changesets for the template table and four account-recovery templates.

The consuming application remains responsible for:

- Spring component, entity, repository, configuration-property, and JPA auditing setup;
- including the library's Liquibase changesets in its master changelog;
- SMTP configuration and operational monitoring;
- enabling the built-in file-link adapter with a `FileStorageService`, or supplying a custom `EmailLinkedFileStorageService`;
- supplying templates required by its business flows;
- exposing any REST endpoints and enforcing request-level authorization;
- providing a `SendSystemNotificationService` when system notifications must be persisted or delivered.

## Runtime design

```text
Email caller
  -> NotificationEmailService
  -> when linked files exist: store files synchronously
  -> add fileLinks, attachmentNames, and downloadLink to a copy of template parameters
  -> load template by (name, competent authority)
  -> FreeMarker substitution and HTML escaping
  -> Markdown to safe HTML
  -> apply the non-production disclaimer when configured
  -> schedule on CompletableFuture common pool
  -> JavaMailSender -> SMTP

System-notification caller
  -> SystemNotificationProcessAndSendService
  -> load global template by (name, null)
  -> FreeMarker substitution and HTML escaping
  -> SendSystemNotificationService implementation

Template-management caller
  -> query/update services
  -> NotificationTemplateRepository
  -> PostgreSQL notification_template table
```

Template lookup, rendering, and any linked-file storage happen synchronously. Only the final email send is scheduled
asynchronously.

### Main components

| Area | Component | Responsibility |
| --- | --- | --- |
| Templates | `NotificationTemplateProcessService` | Loads a template, applies FreeMarker parameters, escapes the result, and optionally converts Markdown to HTML. |
| Templates | `NotificationTemplateQueryService` | Searches templates, returns managed-template details, and supplies competent-authority information to authorization rules. |
| Templates | `NotificationTemplateUpdateService` | Updates the subject and body of an existing managed template in a transaction. |
| Persistence | `NotificationTemplateRepository` | Provides JPA lookup plus PostgreSQL-native managed-template search. |
| Email | `NotificationEmailServiceImpl` | Stores explicitly linked files, supplies file template parameters, renders the email, and schedules delivery. |
| Email | `EmailLinkedFileStorageService` | Optional API SPI implemented by the built-in provider-neutral adapter or by the consuming application. |
| Templates | `DownloadLinkMethod` | FreeMarker helper producing a safely escaped Markdown link with a default filename or template-supplied label. |
| Email | `StoredEmailLinkedFileStorageService` | Sanitizes filenames, supplies download metadata to `FileStorageService`, and builds anonymous public URLs. |
| Email | `NotificationEmailWithDisclaimerServiceImpl` | Adds a test-system disclaimer when `env.isProd=false`. |
| Email | `JavaSendEmailServiceImpl` | Builds a multipart MIME message, adds attachments and an optional SES configuration-set header, and invokes `JavaMailSender`. |
| Email | `MailMimeSystemPropertiesSetter` | Disables Jakarta Mail splitting of long MIME parameters for the whole JVM. |
| System | `SystemNotificationProcessAndSendService` | Renders a global template and delegates delivery to `SendSystemNotificationService`. |
| System | `SendSystemNotificationServiceAutoConfiguration` | Supplies a no-op sender when the application has no implementation. |

## Linked-file storage

The built-in adapter depends on the optional `uk-netz-app-api-files` library and its provider-neutral `FileStorageService`.
Boot registers it when `notification.email-link.enabled=true`, the files API is available, and no custom email-file
provider overrides it. Application setup belongs in the [consumer guide](CONSUMERS.md#built-in-storage-adapter).

The adapter validates its configured destination, sanitizes and sorts filenames, adds attachment disposition and `Cache-Control: no-store`, and maps returned storage paths to public URLs. UUIDs are assigned by the files library. The adapter cleans up stored files if constructing their links fails.

Configuration properties reference no optional files types, so broad property scanning works without that dependency. Destination validation belongs to the built-in adapter and runs only when it is created.

Configuration binding parses the public URL text directly to preserve existing percent escapes. URL construction preserves the encoded base path, removes only literal trailing slashes, and encodes the stored path once before appending it. For example, a base path of `/base%2Fsegment` retains `%2F` in the email link.

Integration is disabled by default. The container defaults to `uk-ets-files`, the prefix is required, and the public base URL defaults to `http://uk-ets-files.s3.localhost.localstack.cloud:4566` for local development. Deployed applications configure their public origin. A custom `EmailLinkedFileStorageService` overrides the built-in adapter and does not require the files library or its settings.

For S3, explicitly include `spring-cloud-aws-starter-s3` and configure the shared client through `spring.cloud.aws.*`. A custom `FileStorageService` can use another provider without AWS configuration. The files library retains its legacy transitive S3 SDK; the Spring Cloud AWS starter remains optional. Storage selection and credentials belong to provider configuration; notification code uses container/path references only.

The adapter is registered through Boot auto-configuration, outside notification component scanning. Core notification
services and the no-op system sender still require component discovery. Do not component-scan `uk.gov.netz.autoconfigure`.
See [CONSUMERS.md](CONSUMERS.md) for dependency and infrastructure setup.

## Template model and persistence

Templates are stored in `notification_template` and identified for rendering by `name` plus nullable `competent_authority`.

| Column | Purpose |
| --- | --- |
| `name`, `subject`, `text` | Template identity and FreeMarker/Markdown content. |
| `competent_authority` | Selects a regulator-specific email template; `NULL` denotes a global template. |
| `event_trigger`, `workflow`, `role_type` | Management metadata used when listing templates. |
| `is_managed` | Controls visibility through managed-template query and update operations. It does not control whether a template can be rendered. |
| `last_updated_date` | Maintained through Spring Data JPA auditing. |

The DDL is PostgreSQL-specific. Search uses `ILIKE`, `ANY`, `LIMIT`/`OFFSET`, and trigram GIN indexes, so the database must provide the `pg_trgm` extension before the DDL runs.

Because `competent_authority` is nullable, PostgreSQL's unique constraint does not prevent multiple `(name, NULL)` rows. Application migrations must preserve a single global row for each template name so repository lookup remains unambiguous.

The supplied data changelog seeds these global, unmanaged email templates:

| Template | Parameters |
| --- | --- |
| `ResetPasswordRequest` | `resetPasswordLink`, `expirationMinutes`, `contactRegulator` |
| `ResetPasswordConfirmation` | `homeUrl`, `contactRegulator` |
| `Reset2FaConfirmation` | `homeUrl`, `contactRegulator` |
| `Change2FA` | `change2FALink`, `expirationMinutes`, `contactRegulator` |

## Internal dependencies

| Library | Integration used here |
| --- | --- |
| `uk-netz-app-api-notificationapi` | Public email/system-notification contracts, DTOs, `NotificationProperties`, and the linked-file storage SPI. |
| `uk-netz-app-api-common` | Business exceptions, error codes, paging, and shared MapStruct configuration. |
| `uk-netz-app-api-competentauthority` | `CompetentAuthorityEnum` used for template partitioning. |
| `uk-netz-app-api-authorization` | `NotificationTemplateAuthorityInfoProvider`, implemented by the query service. |
| `uk-netz-app-api-files` | Optional provider-neutral storage and filename sanitization used by the built-in file-link adapter. |

The contract types intentionally live in `uk-netz-app-api-notificationapi`; this artifact supplies their database-backed and SMTP-backed implementations.

## Delivery semantics and limits

- `NotificationEmailService` is fire-and-forget. It returns after scheduling `SendEmailService` on `CompletableFuture`'s default common pool and exposes no completion handle.
- Template lookup and rendering failures occur before scheduling and propagate as `BusinessException`.
- When `EmailData.linkedFiles` is non-empty, the library resolves exactly one `EmailLinkedFileStorageService` and stores
  all linked files synchronously before template lookup/rendering. Missing/multiple providers and storage failures
  propagate before rendering or SMTP scheduling.
- A copy of the caller's template model receives `fileLinks`, `attachmentNames`, and `downloadLink`. Generated values
  override caller entries when linked files are present. Otherwise, caller entries (including explicit nulls) are preserved
  and only absent keys receive defaults. Templates own all link wording and placement; no heading or fallback link
  section is appended. See [CONSUMERS.md](CONSUMERS.md#file-parameters-in-email-templates) for examples.
- Empty or null `linkedFiles` preserve legacy behavior and do not resolve the optional storage provider.
- Both email implementations retain their original three-argument constructors for attachment-only callers and subclasses. Spring selects the four-argument constructor for provider-aware wiring.
- MIME `attachments` and linked files are independent and may coexist. Only the explicitly supplied attachment map
  becomes MIME parts; this library never switches delivery mode based on file size.
- SMTP and MIME failures are caught and logged by `JavaSendEmailServiceImpl`; they are not returned to the caller and are not retried by this library.
- Attachments are held in memory as `Map<String, byte[]>` and copied into a multipart MIME message.
- Successfully stored linked files remain available after later template lookup/rendering or SMTP/MIME failures.
  Template failures propagate without scheduling email. Storage and link-construction failures retain their existing
  best-effort rollback; the storage SPI has no post-render cleanup hook.
- Component initialization sets the JVM-wide `mail.mime.splitlongparameters=false` system property.
- `env.isProd` must be set explicitly. `true` selects normal email content; `false` prepends the test-system disclaimer. If it is absent, no `NotificationEmailService` implementation is created.
- System-notification processing is synchronous, but delivery silently does nothing when the default no-op sender is active.
- The library contains no delivery ledger, durable queue, retry policy, recipient preferences, or notification history.

See [CONSUMERS.md](CONSUMERS.md) for dependency setup, Spring wiring, migrations, configuration, and API examples.

## Development and verification

The current artifact is `1.8.0-SNAPSHOT`, using NETZ parent `1.12.0` and its Java 21 / Spring Boot 3.5.14 baseline.
The build also uses Maven, MapStruct, Lombok, Checkstyle, ArchUnit, JUnit, and Testcontainers. See [pom.xml](pom.xml) for dependencies.

The four email-link libraries use NETZ parent `1.12.0` and retain their development snapshot versions. For local builds, install `api-notificationapi:1.4.0-SNAPSHOT` and `api-files:1.15.0-SNAPSHOT` first; then build `api-notification:1.8.0-SNAPSHOT` and `api-request:1.38.0-SNAPSHOT` in either order.

Run unit and architecture tests without the Docker-backed repository integration tests:

```bash
mvn test
```

Run the complete build, including PostgreSQL and LocalStack Testcontainers integration tests:

```bash
mvn clean verify
```

The full verification command requires a working Docker-compatible container runtime. Integration tests exercise JPA repositories against PostgreSQL and the S3 email-link flow through real FreeMarker rendering and MIME construction against LocalStack, including retained uploads after template failures. The SMTP transport is mocked; the current test suite does not execute the Liquibase changelogs.

The Jenkins pipeline runs Maven verification for pull requests. On `master` and `release` it additionally performs code-quality and dependency checks, builds the artifact, and deploys it to the configured Maven repository.

## Maintainer checklist

When behavior changes, keep the documentation and these contracts aligned:

- update [CONSUMERS.md](CONSUMERS.md) when configuration, scanning, or public service APIs change;
- update the template table description when `NotificationTemplate` or its Liquibase DDL changes;
- keep seed-template names and parameters aligned with `notification_template.xml`;
- document any change to asynchronous execution, error propagation, disclaimer selection, or the no-op system sender as a consumer-facing compatibility change.
