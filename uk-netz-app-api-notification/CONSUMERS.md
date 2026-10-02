# Consuming the UK NETZ notification library

This guide covers integrating `uk-netz-app-api-notification` into a Spring Boot backend. The library runs inside the backend process; it does not call the separate deployable notification worker and does not provide controllers.

For architecture and developer build details, see [README.md](README.md).

## Dependency

The current development build is `1.8.0-SNAPSHOT`:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-notification</artifactId>
    <version>1.8.0-SNAPSHOT</version>
</dependency>
```

The dependency brings in `uk-netz-app-api-notificationapi`, whose interfaces and DTOs form the public notification contract.

## Register the library with Spring

The core mail, template, and system-notification services require component discovery and JPA setup. The optional
file-link adapter is registered separately through Boot's `AutoConfiguration.imports` mechanism when enabled; it does
not replace the core service registrations below. Keep `uk.gov.netz.autoconfigure` outside component scanning.

An application that already scans `uk.gov`, as the NETZ skeleton does, only needs to ensure configuration properties, repositories, entities, and JPA auditing are enabled. A more narrowly-scoped application can use:

```java
package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateRepository;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;

@SpringBootApplication(scanBasePackages = {
    "com.example.backend",
    "uk.gov.netz.api.notification"
})
@EnableConfigurationProperties(NotificationProperties.class)
@EnableJpaRepositories(basePackageClasses = {
    BackendApplication.class,
    NotificationTemplateRepository.class
})
@EntityScan(basePackageClasses = {
    BackendApplication.class,
    NotificationTemplate.class
})
@EnableJpaAuditing
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```

Component scanning registers the mail, template, mapping, and system-notification services. `@EnableJpaAuditing` is required for `lastUpdatedDate` maintenance.

## Apply the database changelogs

Add both classpath resources to the consuming application's master Liquibase changelog, in this order:

```xml
<include file="db/migration/changelogs/notification_template_ddl.xml" />
<include file="db/migration/changelogs/notification_template.xml" />
```

Before applying the DDL:

- use PostgreSQL;
- install the `pg_trgm` extension because the changelog creates `gin_trgm_ops` indexes;
- ensure the Liquibase context configuration permits the DDL's `migrate` context;
- include the changesets only once across the assembled application classpath.

The DDL creates `notification_template`, `notification_template_seq`, a primary key, a unique constraint on `(name, competent_authority)`, and trigram indexes for `name` and `workflow`. The data changelog deletes existing rows with the four account-recovery names, regardless of competent authority, before seeding the global templates listed in [README.md](README.md#template-model-and-persistence). Review that replacement behavior when introducing the changelog to a populated database.

Application-specific templates should be owned by the consuming application's Liquibase changelogs. A minimal regulator-specific managed template is:

```sql
INSERT INTO notification_template (
    id,
    name,
    subject,
    text,
    competent_authority,
    event_trigger,
    workflow,
    role_type,
    is_managed
) VALUES (
    nextval('notification_template_seq'),
    'APPLICATION_RECEIVED',
    'Application ${applicationId} received',
    'Hello ${recipientName}, your application has been received.',
    'ENGLAND',
    'Application submitted',
    'Application workflow',
    'OPERATOR',
    true
);
```

Use `competent_authority = NULL` for a global template. System notifications always perform global lookup; email callers choose global or regulator-specific lookup through the DTO.

PostgreSQL permits multiple `NULL` values through the `(name, competent_authority)` unique constraint. Ensure application migrations create at most one global row for a given name; otherwise the single-result repository lookup is ambiguous.

`is_managed` affects management queries and updates only. Both managed and unmanaged templates can be rendered.

## Configure email

Set `env.isProd` explicitly and configure the `notification` properties plus standard Spring Mail properties:

```properties
env.isProd=false

notification.email.auto-sender=no-reply@example.gov.uk
notification.email.contact-us-link=https://example.gov.uk/contact-us
notification.smtp-headers.email-originator=my-ses-configuration-set

