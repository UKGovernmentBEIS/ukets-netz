# NETZ REST Logging

Shared request and response logging for servlet-based Spring Boot APIs.

The library:

- logs a <code>REQUEST</code> and <code>RESPONSE</code> entry for each included
  HTTP exchange;
- propagates or creates a <code>Correlation-Id</code>;
- propagates an optional <code>Correlation-Parent-Id</code>;
- captures headers and JSON object payloads;
- logs HTTP errors at <code>ERROR</code>;
- supports URI exclusions; and
- provides Log4j2 configurations with sensitive-field masking.

It is built for Java 21, Spring Boot 3.5, Spring MVC, Jakarta Servlet, Jackson,
and Log4j2. It does not support WebFlux or outbound HTTP logging.

## Documentation

See the [consumer guide](CONSUMERS.md) for dependency setup, Spring component
registration, configuration properties, exclusions, payload behavior, and
sensitive-data guidance.

## Architecture

All library types are under <code>uk.gov.netz.api.restlogging</code>.

| Component | Responsibility |
| --- | --- |
| <code>RestLoggingConfig</code> | Registers <code>RestLoggingProperties</code> under <code>rest.logging</code> |
| <code>RestLoggingFilter</code> | Resolves correlation headers, applies total exclusions, and wraps synchronous servlet exchanges |
| Bounded request/response wrappers | Observe payload bytes without delaying normal response streaming or making requests replayable |
| <code>RestLoggingService</code> | Builds and emits the request entry followed by the response entry |
| <code>MaskRewritePolicy</code> | Redacts configured keys and fails closed when a message cannot be masked safely |
| <code>BoundedRestJsonTemplateLayout</code> | Renders REST entries as bounded UTF-8 JSON lines |

For an included synchronous exchange, the filter:

1. Reuses incoming correlation headers, then existing response values, and
   generates a UUID when <code>Correlation-Id</code> is absent.
2. Passes totally excluded paths through without capture while still returning
   correlation headers.
3. Installs bounded pass-through wrappers and invokes the downstream chain.
4. For a returned 4xx/5xx, completes a partially consumed JSON request only
   when its declared length is known and within the payload limit.
5. Emits <code>REQUEST</code> and <code>RESPONSE</code> entries. Errors use
   <code>ERROR</code>; other responses use the configured level unless normally
   excluded.
6. Releases retained payload buffers after the synchronous log call.

If the downstream chain throws, the normal request/response pair is not
emitted. Async and streaming responses are not reliably captured and should be
totally excluded. The filter has no explicit order, so consuming applications
control its position relative to Spring Security.

## Bounded capture and log entries

Request bytes are observed as application code consumes the servlet input
stream. Response bytes are forwarded directly to the client while being
observed. Complete JSON objects within the configured limit are parsed with the
application's Jackson <code>ObjectMapper</code>. Oversized, incomplete,
non-JSON, and file content produces an empty payload with capture metadata;
oversized prefixes are released rather than logged or retained.

Multipart parsing is not initiated by the main logging path. When application
code requests parts and consumes the selected JSON-compatible part, that part
can be captured. Non-JSON parts are never retained. The deprecated direct-call
path preserves eager extraction of the first JSON-compatible part for source
compatibility.

Each entry contains its type, correlation identifiers, bounded headers,
payload and capture metadata, URI, and user ID. Requests also contain the HTTP
method and start timestamp; responses contain the HTTP status and elapsed time.
Raw query names and values are included in the bounded URI.

The JSON configuration limits retained payloads and final rendered REST
events independently. If a rendered entry is too large, the layout removes
headers and payload, retains identifying and capture metadata, and adds
<code>logEventCapture.reason=LOG_EVENT_SIZE_LIMIT</code>. Generic application
messages are not compacted by this layout.

## Public compatibility surface

Spring applications normally consume <code>RestLoggingConfig</code>,
<code>RestLoggingFilter</code>, and <code>RestLoggingService</code> through
component scanning. <code>RestLoggingEntry</code> is the logged data model, and
<code>RestLoggingProperties</code> exposes the bound configuration.

<code>RestLoggingUtils</code> exposes correlation-header constants, regular
expression URI matching, JSON and multipart content-type checks, and legacy
byte-array payload parsing. The Log4j2 plugins are public so Log4j2 can create
them from XML configuration.

<code>MultiReadHttpServletRequestWrapper</code> is deprecated but remains for
direct callers of the public <code>RestLoggingService.log(...)</code> overloads.
It caches the complete request body and is not used by the main filter. Direct
callers remain responsible for wrapper creation, correlation handling,
exclusions, duplicate prevention, and copying a cached response body.

## Log output and transport

The supplied <code>log4j2-json.xml</code> writes newline-delimited JSON to
standard output; <code>log4j2-local.xml</code> provides readable local output.
The library does not call CloudWatch Logs and contains no CloudWatch SDK client,
OTLP exporter, FireLens router, or telemetry sidecar. In NETZ ECS deployments,
the container logging driver owns buffering, authentication, batching, and
delivery after Log4j writes to standard output.

## Build

~~~bash
mvn test
mvn clean verify
~~~

<code>mvn test</code> runs the Docker-free unit suite. <code>mvn verify</code>
also builds a temporary Java 21 container containing a dummy REST application
and runs black-box small and 20 MiB request/response logging tests. Docker is
required for verification. The temporary container and image are removed by
Testcontainers.

## Masking allocation benchmark

The normal Maven build compiles a JMH benchmark for the structural REST masker
and its JSON-text fallback. A unit smoke test also runs both benchmark fixtures
during <code>mvn verify</code>. This keeps the benchmark compatible with code
changes without adding environment-dependent performance assertions to the
standard build. Both 16 KiB and near-limit 180 KiB payloads are covered. The
benchmark resets sensitive map values after every invocation so the optimized
in-place traversal always processes an unmasked payload.

Build and run it directly with allocation reporting:

~~~bash
mvn -Pjmh -DskipTests clean package
java -cp 'target/test-classes:target/classes:target/jmh-libs/*' \
    org.openjdk.jmh.Main MaskRewritePolicyBenchmark -prof gc
~~~

For constrained resource limits, use the Docker runner. It defaults to 1 CPU,
2 GiB container memory, a 512 MiB initial heap, and a 1 GiB maximum heap. Swap
is disabled by setting the memory+swap limit equal to the memory limit.

~~~bash
./scripts/run-masking-benchmark.sh
~~~

Resource settings can be overridden to match a target runtime:

~~~bash
BENCHMARK_CPUS=2 BENCHMARK_MEMORY=4g \
BENCHMARK_XMS=2432m BENCHMARK_XMX=2432m \
./scripts/run-masking-benchmark.sh
~~~

Extra arguments are passed to JMH; for example,
<code>./scripts/run-masking-benchmark.sh -f 1 -wi 1 -i 2</code> provides a
short smoke run. JSON results are written to
<code>target/jmh-results/masking-benchmark.json</code>.

Docker enforces the CPU quota and memory ceiling, but it cannot reproduce a
deployment host's processor model, memory bandwidth, or neighbouring workload. Treat
<code>gc.alloc.rate.norm</code> (bytes allocated per operation) as the most
portable assurance metric; use timing results primarily for comparisons made
on the same host and runner configuration.
