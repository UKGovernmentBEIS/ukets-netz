# Request workflow architecture

This reference expands the [README](../README.md). Application integration steps and calling examples are in
[CONSUMERS.md](../CONSUMERS.md).

## Request and task persistence

The central [Request](../src/main/java/uk/gov/netz/api/workflow/request/core/domain/Request.java) entity carries its type,
status, engine, process-instance ID, timestamps, payload, metadata, and associated resources. Related entities represent
active tasks, task history, actions, notes, and task visits.

Account, competent-authority, and verification-body associations are represented through `RequestResource`. The request's
accessor methods derive those identifiers from the resource list. Request/task/action type entities describe available
workflows and actions; application seed data supplies the business-specific codes and relationships.

Payloads use PostgreSQL JSONB and Jackson's named `payloadType` discriminator. Request and task payload base classes
carry shared workflow state while application subtypes supply domain data. Keep subtype names compatible with stored
JSON and register new names through NETZ's `JsonSubTypesProvider` mechanism. Hibernate bytecode enhancement supports
the lazy payload fields; MapStruct and QueryDSL generated code supports mapping and queries.

`RequestService` and `RequestTaskService` expose normal lookups and lookups for update. Transactional application actions
coordinate mutations, engine operations, and persistence. `StartProcessRequestService` delegates creation and then sets
the process-instance ID on the resulting entity; it is intended to run within the caller's transaction.

## Engine selection and lifecycle

[WorkflowTypeServiceDelegator](../src/main/java/uk/gov/netz/api/workflow/bpmn/WorkflowTypeServiceDelegator.java) is the
primary `WorkflowService` implementation. It depends on both engine adapters and the repository-backed `WorkflowTypeProvider`.

| Operation | Routing source |
| --- | --- |
| Start a request | Request type code in `workflows-type-service.flowable-workflows`; otherwise Camunda. |
| Start directly by process-definition key | That key in the same configuration set. |
| Restart a request | The request's persisted engine. |
| Complete a task | Engine resolved from its persisted process-task ID. |
| Send a request event | Engine resolved from the persisted request ID. |
| Get/set variables or delete a process instance | Engine resolved from its persisted process-instance ID. |

Each adapter translates the neutral operations into engine-specific calls. Requests use business keys prefixed with
`bk`. Engine event/task listeners connect BPMN lifecycle events back to request services and task handlers. The Flowable
adapter registers its discovered `FlowableEventListener` beans with the runtime service during initialization.

Camunda and Flowable have different concrete APIs and event behavior; a shared interface does not migrate running engine
state. Keep the persisted engine consistent with the actual process instance. The Flowable-specific operation for sending
events to processes containing a variable is an adapter extension and is not part of `WorkflowService`.

When Flowable process support is explicitly enabled, the library configures its datasource/transaction manager and schema
prefix, supplies a job executor, and orders a `processEngine` bean after a `liquibase` bean if both definitions exist.
The bundled Flowable changelogs target `sch_flowable`. Consumer engine configuration must match the deployed database.

## Extension map

| Interface or component | Extension responsibility |
| --- | --- |
| `RequestAccountCreateActionHandler<T>` / `RequestCACreateActionHandler<T>` | Application request creation for account or competent-authority resources. |
| `RequestIdGeneratorResolver` and its registered generators | Select the request-ID strategy for each supported request type. |
| `InitializeRequestTaskHandler` | Build the initial task payload for supported task type codes. |
| `RequestTaskActionHandler<T>` | Process supported task action codes and return the resulting task payload. |
| `RequestTaskActionHandlerMapper` | Resolve the first matching registered action handler; missing matches raise `RESOURCE_NOT_FOUND`. |
| `UserTaskCreatedHandler` and custom/dynamic task handlers | Map engine task creation/deletion to request task behavior. |
| `RequestActionCustomMapperHandler` | Customize action mapping for applicable action types. |
| `JsonSubTypesProvider` implementations | Register request, task, and action payload subtype names. |

The default task-created handler resolves fixed task-definition keys directly and dynamic keys from workflow variables.
It looks up the corresponding `RequestTaskType` and creates a task with any configured expiration date. Initializers are
selected by supported task type; if none matches, the task starts with a null payload. Action and initializer registrations
should avoid overlapping ownership of the same codes.

## Supporting processes and integrations

Shared BPMN resources and handler packages support requests for information, deadline extensions, application review,
peer review, payments, and system messages. The library provides reusable components; consuming applications decide
which processes to deploy and how business workflows invoke them.

- Account/user/authorization services support request resources, contact resolution, assignment, and eligibility.
- File services support task/action attachments, notes, stored documents, and download-token integration.
- Document-template services generate official notices; notification contracts dispatch them.
- Payment services use configured fee/method/bank records. The GOV.UK Pay client and card-payment service are conditional
  on `govuk-pay.isActive=true` and use authority-specific API keys.
- Application-facing query and mapping services provide task views, dashboard items, history, and metadata to controllers
  owned by the consuming backend.

## Official-notice delivery boundary

The [notice sender](../src/main/java/uk/gov/netz/api/workflow/request/flow/common/service/notification/OfficialNoticeSendService.java)
shares contact/template preparation across all delivery methods. After contact/template preparation, it validates filename
uniqueness separately in both collections before reading any document content. It loads each selected document UUID once per invocation,
maps bytes by logical filename separately for attachments and links, and calls `NotificationEmailService`.
Existing short attachment-only overloads dispatch through the four-argument method. Recipient selection dispatches
through the public `getOfficialNoticeToRecipients(Request)` method for all modes, preserving subclass hooks. The default
implementation retains the original separate contact lookups for template parameters and recipient selection.

The request layer is unaware of buckets, object keys, public URL construction, MIME formatting, and SMTP execution.
The standard notification implementation owns those later steps, using the files library through its optional adapter.
In particular, database or workflow transaction rollback does not imply rollback of a successful external upload.

Missing contact/document data and filename collisions stop the notice before notification dispatch. Storage and template
failures stop the standard sender before SMTP scheduling. Template failures retain successful uploads, and asynchronous
SMTP failure has no delivery-result callback for this layer to consume.

## Change and verification boundaries

Adding a workflow requires coordinated type data, payload registration, handlers, BPMN definitions, and application
authorization. Changing the engine routing configuration affects new starts; moving existing instances requires separate
migration work. Database migrations must account for request/payment tables and the chosen engine schemas.

Unit tests cover routing, handlers, request services, and notice delivery preparation. ArchUnit checks enforce package
dependencies. PostgreSQL integration tests exercise repositories and query behavior. Consumer integration tests should
also exercise their assembled Spring context, migrations, BPMN deployments, and application-specific extensions.
