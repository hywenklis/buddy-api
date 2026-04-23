# AGENTS.md

## Purpose
- This is a Spring Boot 3.5 / Java 21 backend for pet adoption; main entrypoint is `src/main/java/com/buddy/api/BuddyApplication.java`.
- Runtime base path is `/api` (`src/main/resources/application-local.yml`), while controllers map `/v1/...` (e.g. `/v1/accounts/register`).

## Architecture You Must Respect
- Keep changes inside the existing layered-by-domain structure in `src/main/java/com/buddy/api/{web,domains,integrations,commons}`.
- `web` handles HTTP contracts + validation; domain services keep business rules; repositories stay in domain packages.
- External APIs go through `integrations/clients/*` (Feign), never directly from controllers.
- Cross-cutting concerns (security, cache, exceptions, properties) belong in `commons/configurations` and `commons/exceptions`.

## Existing Request Flow Pattern (example)
- Follow: `Controller -> web mapper -> domain DTO -> domain service -> repository`.
- Example chain: `CreateAccountController` -> `AccountMapperRequest` -> `CreateAccount`/`CreateAccountImpl` -> `AccountRepository`.
- Keep DTOs as transport-only; mapping logic belongs in MapStruct mappers (`domains/*/mappers`, `web/*/mappers`).

## Conventions Specific to This Repo
- Prefer `record` request/response models with Jakarta validation annotations (see `web/accounts/requests/AccountRequest.java`, `web/pets/requests/PetRequest.java`).
- Domain errors should extend `DomainException`; API errors are normalized by `web/advice/controller/GlobalExceptionHandler` into `ErrorResponse`.
- Use `EmailAddress` value object (`domains/valueobjects/EmailAddress.java`) instead of raw email strings in domain/repository paths.
- MapStruct is globally configured in `build.gradle` (`-Amapstruct.defaultComponentModel=spring`), so mapper interfaces are Spring beans.

## Security and Auth Expectations
- Security is stateless JWT in `commons/configurations/security/SecurityConfig.java` and `JwtAuthenticationFilter.java`.
- Public endpoints are explicitly allowlisted in `SecurityConfig`; adding new public routes requires updating that matcher list.
- Auth endpoints are in `web/authentication/controllers/AuthController.java`; token lifecycle uses cookies + JWT utilities.

## Integrations and External Dependencies
- Manager integration is Feign-based: `integrations/clients/manager/ManagerClient.java` + `ManagerService.java`.
- Manager token is cached in Redis key `manager-api:token`; token refresh and notification dispatch are handled in `ManagerService`.
- Integration errors are translated via `CustomErrorDecoder` and `ApiClientExecutor` into domain-facing exceptions.

## Local Dev + Infra Workflow
- Start infra via Docker Compose (`compose.yml`): PostgreSQL 15, Redis, WireMock.
- Common commands:
  - `./gradlew bootRun`
  - `./gradlew test`
  - `./gradlew clean build`
  - `./gradlew checkstyleMain spotbugsMain pmdMain`
- Flyway migrations live in `src/main/resources/db/migration` with `VYYYYMMDD_N__description.sql` naming.

## Testing Patterns You Should Follow
- Unit tests: `src/test/java/com/buddy/api/units/...`.
- Integration/API tests: extend `src/test/java/com/buddy/api/integrations/IntegrationTestAbstract.java`.
- Integration tests use Testcontainers (`TestContainersConfig`) + WireMock stubs from `src/test/resources/mappings`.
- Reuse builders/components under `src/test/java/com/buddy/api/{builders,components,customverifications}` to match current test style.

## High-Value Files to Read Before Large Changes
- `build.gradle` (toolchain, quality gates, MapStruct compiler args).
- `src/main/resources/application-local.yml` and `src/test/resources/application-test.yml` (profile behavior).
- `src/main/java/com/buddy/api/commons/configurations/security/SecurityConfig.java` (route protection).
- `src/main/java/com/buddy/api/web/advice/controller/GlobalExceptionHandler.java` (error contract).
- `best_practices.md` (repo-specific review patterns from prior PRs).