spring.mail.host=localhost
spring.mail.port=1025
spring.mail.protocol=smtp
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

| Property | Behavior |
| --- | --- |
| `env.isProd` | Must be `true` or `false`. `false` prepends a test-system disclaimer; `true` does not. Omitting it leaves no `NotificationEmailService` bean. |
| `notification.email.auto-sender` | Used as the MIME `From` address. |
| `notification.email.contact-us-link` | Part of the validated shared properties contract. This module does not insert it automatically; callers may pass it as a template parameter. |
| `notification.smtp-headers.email-originator` | Optional block. When present and non-empty, its value is sent as `X-SES-CONFIGURATION-SET`. |
| `spring.mail.*` | Standard Spring Boot mail connection, authentication, TLS, and timeout settings used by `JavaMailSender`. |

The nested `notification.email` object validates both `auto-sender` and `contact-us-link` as non-empty. If the optional `smtp-headers` object is bound, `email-originator` must also be non-empty.

When its component is initialized, the library also sets the process-wide Jakarta Mail system property `mail.mime.splitlongparameters=false`. Account for that global setting if the same JVM has unrelated mail integrations.

For local development, point `spring.mail.host` and `spring.mail.port` at a local SMTP capture service and keep `env.isProd=false`.

## Author templates

Subjects and bodies support FreeMarker expressions such as `${recipientName}`. Each render loads the row from the database and processes the subject and body with the supplied parameter map.

Rendering applies these safeguards:

1. FreeMarker class construction through the `?new` built-in is disabled.
2. The processed subject and body are HTML-escaped.
3. Email bodies are parsed as CommonMark Markdown with raw HTML escaped and unsafe URLs sanitized.

`processEmailNotificationTemplate` returns HTML suitable for a MIME HTML body. `processNotificationTemplate`, used by system notifications, does not perform Markdown-to-HTML conversion and returns the escaped processed text.

Template selection is exact:

| Call | Repository key |
| --- | --- |
| Email with a competent authority | `(templateName, competentAuthority)` |
| Email with `competentAuthority = null` | `(templateName, NULL)` |
| System notification | `(templateName, NULL)` |

Supply every referenced FreeMarker parameter. A missing template raises `EMAIL_TEMPLATE_NOT_FOUND`; invalid FreeMarker or a missing required expression raises `EMAIL_TEMPLATE_PROCESSING_FAILED`.

## Send email

Inject the contract interface from `uk-netz-app-api-notificationapi`:

```java
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;

@Service
@RequiredArgsConstructor
class ApplicationReceivedNotifier {

    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;

    void notifyRecipient(String emailAddress, String applicationId, String recipientName) {
        EmailData<EmailNotificationTemplateData> emailData = EmailData.builder()
            .notificationTemplateData(EmailNotificationTemplateData.builder()
                .templateName("APPLICATION_RECEIVED")
                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                .templateParams(Map.of(
                    "applicationId", applicationId,
                    "recipientName", recipientName
                ))
                .build())
            .attachments(Map.of())
            .build();

        notificationEmailService.notifyRecipient(emailData, emailAddress);
    }

    void notifySeveral(EmailData<EmailNotificationTemplateData> emailData) {
        notificationEmailService.notifyRecipients(
            emailData,
            List.of("to@example.gov.uk"),
            List.of("cc@example.gov.uk"),
            List.of("audit@example.gov.uk")
        );
    }
}
```

Attachments are a map from MIME filename to complete file bytes:

```java
Map<String, byte[]> attachments = Map.of(
    "notice.pdf", generatedPdfBytes
);
```

Supply attachments and linked files independently, including both in one email:

```java
EmailData<EmailNotificationTemplateData> emailData = EmailData
    .<EmailNotificationTemplateData>builder()
    .notificationTemplateData(templateData)
    .attachments(Map.of("summary.pdf", summaryPdfBytes))
    .linkedFiles(Map.of("notice.pdf", generatedPdfBytes))
    .build();
```

