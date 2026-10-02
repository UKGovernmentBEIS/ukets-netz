# Consuming the UK NETZ notification API library

This guide covers the contracts used by application callers and notification-provider implementers. For architecture
and development details, see [README.md](README.md).

## Dependency and implementation selection

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-notificationapi</artifactId>
    <version>1.4.0-SNAPSHOT</version>
</dependency>
```

The API dependency supplies types and interfaces. To send notifications, register compatible implementations:

- Use [uk-netz-app-api-notification](https://github.com/UKGovernment-IDET/uk-netz-app-api-notification) for the standard
  database-backed template renderer and in-process SMTP sender. Follow that library's `CONSUMERS.md` for Spring scanning,
  database migrations, SMTP properties, and file-link adapter configuration. It already depends on this API artifact.
- Supply your own `NotificationEmailService<T>` and `SendEmailService` implementations when your application owns the
  rendering or transport. A service that accepts a custom template-data subtype must handle that subtype's context.

Adding this API dependency alone does not register either sender, a storage provider, or system-notification persistence.

## Send a template-based email

Inject `uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService<EmailNotificationTemplateData>` from the
selected implementation. The following method body assumes a registered `notificationEmailService` and existing template:

```java
EmailData<EmailNotificationTemplateData> data = EmailData.<EmailNotificationTemplateData>builder()
    .notificationTemplateData(EmailNotificationTemplateData.builder()
        .templateName("DOCUMENTS_READY")
        .competentAuthority(CompetentAuthorityEnum.ENGLAND)
        .templateParams(Map.of("recipientName", "Alex"))
        .build())
    .attachments(Map.of("summary.pdf", summaryBytes))
    .linkedFiles(Map.of("notice.pdf", noticeBytes))
    .build();

notificationEmailService.notifyRecipients(
    data,
    List.of("recipient@example.gov.uk"),
    List.of("copy@example.gov.uk"),
    List.of("audit@example.gov.uk")
);
```

`EmailData` and `EmailNotificationTemplateData` are in `uk.gov.netz.api.notificationapi.mail.domain`;
`CompetentAuthorityEnum` is in `uk.gov.netz.api.competentauthority`. The byte arrays contain the complete documents.

For one TO recipient, use `notifyRecipient(data, address)`. `notifyRecipients` also has overloads with only TO, or TO+CC.
Use the default empty maps for an unused file category, or supply non-null maps explicitly. Attachment names and linked
filenames belong to separate maps, so the same name can occur in both. Nothing is automatically converted to a link by size.

## File-link storage providers

Applications sending linked files through the standard NETZ sender need exactly one
`EmailLinkedFileStorageService` bean. The built-in adapter is in the notification implementation; it can use the optional
S3 provider from `api-files`. Applications may instead provide their own implementation of:

```java
List<EmailFileLink> storeFiles(Map<String, byte[]> files);
```

A provider must:

- return an empty list for empty input;
- return exactly one link per entry, sorted by logical filename;
- supply each link's UUID, safe logical filename, and complete anonymous download URI;
- throw a runtime exception if storage fails, with best-effort cleanup of files already created by that call.

This SPI defines no expiry, revocation, authorization, or cleanup callback after successful storage. The standard NETZ
sender retains successful uploads after later template or SMTP/MIME failures. Storage and link-construction failures
inside its built-in adapter keep their own best-effort rollback behavior.

## File parameters in NETZ templates

The standard NETZ sender uploads linked files before template lookup/rendering and adds the following generated values
to a copy of the caller's parameters. When `linkedFiles` is non-empty, these values take precedence over caller entries.
When `linkedFiles` is empty or null, existing caller entries are preserved, including explicit nulls, and defaults are
added only for absent keys. The caller's map is never modified.

| Key | Generated value |
| --- | --- |
| `fileLinks` | `List<EmailFileLink>`, sorted by filename and then URL. Empty when no linked files were supplied. |
| `attachmentNames` | Sorted MIME attachment filenames, without file contents. |
| `downloadLink(file, label?)` | FreeMarker helper producing an escaped Markdown link, using the filename or an optional custom label. |

The key constants are in
[EmailFileTemplateConstants](src/main/java/uk/gov/netz/api/notificationapi/mail/constants/EmailFileTemplateConstants.java).
The API artifact does not implement or automatically inject the helper. Custom senders must arrange their own rendering
integration; the standard implementation's helper is `uk.gov.netz.api.notification.template.DownloadLinkMethod`.

An email body in the standard renderer can contain:

```ftl
Hello ${recipientName},

<#if attachmentNames?has_content>
Please also see the attached documents.
</#if>

<#if fileLinks?has_content>
Your documents are ready:
<#list fileLinks as file>
- ${downloadLink(file)}
</#list>
</#if>
```

Use `${downloadLink(file, "Download this document")}` for a custom label. The template owns all wording and placement;
NETZ does not append a heading or fallback list. A template that omits `fileLinks` can send an email without links even
though the files were uploaded. Consumer templates must adopt the parameters before their flows send linked files.

## Rendered email and delivery behavior

`SendEmailService.sendMail(Email)` accepts an already-rendered subject/body, sender, recipients, and MIME attachments.
Use this lower-level interface when rendering is already handled. There is no linked-file map on `Email` and this
interface does not perform storage or template lookup for you.

With the standard NETZ implementation:

1. Linked-file storage, template lookup, and rendering run synchronously. Failures prevent SMTP scheduling.
2. A template failure leaves successful uploads stored.
3. Only final delivery is scheduled asynchronously. SMTP/MIME errors are logged by the standard sender; no future or
   delivery receipt is returned to the caller, and successful uploads are retained.

Applications using custom implementations must check their chosen delivery semantics.

## Shared email properties

When using the standard sender, register `NotificationProperties` through `@EnableConfigurationProperties` or property
scanning. Its property names are:

```properties
notification.email.auto-sender=no-reply@example.gov.uk
notification.email.contact-us-link=https://example.gov.uk/contact-us
notification.smtp-headers.email-originator=my-ses-configuration-set
```

The SMTP-header block is optional; when configured, its originator must be non-empty. Both fields in the configured
email block must also be non-empty. `env.isProd`, SMTP connection properties, and `notification.email-link.*` belong to
the notification implementation's integration guide rather than this API's registration logic.

## System notifications

`SystemNotificationInfo` carries the template name, parameters, account ID, and receiver. `NotificationContent` contains
the rendered subject and text. A custom `SendSystemNotificationService` implements:

```java
void send(SystemNotificationInfo info, NotificationContent content);
```

The interface supplies no persistence or default behavior. The standard notification library's
`SystemNotificationProcessAndSendService` performs template rendering and then invokes it synchronously; that library
also provides a no-op sender when no custom implementation exists. Applications needing notification records must supply
an implementation that stores or delivers them.
