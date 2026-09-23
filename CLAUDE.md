# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A demo Spring Boot 3 / Java 21 service ("petstore analyzer consumer") that consumes events from
suppliers over a single `POST /api/process/event` endpoint. It accepts and acknowledges an event —
nothing is called downstream and nothing is stored. Its purpose is to demonstrate two things:
a **contract-first** web layer generated from an OpenAPI spec, and the difference between **unit**
and **integrated** test layers. Keep that demo intent in mind — the layered test setup is the point
of the repo, not incidental scaffolding.

## Commands

Maven wrapper is committed (`./mvnw`); `mvn` works too.

```bash
mvn clean install                 # build + unit tests (surefire) + cucumber suite (failsafe)
mvn clean install -DskipTests=true
mvn clean install spring-boot:run # run locally (README); or: mvn -pl backend spring-boot:run
```

Running a subset:

```bash
mvn -pl backend test -Dtest=EventsServiceImplTest                    # single unit test (surefire)
mvn -pl backend verify -Dit.test=RunIntegratedCucumberTest            # only the integrated suite (failsafe)
mvn -pl backend verify -Dit.test=RunIntegratedCucumberTest \
  -Dcucumber.filter.name="Receive event missing eventType"            # single scenario
```

Surefire **excludes** `**/*IntegrationTest.java` and `**/*CucumberTest.java`; failsafe includes only
those. A new cucumber runner must therefore be named `*CucumberTest` or it will never run.

Once running: Swagger UI at `/api`, OpenAPI JSON at `/api-docs`, actuator at `/actuator`.

## Architecture

Everything lives in `backend`; the `api` module contains only the OpenAPI contract
(`api/definition/petstore-analyzer-consumer-api.yaml`) — no Java. The build resolves everything from
Maven Central; no private repository or `~/.m2/settings.xml` credentials are needed.

**Contract-first web layer.** The openapi-generator plugin is configured in `backend/pom.xml` (not in
`api`) and generates *interfaces only* (`interfaceOnly=true`, `skipIfSpecIsUnchanged=true`) into
`backend/target/generated-sources/openapi`, packages `…analyzer.webapi.api` / `…webapi.model`. Adding
or changing an endpoint means: edit the yaml → rebuild → implement/adjust the generated `*Api`
interface in a controller. The request and response models (`EventRequestBody`,
`EventAcceptedResponse`, `ErrorResponse`) are generated — never hand-write them.

**Request flow** (one chain, for the `events` tag):

```
generated EventsApi interface
  → controllers/EventsRestController (extends BaseRestController, holds ProducerTemplate)
  → producerTemplate.requestBody("direct:<operationId>", body, ResponseType.class)
  → routes/EventsRouteBuilder  (Camel direct: route, .process(...) calls the service)
  → service/impl/EventsServiceImpl  (mints eventId + receivedAt)
```

Route URIs are constants in `routes/RouteBuilderConstants` and are named after the spec's
`operationId`s; controllers and route builders must agree on the constant. An operation with query or
path parameters passes them into the route as a `Map<String, Object>` body and unpacks them in the
processor; `processEvent` has none, so the route body is the typed `EventRequestBody`.

**Validation and error handling.** `EventsServiceImpl` accepts an event only when it carries both a
non-empty `eventType` and a non-empty `payload` **object**, and throws
`service/InvalidEventException` otherwise. (`payload` is generated as `Object`, so "is it an object"
is a runtime `instanceof Map` check — the generated `@NotNull` on the body does not cover it.)

`controllers/GlobalExceptionHandler` (`@RestControllerAdvice`) turns exceptions into the contract's
`ErrorResponse`, since the generated `processEvent` signature only permits an `EventAcceptedResponse`
body:

| Exception | Status | `code` |
|---|---|---|
| `InvalidEventException` | 400 | 2000 |
| `MethodArgumentNotValidException` | 400 | 2000 |
| `HttpMessageNotReadableException` (unreadable json) | 400 | 2000 |
| anything else | 500 | 5000 |

`ProducerTemplate` wraps whatever is thrown inside a route in a `CamelExecutionException`, so the
handler unwraps `getCause()` before mapping it. A rejection is an expected outcome, not a delivery
failure, so `EventsRouteBuilder` declares
`onException(InvalidEventException.class).logStackTrace(false).logExhausted(false).handled(false)` —
that keeps Camel's error handler from logging a stack trace on every bad request while still letting
the exception surface to the caller. Keep build and test output pristine.

## Test layers

Two layers, both under `backend/src/test/java/…/analyzer/`:

| Layer | Location | Runner |
|---|---|---|
| unit | `unit/` | surefire |
| integrated | `integrated/`, `features/integrated/` | `RunIntegratedCucumberTest` |

The cucumber layer boots the full app with `@SpringBootTest(webEnvironment = RANDOM_PORT)` from
`IntegratedContainerBootstrapper` (`@CucumberContextConfiguration`, profiles `dev, test, cucumber`,
loading `IntegratedTestConfiguration`) and exercises it **over HTTP** via `TestRestTemplate` — not by
calling services directly. There are no `application-<profile>.yaml` files; the profiles exist purely
for bean selection.

**Cucumber glue conventions.**

- Glue packages are listed explicitly on the runner: `…analyzer.base` plus the layer's own package.
  A new step-def package will not be discovered unless added there.
- Shared steps (all the `When`/`Then` HTTP calls and assertions) live in
  `base/cucumber/steps/PetstoreAnalyzerConsumerStepDefs`. Put anything reusable in `base` so a second
  suite would get it for free.
- Step-def classes extend `BaseCucumberStepDefs` and receive collaborators through **constructor
  injection** (cucumber-spring), including `TestDataHolder` — a `@ScenarioScope` bean from
  `BaseTestConfiguration` that carries the raw `ResponseEntity<String>` from a `When` step to the
  `Then` steps. It is deliberately the unparsed body plus status, so the same steps can assert on a
  `202` `EventAcceptedResponse` and a `400` `ErrorResponse`.
- Do not set an error handler on the `TestRestTemplate`: the rejection scenarios rely on its default
  non-throwing behaviour to assert on a `400` response.
- `Then I expect a "<reason phrase>" response` resolves the phrase (e.g. `Bad Request`) against
  `HttpStatus`, so feature files read in words rather than status codes.