`attachments` and `linkedFiles` may both be populated. The library never moves a file between them or chooses a
delivery mode based on size. Attachments become MIME parts; linked files are stored and represented only by links in
the HTML body.

An application using `linkedFiles` must provide exactly one
`uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService` bean. Its `storeFiles` operation must
return one `EmailFileLink` per input file and provide complete anonymous URLs. The notification library calls it
before template lookup/rendering, then sorts the results by filename and URL and exposes them to the template.
Applications that use only attachments or no files need no provider and the optional provider is not resolved.

The original three-argument `NotificationEmailServiceImpl` constructor remains available for direct construction and
subclassing. It has no linked-file provider. Use Spring injection or the four-argument constructor when supplying linked
files; Spring selects that constructor and injects an `ObjectProvider` even when no storage bean exists.

### File parameters in email templates

The library adds these generated values to a copy of `templateParams`. When `linkedFiles` is non-empty, generated
values override caller entries with the same names. When `linkedFiles` is empty or null, existing entries are preserved,
including explicit nulls, and only absent keys receive defaults. The caller's map is never modified. Constants are defined by
`uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants`.

| Parameter | Generated value |
| --- | --- |
| `fileLinks` | A list of `EmailFileLink` objects sorted by filename and URL; each exposes `uuid`, `fileName`, and `url`. Empty when no linked files were supplied. |
| `attachmentNames` | A sorted list of MIME attachment filenames, without file contents. Empty when no attachments were supplied. |
| `downloadLink(file, label?)` | A FreeMarker helper that renders one escaped Markdown link. The label defaults to `file.fileName`; an optional second argument supplies custom text. |

Templates control the wording, layout, and placement of all file links. For example:

```ftl
<#if attachmentNames?has_content>
Please also see the attached documents.
</#if>

<#if fileLinks?has_content>
Download your documents:
<#list fileLinks as file>
- ${downloadLink(file)}
</#list>
</#if>

Kind regards,
${competentAuthorityName}
```

Use `${downloadLink(file, "Download this document")}` for a custom label. The helper escapes Markdown punctuation
in labels and URL delimiters while preserving existing percent escapes. Its output goes through the existing
FreeMarker → HTML escaping → Markdown-to-HTML pipeline. Raw HTML anchors are not needed.

There is no automatic heading or appended fallback section. A template that omits the links sends only the content
it renders, even if files were uploaded. Templates used for linked emails must adopt these parameters separately.
Maritime uses the NETZ sender; PMRV has its own sender and needs a later integration to populate this model. This
library change does not modify consumer application code or template migrations.

### Mixed official notices

Consumers of `uk-netz-app-api-request` can pass both document collections to the NETZ `OfficialNoticeSendService`:

```java
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request);
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request, ccRecipients);
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request, ccRecipients, bccRecipients);
```

Both collections contain `FileInfoDTO` values. Existing attachment-only and `sendOfficialNoticeAsLinks` methods
remain available. Content is loaded once per UUID per call. Names must be unique within each collection; the same
name or document may appear in both collections. Template selection and recipient handling are shared across all
variants, so the selected template must describe the chosen delivery mode.

### Built-in storage adapter

The built-in `StoredEmailLinkedFileStorageService` uses the provider-neutral files library. To enable it, explicitly add:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-files</artifactId>
    <version>1.15.0-SNAPSHOT</version>
</dependency>
```

Configure the destination once per application:

```properties
notification.email-link.enabled=true
notification.email-link.container=uk-ets-files
notification.email-link.prefix=installation
notification.email-link.public-base-url=https://files.example.gov.uk
```

The adapter is disabled by default and requires a `FileStorageService` bean when enabled. The container defaults to `uk-ets-files`; the prefix is required. The public URL defaults to `http://uk-ets-files.s3.localhost.localstack.cloud:4566` for local development. Public base URLs must be valid absolute HTTP(S) URIs without user info, query, or fragment; encode spaces as `%20`. The encoded base path is preserved, removing only literal trailing slashes. The returned storage path is encoded once and appended; `/base%2Fsegment` stays encoded, while a literal `%` in a storage path becomes `%25`.

