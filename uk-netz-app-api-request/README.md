# UK NETZ request workflow library

`uk-netz-app-api-request` is a shared Java library for request lifecycles, workflow tasks, assignment, history,
supporting processes, and official notices. It builds a JAR that runs inside a consuming Spring Boot application.
It integrates with Camunda and Flowable and provides no REST controllers or standalone application entrypoint.

This README introduces the architecture and development workflow. Use [CONSUMERS.md](CONSUMERS.md) for application
setup and API examples, and [docs/architecture.md](docs/architecture.md) for persistence, engine routing, and extension details.

## Responsibilities

| Area | Responsibility |
| --- | --- |
| `workflow.request.core` | Request, resource, task, action, history, and note models; creation/query/update services; assignment and validation. |
| `workflow.request.application` | Application-facing task views, dashboard items, authorization adapters, document/attachment association, and lifecycle listeners. |
| `workflow.request.flow.common` | Reusable action handlers, task initialization, ID generation, payload registration, official-notice generation and sending. |
| `workflow.request.flow` | Shared request-for-information, deadline-extension, payment, review, and system-message flow components. |
| `workflow.bpmn` | Engine-neutral operations, engine routing, Camunda/Flowable adapters, handlers, and event listeners. |
| `workflow.payment` | Payment methods, fee data, bank details, and the conditional GOV.UK Pay client. |

Applications provide their business workflow types, payload subtypes, migrations, BPMN deployment configuration,
controllers, and authorization boundaries. Library services depend on the wider NETZ account, user, authorization,
files, and notification integrations; the JAR is not an independently bootable workflow application.

## Runtime architecture

```text
Application action / transactional service
  -> StartProcessRequestService
  -> request ID generation + RequestCreateService
  -> WorkflowService (WorkflowTypeServiceDelegator)
  -> Camunda or Flowable process
  -> task/event listeners
  -> request tasks, payloads, assignment, actions and history
```

New process starts are routed by `workflows-type-service.flowable-workflows`; unlisted types use Camunda. The selected
engine is stored on `Request`. Operations on existing requests and tasks resolve that persisted engine. Changing the
routing list does not move running instances to another engine or disable an engine's bean registration.

The data model uses JPA and PostgreSQL JSONB payloads, including lazy payload fields. Keep Hibernate bytecode enhancement
enabled in the Maven build. QueryDSL and MapStruct generate code during compilation. Jackson subtype names, database
type codes, BPMN task keys, and registered handlers must stay consistent when adding or changing a workflow.

## Official notices and file links

`OfficialNoticeSendService` resolves contacts and competent-authority parameters, loads document bytes through
`FileDocumentStorageService`, and passes `EmailData` to `NotificationEmailService`. It supports attachment-only, link-only,
and mixed delivery, including CC and BCC variants. A per-call cache loads each document UUID once across both collections.
Filename uniqueness is checked separately within both collections before loading any document bytes; the same name
may appear once in each collection.

| NETZ library | Role in document delivery |
| --- | --- |
| `api-request` | Document selection/loading, official-notice recipients, and generic template parameters. |
| `api-notificationapi` | Email DTOs, service interfaces, file-link metadata, and template parameter names. |
| `api-notification` | Optional file-link adapter, template rendering, and SMTP scheduling. |
| `api-files` | Database-backed documents and provider-neutral object storage with optional S3 support. |

Existing attachment-only overloads retain delegation through the four-argument sending method. All delivery modes
use the public recipient-selection method, preserving application subclass overrides.

The notification implementation uploads linked files before template lookup/rendering and exposes `fileLinks`,
`attachmentNames`, and `downloadLink`. Templates control wording and placement; there is no appended fallback list.
Template failures stop email scheduling and retain successfully uploaded files. The request library performs no S3
upload, URL construction, or automatic selection of delivery mode by size.

## Persistence and integration boundaries

Liquibase resources live under [db/migration/changelogs](src/main/resources/db/migration/changelogs). They cover request
tables and seed data, payment tables, and engine schemas. Consumers assemble these resources with their application
and sibling-library migrations. The Flowable resources use `sch_flowable`; engine configuration and the schema user
must match that layout when those resources are used.

The library depends on notification contracts, not the notification implementation. Applications sending notices must
provide `NotificationEmailService<EmailNotificationTemplateData>` and its required dependencies. Generating documents
also uses `api-documenttemplate`; consuming a stored document does not itself require an S3 provider.

Further details and the extension map are in [docs/architecture.md](docs/architecture.md).

## Development and verification

The current artifact is `1.38.0-SNAPSHOT`, using NETZ parent `1.12.0` and its Java 21 / Spring Boot 3.5.14 baseline.
See [pom.xml](pom.xml) for the complete dependency graph.

| Command | Purpose |
| --- | --- |
| `mvn compile` | Compile and generate mapper/query classes. |
| `mvn test` | Run unit and architecture tests. |
| `mvn test -Dtest=OfficialNoticeSendServiceTest` | Check recipient handling and all document delivery combinations. |
| `mvn clean verify` | Run the complete build, quality checks, and PostgreSQL integration tests. |
| `mvn clean install` | Verify and install the library locally. |

Full verification requires Docker. Install `api-notificationapi:1.4.0-SNAPSHOT` and `api-files:1.15.0-SNAPSHOT` before
building this snapshot. The notification implementation and request library can then be built in either order.

When extending the library, update related request/task/action metadata, payload registration, BPMN handlers, migrations,
and tests together. Preserve consumer-owned flow behavior and update the consumer guide whenever an integration contract changes.
