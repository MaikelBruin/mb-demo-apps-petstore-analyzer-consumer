# petstore-analyzer-consumer — design

**Date:** 2026-09-23
**Status:** approved, then amended on 2026-09-23 to match what was built — see *Amendments*.

## Intent

This repository is a copy of `mb-demo-applications-petstore-analyzer` (which still exists as a
sibling directory and keeps the analyzer behaviour). The copy is being turned into a different
application: an **event consumer**. Its contract,
`api/definition/petstore-analyzer-consumer-api.yaml`, has already been rewritten by hand to a
single operation, and the application must now be made to match it.

Success means: `mvn clean install` passes, the only HTTP endpoint is
`POST /api/process/event`, no code remains that serves the old totals / ratios / available
endpoints, and the build no longer requires GitHub Packages credentials.

### Decisions taken by the repository owner

1. **Accept-and-acknowledge only.** Processing an event mints an id and a timestamp and returns
   `202`. Nothing is called downstream and nothing is stored.
2. **The event carries a request body.** The hand-written spec declared no `requestBody`; that was
   an oversight. The body has exactly two properties — `eventType` (string) and `payload` (any
   object), **both required**.
3. **The petstore client is stripped out entirely** — the generated client dependency, its
   configuration and the Jersey plumbing all go. A consequence the owner accepted explicitly: the
   repository stops demonstrating generated-client consumption and mock support.
4. **Two test layers, unit and integrated — not three.** With no downstream client there is nothing
   left to stub, so a stub-backed cucumber suite would exercise behaviour identical to the integrated
   one.
5. **An event is accepted only when both fields carry content** — a non-empty `eventType` *and* a
   non-empty `payload` object. Anything else is a `400`.

### Amendments

Recorded after implementation, so this document describes what exists:

- The request schema is named **`EventRequestBody`** (the owner's wording), not `SupplierEvent`.
- The service is `EventsService` / `EventsServiceImpl`, not `ProcessEventService`.
- Both body properties are required. The originally-assumed "`payload` optional" is wrong.
- Java packages were **not** renamed; they remain `mb.demo.applications.petstore.analyzer.*`. Only
  the Maven coordinates, `finalName` and the application class carry the `consumer` suffix.
- The contract documents a `500` branch as well as `202` and `400`.

## The contract

`api/definition/petstore-analyzer-consumer-api.yaml`. The `processEvent` operation takes a required
body and declares three responses: `202` → `EventAcceptedResponse`, `400` and `500` →
`ErrorResponse`. The request schema:

```yaml
    EventRequestBody:
      title: EventRequestBody
      type: object
      required:
        - eventType
        - payload
      properties:
        eventType:
          type: string
          description: The type of event
          example: ratio
        payload:
          type: object
```

`EventAcceptedResponse` and `ErrorResponse` are exactly as the owner wrote them.

Generated shapes from `interfaceOnly=true` + `useTags=true`: interface `EventsApi` with method
`processEvent`, and models `EventRequestBody` (`String eventType`, **`Object payload`** — a bare
`type: object` with no `additionalProperties` does not generate a `Map`), `EventAcceptedResponse`
(`UUID eventId`, `OffsetDateTime receivedAt`) and `ErrorResponse` (`BigDecimal code`,
`String message`, `OffsetDateTime receivedAt`). Models are generated — never hand-written.

## Request flow

One chain, shaped like the three it replaces:

```
EventsApi (generated interface, tag "events")
  → EventsRestController          implements EventsApi, extends BaseRestController
  → producerTemplate.requestBody(DIRECT_ROUTE_PROCESS_EVENT, eventRequestBody, EventAcceptedResponse.class)
  → EventsRouteBuilder            from(direct:processEvent).routeId(...).process(...)
  → EventsServiceImpl             validates the event, mints eventId + receivedAt
  → 202 { eventId, receivedAt }
```

`EventsService.processEvent(EventRequestBody)` returns an `EventAcceptedResponse` whose `eventId` is
`UUID.randomUUID()` and whose `receivedAt` is `OffsetDateTime.now(ZoneOffset.UTC)`. There is no retry
loop, because there is no downstream call to retry.

The route body is the typed `EventRequestBody` rather than a `Map<String, Object>`: the map convention
in this repository exists for unpacking query and path parameters, and this operation has none.

`RouteBuilderConstants` is reduced to one constant:

```java
public static final String DIRECT_ROUTE_PROCESS_EVENT = "direct:processEvent";
```

The controller returns `ResponseEntity.accepted().body(response)` so the status matches the spec's
`202`.

## Validation and error handling

Validation lives in `EventsServiceImpl`, not in bean validation on the controller. The generated
`@NotNull` annotations cannot express "a non-empty object", and in practice do not fire for this
operation at all; putting the rules in the service also satisfies the requirement that all three
scenarios travel the controller → route → service chain. `service/InvalidEventException` (a
`RuntimeException`) signals a rejection.

`controllers/GlobalExceptionHandler`, annotated `@RestControllerAdvice`, maps exceptions onto the
generated `ErrorResponse` with `receivedAt` populated on every branch. It is required because the
generated `processEvent` signature returns `ResponseEntity<EventAcceptedResponse>` and so cannot carry
an error body itself.

| Exception | Status | `code` |
|---|---|---|
| `InvalidEventException` | 400 | 2000 |
| `MethodArgumentNotValidException` | 400 | 2000 |
| `HttpMessageNotReadableException` (malformed JSON) | 400 | 2000 |
| anything else | 500 | 5000 |

`ProducerTemplate` wraps exceptions thrown inside a route in a `CamelExecutionException`, so the
handler unwraps `getCause()` before mapping. `EventsRouteBuilder` declares
`onException(InvalidEventException.class).logStackTrace(false).logExhausted(false).handled(false)`, so
a rejection does not log a Camel delivery-failure stack trace while still surfacing to the caller.

## Main sources

**New**

- `controllers/EventsRestController.java`
- `controllers/GlobalExceptionHandler.java`
- `routes/EventsRouteBuilder.java`
- `service/EventsService.java`
- `service/InvalidEventException.java`
- `service/impl/EventsServiceImpl.java`

**Kept:** `controllers/BaseRestController.java`, `routes/BaseRouteBuilder.java`.

**Edited:** `config/ObjectMapperConfiguration.java` — it imported `RFC3339DateFormat` *from the
generated client jar*, so the `setDateFormat(new RFC3339DateFormat())` call and its import are
dropped. `JavaTimeModule` plus the already-disabled `WRITE_DATES_AS_TIMESTAMPS` handle
`OffsetDateTime` serialisation.

**Deleted**

- `controllers/TotalsRestController.java`, `RatiosRestController.java`, `HasAvailableRestController.java`
- `routes/TotalsRouteBuilder.java`, `RatiosRouteBuilder.java`, `AvailableRouteBuilder.java`
- `service/TotalsService.java`, `RatiosService.java`, `HasAvailableService.java` and all three impls
- `config/clients/PetstoreClientConfiguration.java`
- `utils/JerseyRequestFilter.java`

## Build and configuration

`backend/pom.xml`:

- `api.defininiton.input.file` → `petstore-analyzer-consumer-api.yaml`. Until this was fixed the
  property still named the deleted analyzer spec, so the generator failed before anything else could.
- Remove the `mb.demos.openapi.generated.api.client:petstore` dependency, `jersey-client`,
  `jersey-media-json-jackson`, `jersey-media-multipart` and `scribejava-core` — all of them existed
  only for the generated client.
- Remove the `github` `<repositories>` block; nothing is resolved from it any more, so a clean
  build no longer needs `~/.m2/settings.xml` credentials.
- Keep `jackson-databind-nullable` (harmless, and `openApiNullable=false` means nothing generated
  references it).
- `finalName` → `petstore-analyzer-consumer`; `mainClass` → the renamed application class.

`application.yaml`: drop the whole `services.petstore` block; `spring.application.name` →
`petstore-analyzer-consumer`.

There is no `local-deps/` directory in this copy, so there is no offline copy of the client jar to
remove.

## Rename

Maven coordinates become `mb.demo.applications:petstore-analyzer-consumer` in the root pom and in
both module parents; the root `name` and `description` describe an event consumer.
`PetstoreAnalyzerApplication` → `PetstoreAnalyzerConsumerApplication`, with its `@OpenAPIDefinition`
title and description updated.

Java packages stay at `mb.demo.applications.petstore.analyzer.*` (see *Amendments*). Should they ever
move, three places reference package names as *strings* and will not be caught by a rename refactor:
the `GLUE_PROPERTY_NAME` value in `RunIntegratedCucumberTest`, the generator properties
`api.package.api` / `api.package.model` / `api.package.invoker`, and the `mainClass` in
`backend/pom.xml`.

## Tests

`TestDataHolder` drops its six petstore response fields and holds a single `ResponseEntity<String>`,
so `Then` steps can assert on status code *and* body — required for the `400` scenarios, where the
body is an `ErrorResponse` rather than an `EventAcceptedResponse`. Step definitions parse it with the
already-injected `ObjectMapper`. The `exception` field is dropped.

`BaseCucumberStepDefs` stops setting an `ExtractingResponseErrorHandler` on the `TestRestTemplate`:
with no mappings it delegates to `DefaultResponseErrorHandler` and throws on `4xx`, which would make
the rejection scenarios error instead of assert.

Shared steps live in `base/cucumber/steps/PetstoreAnalyzerConsumerStepDefs`: the `When`s that POST an
event, and the `Then`s asserting status, `eventId` and error `message`. `Then I expect a "<reason
phrase>" response` resolves the phrase against `HttpStatus`, so the feature file reads in words.

`features/integrated/PetstoreAnalyzerConsumerIntegratedTests.feature`, written by the owner, holds
three scenarios:

1. An event with both an `eventType` and a `payload` → `202`, with an id
2. An event missing `eventType` → `400`, with an error message
3. An event missing `payload` → `400`, with an error message

New unit test `unit/EventsServiceImplTest` covers the service's rules directly: a valid event is
accepted, each call mints a distinct `eventId`, and a missing / blank `eventType`, missing / empty /
non-object `payload`, or null event are each rejected. `unit/SampleUnitTest` stays as the surefire
smoke test.

Nothing changes in the surefire / failsafe split: a runner must still be named `*CucumberTest`.

## Documentation

`CLAUDE.md` described the old architecture in detail — the three request chains, the retry-loop
convention, the generated downstream client, and a three-layer test setup resting on a stub of that
client. All of it became wrong and is rewritten to describe the event consumer and its two test
layers. `README.md` gets its description brought in line.

## Out of scope

Persisting events, forwarding them anywhere, authentication, and any endpoint not in the contract.
