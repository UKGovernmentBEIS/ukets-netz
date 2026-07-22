# Repository Guidelines

## Project Structure & Module Organization

This is a Java 21 Maven multi-module library, not a runnable application. The root `pom.xml` defines the reactor and shared Checkstyle configuration.

- `uk-netz-app-api-doc-generator-client-core/`: provider-neutral client API, orchestration, Kafka result consumer, storage and queue contracts, and Spring Boot auto-configuration.
- `uk-netz-app-api-doc-generator-client-aws/`: AWS adapter implementations for S3 and SQS using Spring Cloud AWS.
- `src/main/java/`: production Java sources in each module.
- `src/test/java/`: unit, integration, and architecture tests. Integration tests use `*IT.java`.
- `src/main/resources/META-INF/spring/`: Spring Boot auto-configuration imports.
- `README.md` and `CONSUMERS.md`: architecture, configuration, and consumer usage docs.

## Build, Test, and Development Commands

Use local Maven; this repository does not include a Maven wrapper.

- `mvn clean verify`: main verification command. Builds all modules, runs unit tests, Failsafe integration tests, and configured quality checks.
When you need to run this command, ask user permission because the sandbox does not have access to docker for running the IT tests.
** DO NOT ** neglect to run it when you deem it fit
- `mvn -pl uk-netz-app-api-doc-generator-client-core test`: run core module tests only.
- `mvn -pl uk-netz-app-api-doc-generator-client-aws test`: run AWS adapter tests only.
- `mvn -pl <module> -Dtest=ClassNameTest test`: run one JUnit test class.

Integration tests require a working container runtime because Kafka and LocalStack coverage use Testcontainers.

## Coding Style & Naming Conventions

Follow the repository Checkstyle rules in `checkstyle-uknetz.xml`, based on Google Java Style. Use four-space indentation, no tab characters, alphabetical import groups, no wildcard imports, and a 200-character Java line limit. Keep Java packages under `uk.gov.netz.docgenerator.client`. Name unit tests `*Test` and integration tests `*IT`.

Use Lombok consistently with existing classes. `lombok.config` adds `@Generated` metadata for Lombok-generated code.

## Testing Guidelines

Tests use JUnit Jupiter through Spring Boot test support. The suite also uses Spring Kafka test utilities, Testcontainers, LocalStack, and ArchUnit. Add focused unit tests for new behavior and integration tests when changing Kafka, storage, queue, or auto-configuration wiring. Keep provider-specific behavior in adapter modules; the existing architecture test protects the core module from AWS/Spring Cloud AWS dependencies.

## Documentation

With any code changes, update this file, README.md and CONSUMERS.md if necessary to reflect new state.
Pay particular attention into updating documentation inside `docs` directory if appropriate.
