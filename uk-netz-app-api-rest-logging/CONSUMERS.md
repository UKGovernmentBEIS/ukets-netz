# Consumer guide

This library adds bounded request and response logging to servlet-based Spring
Boot APIs. It requires Java 21, Spring Boot 3.5, Spring MVC, Jakarta Servlet,
Jackson, and Log4j2. It does not provide WebFlux or outbound HTTP logging.

## Add and activate the library

Add the released artifact to the consuming application:

```xml
<dependency>
    <groupId>uk.gov.netz</groupId>
    <artifactId>uk-netz-app-api-rest-logging</artifactId>
    <version>RELEASE_VERSION</version>
</dependency>
```

The library does not use Spring Boot auto-configuration. Include
`uk.gov.netz.api.restlogging` in component scanning; NETZ applications usually
scan all of `uk.gov`:

```java
@SpringBootApplication(scanBasePackages = {"uk.gov", "com.example.application"})
public class Application {
}
```

An application with narrower scanning can import the components explicitly:

```java
@Configuration
@Import({
    RestLoggingConfig.class,
    RestLoggingFilter.class,
    RestLoggingService.class
})
class RestLoggingConfiguration {
}
```

Use Log4j2 and select one of the supplied configurations:

```properties
# Newline-delimited JSON on stdout
logging.config=classpath:log4j2-json.xml

# Readable local console output
# logging.config=classpath:log4j2-local.xml
```

If the application copies and customises either XML file, preserve the
`MaskRewritePolicy` route for `uk.gov` loggers. The JSON configuration also
uses `BoundedRestJsonTemplateLayout` to cap final REST event size.

## Configure request logging

```properties
rest.logging.level=INFO
rest.logging.max-payload-size=1000000B
rest.logging.excluded-uri-patterns=^/actuator(?:/.*)?$,^/v3/api-docs(?:/.*)?$
rest.logging.excluded-totally-uri-patterns=^/downloads/[^/]+/content$
```

| Property | Default | Effect |
| --- | --- | --- |
| `rest.logging.level` | `INFO` | Level used for successful exchanges |
| `rest.logging.max-payload-size` | `1000000B` | Shared request/response retention limit; accepts `0B` through `2147483647B` |
| `rest.logging.excluded-uri-patterns` | empty | Suppress successful entries matching a Java regular expression |
| `rest.logging.excluded-totally-uri-patterns` | empty | Do not wrap or emit matching exchanges; correlation headers are still returned |

Spring Boot relaxed binding also permits environment variables such as
`REST_LOGGING_LEVEL`, `REST_LOGGING_MAX_PAYLOAD_SIZE`,
`REST_LOGGING_EXCLUDED_URI_PATTERNS`, and
`REST_LOGGING_EXCLUDED_TOTALLY_URI_PATTERNS`.

Patterns are matched against the request path with `Matcher.find()`. Use
anchors where exact path or prefix matching matters. Normal exclusions still
capture bodies and still emit 4xx/5xx exchanges at `ERROR`; total exclusions
skip capture and logging for every status. Use total exclusions for streaming,
large, binary, or highly sensitive endpoints.

The JSON layout has a separate final event ceiling:

```text
REST_LOGGING_MAX_RENDERED_EVENT_BYTES=1000000
```

Raising either limit increases heap usage and can allow the `awslogs` driver to
split one JSON line into multiple CloudWatch events.

## Understand capture results

Every emitted request and response contains `payloadCapture` metadata. Its
`status` is one of:

| Status | Meaning |
| --- | --- |
| `COMPLETE` | A complete JSON object was retained and parsed |
| `EMPTY` | No body bytes were observed |
| `TRUNCATED` | The body exceeded the configured retention limit; no prefix is logged |
| `SKIPPED` | Capture was intentionally bypassed or the body was not fully consumed |
| `FAILED` | A complete retained body could not be parsed as a JSON object |

The metadata also reports the observed size, configured limit, content type,
and a reason where applicable. On a returned 4xx/5xx, the filter finishes
reading an incomplete JSON request only when `Content-Length` is known and is
within the payload limit. This improves rejected-request diagnostics but can
delay the response while the remaining declared bytes arrive.

Multipart parsing is never initiated by the main logging path. If the
application requests parts and consumes the selected JSON-compatible part,
that part can be captured. Non-JSON parts are not retained. Responses with
`Content-Disposition` and non-JSON request or response bodies are counted but
not retained.

## Protect sensitive data

The supplied configurations mask configured property names structurally and
case-insensitively. The default masked names are `password`, `token`, `emailToken`,
`invitationToken`, `email`, `name`, `firstName`, `lastName`, `line1`, `line2`,
`city`, `country`, `postcode`, `number`, `phoneNumber`, `phoneNumberCode`,
`mobileNumber`, `mobileNumberCode`, `countryCode`, and `jobTitle`.

Add domain-specific names as `payloadProperty` entries when customising the
Log4j2 XML:

```xml
<MaskRewritePolicy>
    <property name="payloadProperty">password</property>
    <property name="payloadProperty">clientSecret</property>
</MaskRewritePolicy>
```

Authorization, proxy-authenticate, proxy-authorization, cookie, and set-cookie
headers are always redacted. Query parameter names and values remain visible
in the bounded URI and are not structurally masked, so applications must not
put secrets or personal data in URLs.

Malformed request JSON on error responses may be included as
`payload.rawBody` after best-effort text masking. Because malformed syntax
cannot be traversed structurally, use a total exclusion when an endpoint can
carry data that must never reach logs.

## Validate an integration

Before deployment, verify that the consuming application:

1. Emits exactly one `REQUEST` and one `RESPONSE` entry for a normal exchange.
2. Returns an incoming `Correlation-Id`, or creates one when absent.
3. Redacts a representative domain-specific sensitive property and the
   authorization/cookie headers.
4. Totally excludes streaming, download, and highly sensitive endpoints.
5. Produces metadata-only entries for bodies above the configured limit.
6. Uses a rendered-event limit appropriate for its stdout log transport.

See the [README](README.md) for the implementation overview, public
compatibility notes, build commands, and benchmark instructions.
