# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A demo Spring Boot 3 / Java 21 service ("petstore analyzer") that aggregates data from the public
Swagger Petstore (`https://petstore3.swagger.io/api/v3`). Its purpose is to demonstrate two things:
consuming a **generated** OpenAPI client, and the difference between **unit / isolated / integrated**
test layers. Keep that demo intent in mind — the layered test setup is the point of the repo, not
incidental scaffolding.

## Commands

Maven wrapper is committed (`./mvnw`); `mvn` works too.

```bash
mvn clean install                 # build + unit tests (surefire) + cucumber suites (failsafe)
mvn clean install -DskipTests=true
mvn clean install spring-boot:run # run locally (README); or: mvn -pl backend spring-boot:run
```

Running a subset:

```bash
mvn -pl backend test -Dtest=SampleUnitTest                          # single unit test (surefire)
mvn -pl backend verify -Dit.test=RunIsolatedCucumberTest             # only the isolated suite (failsafe)
mvn -pl backend verify -Dit.test=RunIntegratedCucumberTest           # only the integrated suite (hits the real petstore)
mvn -pl backend verify -Dit.test=RunIsolatedCucumberTest \
  -Dcucumber.filter.name="Has available rats - no rats available"    # single scenario
```

Surefire **excludes** `**/*IntegrationTest.java` and `**/*CucumberTest.java`; failsafe includes only
those. A new cucumber runner must therefore be named `*CucumberTest` or it will never run.

Once running: Swagger UI at `/api`, OpenAPI JSON at `/api-docs`, actuator at `/actuator`.

## Dependency on the generated petstore client

`mb.demos.openapi.generated.api.client:petstore` is an external artifact from GitHub Packages
(`maven.pkg.github.com/MaikelBruin/mb-pipelines-openapi-generator`), so a `github` server entry with
credentials in `~/.m2/settings.xml` is required for a clean build (CI injects `MB_GITHUB_USERNAME` /
`MB_PAT`). A copy of the jar also sits in `local-deps/petstore-1.0.27.jar` for offline installs.

Besides the client (`PetApi`, `PetApiClient`, models), that jar ships **mock support** used by the
isolated tests: `PetApiMockConfiguration` (a `@Configuration` exposing a `@Primary PetApi` bean),
`PetApiMockProvider` (builds a Mockito **spy** of `PetApiClient` pre-stubbed with defaults) and
`PetApiResponseExamples` (example payloads from the spec).

## Architecture

Everything lives in `backend`; the `api` module contains only the OpenAPI contract
(`api/definition/petstore-analyzer-consumer-api.yaml`) — no Java.

**Contract-first web layer.** The openapi-generator plugin is configured in `backend/pom.xml` (not in
`api`) and generates *interfaces only* (`interfaceOnly=true`, `skipIfSpecIsUnchanged=true`) into
`backend/target/generated-sources/openapi`, packages `…analyzer.webapi.api` / `…webapi.model`. Adding
or changing an endpoint means: edit the yaml → rebuild → implement/adjust the generated `*Api`
interface in a controller. Response models (`TotalResponse`, `AvailabilityRatioResponse`,
`HasAvailableResponse`) are generated — never hand-write them.

**Request flow** (one chain per endpoint group: totals / ratios / available):

```
generated *Api interface
  → controllers/*RestController (extends BaseRestController, holds ProducerTemplate)
  → producerTemplate.requestBody("direct:<operationId>", body, ResponseType.class)
  → routes/*RouteBuilder  (Camel direct: route, .process(...) calls the service)
  → service/impl/*ServiceImpl
  → generated PetApi (Jersey client) → real petstore
```

Route URIs are constants in `routes/RouteBuilderConstants` and are named after the spec's
`operationId`s; controllers and route builders must agree on the constant. Query/path parameters are
passed into the route as a `Map<String, Object>` body and unpacked in the processor.

**Downstream client wiring.** `config/clients/PetstoreClientConfiguration` builds the `PetApi` bean
from `services.petstore.base-url` / `connection-timeout` in `application.yaml`, over a Jersey client
using the shared `ObjectMapper` from `config/ObjectMapperConfiguration` plus a logging
`utils/JerseyRequestFilter`.

**Retry loops.** Every service method wraps its `petApi` call in a hand-rolled retry loop (5 attempts,
2s apart) because the public petstore intermittently returns 500s. This is deliberate — see commits
"debug 500 response from petstore api" and "fix nullpointer when petstore keeps responding with 500".
When adding a service method, follow the same pattern *and* null-check the result after the loop
before dereferencing it.

## Test layers

Three layers, all under `backend/src/test/java/…/analyzer/`:

| Layer | Location | Runner | Downstream petstore |
|---|---|---|---|
| unit | `unit/` | surefire | n/a |
| isolated | `isolated/`, `features/isolated/` | `RunIsolatedCucumberTest` | Mockito spy from the generated jar |
| integrated | `integrated/`, `features/integrated/` | `RunIntegratedCucumberTest` | the real public petstore (network, flaky) |

Both cucumber layers boot the full app with `@SpringBootTest(webEnvironment = RANDOM_PORT)` from a
`*ContainerBootstrapper` (`@CucumberContextConfiguration`) and exercise it **over HTTP** via
`TestRestTemplate` — not by calling services directly.

The only structural difference is the active profiles and configuration class:

- `IsolatedContainerBootstrapper` activates `dev, isolated, test, cucumber` and loads
  `IsolatedTestConfiguration`, which `@ComponentScan`s `mb.demos.openapi.generated.api.client`. That
  scan picks up `PetApiMockConfiguration`, whose `@Primary PetApi` spy bean **overrides** the real one
  from `PetstoreClientConfiguration`.
- `IntegratedContainerBootstrapper` activates `dev, test, cucumber` (no `isolated`), loads
  `IntegratedTestConfiguration`, and so keeps the real client.

There are no `application-<profile>.yaml` files; profiles exist purely for bean selection.

**Cucumber glue conventions.**

- Glue packages are listed explicitly on each runner: `…analyzer.base` plus the layer's own package.
  A new step-def package will not be discovered unless added there.
- Shared steps (all the `When`/`Then` HTTP calls and assertions) live in
  `base/cucumber/steps/PetstoreAnalyzerStepDefs`; layer-specific `Given`s (mock stubbing, "I have
  access to the actual/mocked petstore") live in `isolated/…/IsolatedStepDefs` /
  `integrated/…/IntegratedStepDefs`. Put anything reusable in `base` so both suites get it.
- Step-def classes extend `BaseCucumberStepDefs` and receive collaborators through **constructor
  injection** (cucumber-spring), including `TestDataHolder` — a `@ScenarioScope` bean from
  `BaseTestConfiguration` used to carry the response from a `When` step to the `Then` steps.
- When stubbing in isolated tests, stub the client's `*WithHttpInfo` methods (e.g.
  `petApiClient.findPetsByTagsWithHttpInfo(tags)` returning an `ApiResponse<>`), since the plain
  methods delegate to those on the spy.
- The shared scenarios are duplicated verbatim in both feature files (marked with `### same tests as
  … ###` banners); when changing one, change the other.