The adapter sanitizes and sorts filenames, supplies attachment disposition and `Cache-Control: no-store`, and creates `EmailFileLink` values from library-assigned UUIDs and returned paths. Storage handles partial upload cleanup; the adapter handles cleanup if constructing links fails. Cleanup failures are suppressed on the original operation failure.

S3 applications also explicitly include `io.awspring.cloud:spring-cloud-aws-starter-s3` with the NETZ-parent-managed version and configure `spring.cloud.aws.*`. The files library registers its S3 implementation when a shared `S3Operations` exists. A consumer-supplied `FileStorageService` overrides it. The notification adapter uses no AWS types and also supports custom providers without AWS configuration. The files library retains its transitive S3 SDK for compatibility; its Spring Cloud AWS starter remains optional.

`EmailLinkProperties` can be discovered by broad configuration-property scanning without the files library.
Storage-destination validation runs when the built-in adapter is created; disabled integration and custom email providers
do not require a storage prefix or the optional files types.

Boot discovers the storage and email adapter auto-configurations through their imports files. Do not component-scan `uk.gov.netz.autoconfigure`. Applications supplying their own `EmailLinkedFileStorageService` need neither the files library nor built-in adapter configuration.

The public base URL must route each appended storage path to the configured container. For S3/CloudFront, `/installation/uuid` must resolve to the corresponding bucket key, with the bucket kept private and CloudFront permitted to read it. Provider permissions must allow upload and cleanup deletion. Applications and infrastructure own DNS, TLS, access controls, encryption, expiry, and lifecycle cleanup; this library provisions none of them.

Returned URLs, UUIDs, and stored paths identify anonymous downloads. The file and email-link types apply no feature-specific redaction to their standard string representations. Successfully stored files remain available after later template lookup/rendering or asynchronous email-delivery failures.

Calling `notifyRecipient` or `notifyRecipients` has split failure semantics:

- linked-file storage runs on the caller's thread before template lookup/rendering; missing/multiple storage providers
  and upload failures propagate before rendering or scheduling email;
- database lookup and template rendering then run on the caller's thread; their `BusinessException` failures propagate
  without scheduling email, and any successfully uploaded files remain stored;
- the constructed email is scheduled with `CompletableFuture.runAsync` on the JVM common pool;
- the method does not return a future or delivery result;
- SMTP and MIME failures are logged and swallowed by `JavaSendEmailServiceImpl`;
- files successfully stored before a later template or SMTP/MIME failure are retained; storage and link-construction
  failures retain their existing best-effort rollback, with no new cleanup callback in the SPI;
- no retry, durable handoff, backpressure, or delivery-status persistence is provided.

Do not interpret a normal return as proof that the SMTP server accepted the message. Avoid unbounded or very large attachments because all attachment content remains in heap memory.

## Send system notifications

`SystemNotificationProcessAndSendService` renders a global template and passes the content plus routing information to `SendSystemNotificationService`:

```java
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.notification.system.SystemNotificationProcessAndSendService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;

@Service
@RequiredArgsConstructor
class AccountMessageNotifier {

    private final SystemNotificationProcessAndSendService notificationService;

    void notifyUser(Long accountId, String userId) {
        notificationService.processAndSend(SystemNotificationInfo.builder()
            .template("ACCOUNT_ACTION_REQUIRED")
            .parameters(Map.of("accountId", accountId))
            .accountId(accountId)
            .receiver(userId)
            .build());
    }
}
```

The library registers a no-op `SendSystemNotificationService` when no other bean implements the interface. If system messages are required, supply an implementation:

```java
import org.springframework.stereotype.Service;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SendSystemNotificationService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;

@Service
class PersistentSystemNotificationSender implements SendSystemNotificationService {

    @Override
    public void send(SystemNotificationInfo info, NotificationContent content) {
        // Persist or route the notification through the application's domain flow.
    }
}
```

