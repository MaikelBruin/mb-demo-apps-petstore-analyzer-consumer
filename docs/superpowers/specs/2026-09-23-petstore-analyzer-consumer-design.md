# petstore-analyzer-consumer — design

**Date:** 2026-09-23
**Status:** approved

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
   object).
3. **The petstore client is stripped out entirely** — the generated client dependency, its
   configuration, the Jersey plumbing and the isolated-layer mock scan all go. A consequence the
   owner accepted explicitly: the repository stops demonstrating generated-client consumption and
   mock support, and the isolated and integrated cucumber suites end up exercising identical
   behaviour.
4. **Both cucumber suites are kept** regardless of (3), so the layered test structure survives.
5. **Full rename**, Java packages included, so this project stops colliding with its sibling in
   the local Maven repository.

### Assumptions

These were proposed rather than decided, and are settled here so implementation is unambiguous:

- `eventType` is required; `payload` is optional. `eventType` is therefore the only field whose
  absence produces the spec's `400`.
- Error `code` is `2000` for client errors (the example value in the owner's spec) and `5000` for
  server errors.
- The request schema is named `SupplierEvent`, matching the operation summary "Processes events
  from suppliers".

## The contract

`api/definition/petstore-analyzer-consumer-api.yaml` is edited in two places. The
`processEvent` operation gains a required body:

```yaml
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SupplierEvent'
```

and `components.schemas` gains:

```yaml
    SupplierEvent:
      title: SupplierEvent
      type: object
      required:
        - eventType
      properties:
        eventType:
          type: string
          description: Type of the event
          example: PET_ADDED
        payload:
          type: object
          additionalProperties: true
          description: Free-form event payload
```

`EventAcceptedResponse` and `ErrorResponse` are left exactly as the owner wrote them. Nothing else
in the file changes.

Generated shapes to expect from `interfaceOnly=true` + `useTags=true`: interface `EventsApi` with
method `processEvent`, models `SupplierEvent` (with `payload` as `Map<String, Object>`),
`EventAcceptedResponse` (`UUID eventId`, `OffsetDateTime receivedAt`) and `ErrorResponse`
(`BigDecimal code`, `String message`, `OffsetDateTime receivedAt`). Response models are generated —
never hand-written.

## Request flow

One chain, shaped like the three it replaces:

```
EventsApi (generated interface, tag "events")
  → EventsRestController          implements EventsApi, extends BaseRestController
  → producerTemplate.requestBody(DIRECT_ROUTE_PROCESS_EVENT, supplierEvent, EventAcceptedResponse.class)
  → EventsRouteBuilder            from(direct:processEvent).routeId(...).process(...)
  → ProcessEventServiceImpl       logs the event, mints eventId + receivedAt
  → 202 { eventId, receivedAt }
```

`ProcessEventService.processEvent(SupplierEvent)` returns an `EventAcceptedResponse` whose
`eventId` is `UUID.randomUUID()` and whose `receivedAt` is `OffsetDateTime.now(ZoneOffset.UTC)`.
There is no retry loop, because there is no downstream call to retry.

The route body is the typed `SupplierEvent` rather than a `Map<String, Object>`: the map convention
in this repository exists for unpacking query and path parameters, and this operation has none.

`RouteBuilderConstants` is reduced to one constant:

```java
public static final String DIRECT_ROUTE_PROCESS_EVENT = "direct:processEvent";
```

The controller returns `ResponseEntity.accepted().body(response)` so the status matches the spec's
`202`.

## Error handling

A new `controllers/GlobalExceptionHandler` annotated `@RestControllerAdvice`, returning the
generated `ErrorResponse` with `receivedAt` populated on every branch:

| Exception | Status | `code` |
|---|---|---|
| `MethodArgumentNotValidException` (missing `eventType`) | 400 | 2000 |
| `HttpMessageNotReadableException` (malformed JSON) | 400 | 2000 |
| `Exception` | 500 | 5000 |

Validation reaches the controller because the generator emits `@Valid` on the body parameter when
`jakarta.validation-api` is on the classpath, which it already is.

## Main sources

**New**

- `controllers/EventsRestController.java`
- `controllers/GlobalExceptionHandler.java`
- `routes/EventsRouteBuilder.java`
- `service/ProcessEventService.java`
- `service/impl/ProcessEventServiceImpl.java`

**Kept** (repackaged only): `controllers/BaseRestController.java`,
`routes/BaseRouteBuilder.java`.

**Edited:** `config/ObjectMapperConfiguration.java` — it imports `RFC3339DateFormat` *from the
petstore client jar*, so the `setDateFormat(new RFC3339DateFormat())` call and its import are
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

- `api.defininiton.input.file` → `petstore-analyzer-consumer-api.yaml`. This is the current build
  break: the property still names the deleted `petstore-analyzer-api.yaml`, so the generator fails
  before anything else can.
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

`CLAUDE.md` mentions a `local-deps/petstore-1.0.27.jar` offline copy of the client; that directory
does not exist in this copy, so there is nothing to remove there.

## Rename

Maven coordinates become `mb.demo.applications:petstore-analyzer-consumer` in the root pom and in
both module parents; the root `name` and `description` are updated to describe an event consumer.

Java packages move from `mb.demo.applications.petstore.analyzer.*` to
`mb.demo.applications.petstore.analyzer.consumer.*`, in main *and* test sources.
`PetstoreAnalyzerApplication` → `PetstoreAnalyzerConsumerApplication`, with its
`@OpenAPIDefinition` title and description updated. Generator properties `api.package.api`,
`api.package.model` and `api.package.invoker` follow the same move.

Three places reference package names as *strings* and will not be caught by a rename refactor:
the `GLUE_PROPERTY_NAME` values in `RunIsolatedCucumberTest` and `RunIntegratedCucumberTest`, and
the `mainClass` in `backend/pom.xml`.

## Tests

`TestDataHolder` drops its six petstore response fields and holds a single
`ResponseEntity<String>`, so `Then` steps can assert on status code *and* body — required for the
`400` scenarios, where the body is an `ErrorResponse` rather than an `EventAcceptedResponse`. Step
definitions parse it with the already-injected `ObjectMapper`. The `exception` field is dropped.

Shared steps move from `base/cucumber/steps/PetstoreAnalyzerStepDefs` to
`base/cucumber/steps/EventProcessingStepDefs`: the `When` that POSTs an event, and the `Then`s
asserting status, `eventId`, `receivedAt`, and error `code` / `message`.

The layer-specific `Given`s have nothing left to mock, so they become profile assertions:

- `IsolatedStepDefs` — "the application runs in isolation", asserting the `isolated` profile is
  active. Its `PetApiClient` collaborator is removed.
- `IntegratedStepDefs` — "the application runs with its real dependencies", asserting `isolated`
  is not active. Its `PetApiClient` collaborator is removed, which also removes an existing bug:
  the constructor currently takes `PetApiClient` twice (`petApiClient` and `petApiClient1`) and
  assigns the second.

`IsolatedTestConfiguration` loses `@ComponentScan("mb.demos.openapi.generated.api.client")` but
keeps `@Profile("isolated")`. `IntegratedTestConfiguration` is unchanged apart from its package.
Both bootstrappers keep their profile lists and point at the renamed application class.
`PetstoreAnalyzerApplicationTests` → `PetstoreAnalyzerConsumerApplicationTests`, still just a
context-load test.

Feature files are renamed to `features/isolated/ProcessEventIsolatedTests.feature` and
`features/integrated/ProcessEventIntegratedTests.feature`. These scenarios appear **verbatim in
both**, inside the `### same tests as … ###` banners the repository uses:

1. A valid supplier event is accepted → `202`, `eventId` and `receivedAt` both non-null
2. An event missing `eventType` is rejected → `400`, with `code` and `message` present
3. A malformed JSON body is rejected → `400`

Plus one isolated-only scenario: an event carrying an arbitrary `payload` object is still accepted.

New unit test `unit/ProcessEventServiceImplTest`: a distinct `eventId` on each call, and a
`receivedAt` that is populated. `unit/SampleUnitTest` stays as the surefire smoke test.

Nothing changes in the surefire / failsafe split: a runner must still be named `*CucumberTest`.

## Documentation

`CLAUDE.md` describes the old architecture in detail — the three request chains, the retry-loop
convention, the generated petstore client and its mock support, and the isolated-vs-integrated
distinction resting on that mock. All of it becomes wrong and is rewritten to describe the event
consumer. `README.md` and `TODO.md` get their descriptions brought in line.

## Out of scope

Persisting events, forwarding them anywhere, authentication, and any endpoint not in the contract.
