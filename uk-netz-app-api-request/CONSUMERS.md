# Consuming the UK NETZ request workflow library

This guide covers integrating the request library into a backend application. For developer and architect context,
see [README.md](README.md) and [docs/architecture.md](docs/architecture.md).

## Dependency

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-request</artifactId>
    <version>1.38.0-SNAPSHOT</version>
</dependency>
```

The current snapshot depends on `api-notificationapi:1.4.0-SNAPSHOT` and `api-files:1.15.0-SNAPSHOT`, alongside the wider
NETZ account, authorization, user, token, document-template, and integration libraries. Install the two feature snapshots
first for local development. The request library does not bring in `api-notification` as its email implementation.

## Spring integration

Add the library's packages to the application's existing registrations:

- Component discovery: `uk.gov.netz.api.workflow`, together with the sibling-library service packages your application uses.
- JPA entity discovery: request core entities under `uk.gov.netz.api.workflow.request.core.domain` and payment entities
  under `uk.gov.netz.api.workflow.payment.domain`, plus the related NETZ entity packages.
- Spring Data repository discovery: `uk.gov.netz.api.workflow`, including `WorkflowTypeProvider` and the request/payment repositories.
- Configuration-property discovery: `uk.gov.netz.api.workflow`; this includes workflow routing, feature flags, Flowable
  job settings, and the conditional GOV.UK Pay properties.
- Enable JPA auditing, transaction management, and the shared NETZ Jackson subtype-provider configuration.

Applications already scanning these packages should keep one consistent registration rather than duplicating repository
or bean definitions. Configure a PostgreSQL datasource and the engine services needed by the discovered adapters. Default
workflow wiring injects both Camunda and Flowable adapters; the routing list alone does not make either adapter optional.

## Database and process resources

Include the appropriate classpath changelogs in the application's Liquibase master:

| Resource under `db/migration/changelogs/` | Purpose and dependency |
| --- | --- |
| `request_ddl.xml` | Request, task, action, resource, history, type, and note tables. |
| `request.xml` | Shared system-message request-type seed data; include after request DDL. |
| `request_payment_ddl.xml` | Payment tables referencing request types; include after request DDL. |
| `camunda_schema.xml` | Camunda schema creation and supplied upgrades. |
| `flowable_create_schema.xml` | Creates `sch_flowable` and grants access using the `spring-db-user` changelog parameter. |
| `flowable_ddl.xml` | Flowable tables and upgrades; include after its schema creation. |

For example, the request-specific part of a master changelog contains:

```xml
<include file="db/migration/changelogs/request_ddl.xml" />
<include file="db/migration/changelogs/request.xml" />
<include file="db/migration/changelogs/request_payment_ddl.xml" />
```

Assemble engine and sibling-library migrations according to the application's installed schema and enabled services.
Do not include the same classpath resource twice. Configure the `migrate` context where required by the DDL. Application
migrations must supply workflow-specific request/task/action types and their relationships, as well as document and email templates.

Deploy the shared and application BPMN definitions required by your flows from the
[workflows](src/main/resources/workflows) resources. Configure deployment and engine schema management consistently with
Liquibase. The supplied Flowable startup configurer orders the `processEngine` bean after a bean named `liquibase` when both exist.

## Engine and feature configuration

```properties
workflows-type-service.flowable-workflows=EXAMPLE_REQUEST
flowable.process.enabled=true
flowable-db.schema=sch_flowable
flowable-job.core-pool-size=4
flowable-job.max-pool-size=4
flowable-job.queue-capacity=100
```

`EXAMPLE_REQUEST` is an application-owned request type, not a built-in workflow. The routing set defaults to empty, so
new starts use Camunda unless their type is listed. `startProcessDefinitionByKey` compares the process-definition key
against the same set. Existing request/task operations use the engine saved with the request.

The Flowable settings above activate the library's engine/schema and job configuration; they do not define your complete
datasource or engine deployment configuration. When using the supplied migrations, use `sch_flowable` consistently.

`feature-flag.disabled-workflows` defaults to empty and is read by `EnabledWorkflowValidator.isWorkflowEnabled`.
Application action/availability logic must call the appropriate validation; this list does not migrate or terminate
running processes.

For card-payment flows, enable `govuk-pay.isActive=true` and configure `govuk-pay.service-url`,
`govuk-pay.confirmation-return-url`, and `govuk-pay.api-keys.<authority>`. API-key map entries use lower-case competent-authority
names, for example `england`. Supply application payment-method, fee, and bank-detail data for the payment modes you use.

## Register application workflows

For each application-specific workflow:

1. Seed its request/task/action type codes and associations, and deploy the matching BPMN process definition.
2. Register an ID generator and the applicable request-creation handlers.
3. Register payload subtypes through the NETZ `JsonSubTypesProvider` mechanism; the `payloadType` discriminator must match.
4. Register `InitializeRequestTaskHandler` implementations for tasks requiring an initial payload and
   `RequestTaskActionHandler<T>` implementations for their supported action codes.
5. Add the application validation, authorization, templates, and document-generation integration required by the flow.

The handler mapper selects the first matching action handler, so each action code should have one intended handler.
Missing task initializers produce a null initial payload; flows requiring data should register their initializer explicitly.
See the [extension map](docs/architecture.md#extension-map) for the supporting interfaces.

## Start and operate a request

Use `StartProcessRequestService` from an application transaction. It generates the request ID, creates the request,
builds process variables, starts the selected engine, and associates the process-instance ID with the request.

```java
@Transactional
public Request startExample(Long accountId, RequestPayload payload) {
    RequestParams params = RequestParams.builder()
        .type("EXAMPLE_REQUEST")
        .requestPayload(payload)
        .requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
        .processVars(Map.of())
        .build();
    return startProcessRequestService.startProcess(params);
}
```

This method belongs to an application service with an injected `StartProcessRequestService`, after the example type,
payload subtype, ID generator, account services, and process definition have been registered. `RequestParams` is in
`workflow.request.flow.common.domain.dto`; the request and payload types are in `workflow.request.core.domain` under
`uk.gov.netz.api`. `ResourceType` comes from `uk.gov.netz.api.authorization.rules.domain`.

Inject the neutral `WorkflowService` to complete tasks, send events, access variables, or delete process instances.
Its request/task identifiers must refer to persisted records so the delegator can resolve the correct engine. Direct
`startProcessDefinition(request, variables)` calls need a non-null, mutable variable map containing `requestId`: the
adapters insert a `businessKey` derived as `bk` plus that ID. The higher-level start service prepares that map for you.

## Send official notices

Inject `uk.gov.netz.api.workflow.request.flow.common.service.notification.OfficialNoticeSendService`. Each
`FileInfoDTO` identifies a stored document by UUID and provides its logical email filename.

```java
officialNoticeSendService.sendOfficialNotice(attachments, request);
officialNoticeSendService.sendOfficialNoticeAsLinks(linkedFiles, request);
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request);
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request, ccRecipients);
officialNoticeSendService.sendOfficialNotice(attachments, linkedFiles, request, ccRecipients, bccRecipients);
```

Both file collections are `List<FileInfoDTO>`. Each may be empty, and the same document or filename may appear in both.
Names must be unique within each collection, using case-sensitive comparison. Both collections are checked before
loading any document bytes; duplicate names throw `IllegalStateException`, even when they refer to the same UUID.
Document bytes are loaded once per UUID for the invocation and put into the independent `EmailData` maps.

The service requires the request's primary and service contacts. It removes TO addresses from CC, passes BCC through,
and selects the `Generic email template` using the request's competent authority. Contact and competent-authority
parameters are supplied by the service; templates and documents must already exist in the consuming application.
The existing two- and three-argument overloads delegate through the existing four-argument method, so delivery
overrides remain effective. All modes use `getOfficialNoticeToRecipients(Request)`, preserving custom recipient selection
and applying CC deduplication to those selected addresses.

### Email implementation and linked-file setup

Provide `NotificationEmailService<EmailNotificationTemplateData>`. For the standard implementation, add
[uk-netz-app-api-notification](https://github.com/UKGovernment-IDET/uk-netz-app-api-notification), currently `1.8.0-SNAPSHOT`,
and follow its `CONSUMERS.md` for scanning, templates, SMTP, and `notification.email-link.*` configuration. S3 users also
add the optional Spring Cloud AWS S3 starter described by the files library. An attachment-only flow needs no object-storage provider.

The template receives `fileLinks`, `attachmentNames`, and `downloadLink(file, label?)`. It must describe the chosen
delivery mode and explicitly render any links. The request library does not rewrite the template's wording or append
a link section; the notification guide contains a complete mixed-file FreeMarker example. The standard sender preserves
existing caller values for these keys when no linked files are supplied and fills only missing defaults; generated metadata
takes precedence when linked files are present.

Missing contacts, missing documents, and duplicate filenames fail before notification dispatch. In the standard sender,
storage happens before template lookup/rendering; either failure prevents SMTP scheduling. Successful uploads remain
after template or later SMTP/MIME failures. Storage and link-construction failures retain their own best-effort rollback.

Existing application flows retain their chosen methods. These NETZ APIs do not automatically adopt links in RFI/RDE or
other consumer flows. Applications with their own sender or notice service, such as PMRV, require separate integration.

## Integration checks

- Confirm type metadata, JSON subtype registration, BPMN keys, handlers, and engine routing agree.
- Verify migrations and engine startup against the application's PostgreSQL schema.
- Exercise task creation, action handling, assignment, and the relevant supporting processes in the consuming application.
- Test all chosen file-delivery combinations with real templates and an SMTP capture transport.
- Keep object lifecycle cleanup and delivery monitoring in the application's operational setup.