The conditional default then backs off. Rendering and the custom sender execute synchronously, so their failures propagate to the caller.

## Query and update managed templates

Use `NotificationTemplateQueryService` and `NotificationTemplateUpdateService` from an application service or controller. This library does not authorize HTTP requests itself.

| Operation | Behavior |
| --- | --- |
| `getNotificationTemplatesBySearchCriteria` | Returns managed templates for one competent authority. Optional role types use exact matching; `term` performs case-insensitive substring matching on name or workflow. Results are ordered by name and use zero-based paging. |
| `getNotificationTemplateInfoDTOById` | Returns summary metadata for any existing template. |
| `getManagedNotificationTemplateById` | Returns subject and body only when the row is managed. |
| `getNotificationTemplateCaById` | Implements the authorization library's competent-authority lookup contract. |
| `updateNotificationTemplate` | Changes subject and body through JPA dirty checking, but only for a managed row. |

Callers must provide a non-null competent authority and `PagingRequest` for search. At an HTTP boundary, validate `NotificationTemplateUpdateDTO`: its subject is required with a maximum of 255 characters, and text is required with a maximum of 10,000 characters.

Missing query/update targets raise `RESOURCE_NOT_FOUND`.

## Integration checklist

- Add the implementation artifact, not only `uk-netz-app-api-notificationapi`.
- Scan `uk.gov.netz.api.notification` components and register `NotificationProperties`.
- Scan the template entity and repository, and enable JPA auditing.
- Install PostgreSQL `pg_trgm` and include both Liquibase changelogs in order.
- Add application-owned templates with the correct name, authority, and FreeMarker parameters.
- Set `env.isProd` explicitly and configure both `notification.email.*` properties.
- Configure and test Spring Mail for each environment.
- If linked files are used, enable the built-in adapter with a `FileStorageService` and `notification.email-link.*`, or provide a custom `EmailLinkedFileStorageService`.
- Update the selected email templates to render `fileLinks` with `downloadLink` and use `attachmentNames` where mixed delivery changes the wording.
- Treat anonymous linked-file URLs as bearer credentials and confirm that infrastructure routes them to the
  intended private stored objects.
- Decide whether fire-and-forget, non-retrying SMTP semantics meet the flow's reliability needs.
- Supply `SendSystemNotificationService` or confirm that no-op system delivery is intentional.
- Add application tests for template rows and their parameter maps; these form a runtime contract that Java compilation cannot check.

## Common integration failures

| Symptom | Likely cause |
| --- | --- |
| No `NotificationEmailService` bean | `env.isProd` is absent or not exactly a supported boolean value, or the notification package is not scanned. |
| No `NotificationProperties` bean | Configuration-property scanning or `@EnableConfigurationProperties` is missing. |
| Template repository/entity startup failure | Repository or entity scanning is missing. |
| Email template not found | The `(name, competent_authority)` pair does not match the database row; check whether the template is global. |
| Template processing failed | A FreeMarker expression is invalid or its parameter is missing. |
| Email call returns but no message arrives | Delivery is asynchronous and SMTP/MIME failures are only logged; inspect application logs and SMTP connectivity. |
| Linked-file email fails before returning | Check the storage provider and template parameters. Upload or template failures prevent SMTP scheduling; successful uploads remain after template failures. |
| Files uploaded but no links appear in the email | The selected template must render `fileLinks`, for example with `${downloadLink(file)}` in a FreeMarker loop; no fallback section is appended. |
| System notification produces no record | The default no-op `SendSystemNotificationService` is active. |
| Liquibase fails on `gin_trgm_ops` | PostgreSQL `pg_trgm` is not installed before the notification DDL. |
| Managed search fails or returns unexpected rows | Check non-null authority/paging, `is_managed`, role filters, and PostgreSQL-specific query support. |
