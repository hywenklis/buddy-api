# AGENTS.md

## Purpose
- This is a Spring Boot 3.5 / Java 21 backend for pet adoption; main entrypoint is `src/main/java/com/buddy/api/BuddyApplication.java`.
- Runtime base path is `/api` (`src/main/resources/application-local.yml`), while controllers map `/v1/...` (e.g. `/v1/accounts/register`).
- Before starting a task, check `.agent/agents`, `.agent/skills`, and `.agent/workflows` and pick the most specific specialist or workflow available for the job.

## Architecture You Must Respect
- Keep changes inside the existing layered-by-domain structure in `src/main/java/com/buddy/api/{web,domains,integrations,commons}`; current subpackages also include `commons/configurations/{cache,properties,security/{cookies,jwt,origin},swagger}`, `commons/converters`, `commons/page`, `domains/{address,image,terms}`, and `web/{defaultresponses,terms}`.
- `web` handles HTTP contracts + validation; domain services keep business rules; repositories stay in domain packages.
- External APIs go through `integrations/clients/*` (Feign), never directly from controllers.
- Cross-cutting concerns (security, cache, exceptions, properties) belong in `commons/configurations` and `commons/exceptions`.

## Existing Request Flow Pattern (example)
- Follow: `Controller -> web mapper -> domain DTO -> domain service -> repository`.
- Example chain: `CreateAccountController` -> `AccountMapperRequest` -> `CreateAccount`/`CreateAccountImpl` -> `AccountRepository`.
- Keep DTOs as transport-only; mapping logic belongs in MapStruct mappers (`domains/*/mappers`, `web/*/mappers`).

## Conventions Specific to This Repo
- Prefer `record` request/response models with Jakarta validation annotations (see `web/accounts/requests/AccountRequest.java`, `web/pets/requests/PetRequest.java`).
- Use the default web wrappers in `web/defaultresponses/*` for create/accepted flows (`CreatedSuccessResponse`, `AcceptedSuccessResponse`) instead of ad hoc response payloads.
- Domain errors should extend `DomainException`; API errors are normalized by `web/advice/controller/GlobalExceptionHandler` into `ErrorResponse`.
- Use `EmailAddress` value object (`domains/valueobjects/EmailAddress.java`) instead of raw email strings in domain/repository paths.
- `buddy.rate.limit` config backs Redis rate limiting in `commons/configurations/cache/RateLimitChecker.java` for verification and password-recovery requests; `buddy.security` config backs AES/GCM attribute encryption in `commons/converters/*`.
- MapStruct is globally configured in `build.gradle` (`-Amapstruct.defaultComponentModel=spring`), so mapper interfaces are Spring beans.

## Security and Auth Expectations
- Security is stateless JWT in `commons/configurations/security/SecurityConfig.java` and `JwtAuthenticationFilter.java`.
- Auth endpoints are in `web/authentication/controllers/AuthController.java` (`/v1/auth/login`, `/v1/auth/refresh`, `/v1/auth/logout`); `CookieManager` writes/clears access + refresh cookies, respects client origin (`WEB`, `TOOLS`, `MOBILE`, `UNKNOWN`), and logout blocks both tokens via the JWT blocklist.
- Public endpoints are explicitly allowlisted in `SecurityConfig`; current public routes include `/v1/auth/**`, `/v1/accounts/register`, `/v1/accounts/password/forgot`, `/v1/pets/**`, `/v1/shelters/**`, and `/v1/terms/active`. Keep `/v1/terms/accept` authenticated.

## Integrations and External Dependencies
- Manager integration is Feign-based: `integrations/clients/manager/ManagerClient.java` + `ManagerService.java`.
- Manager token is cached in Redis key `manager-api:token`; token refresh and async notification dispatch are handled in `ManagerService`, and email sending is routed through `domains/account/email/services/impl/EmailSenderImpl.java`.
- Integration errors are translated via `CustomErrorDecoder` and `ApiClientExecutor` into domain-facing exceptions.

## Local Dev + Infra Workflow
- Start infra via Docker Compose (`compose.yml`): PostgreSQL 15, Redis, WireMock.
- Common commands:
  - `./gradlew bootRun`
  - `./gradlew test`
  - `./gradlew clean build`
  - `./gradlew checkstyleMain spotbugsMain pmdMain`
- Flyway migrations live in `src/main/resources/db/migration` with `VYYYYMMDD_N__description.sql` naming.
- Development follows Gitflow: `feature/*` and `fix/*` land in `develop`; PR labels `create-release` and `create-major-release` trigger release branch creation; merging `release/*` or `hotfix/*` into `main` creates tags, opens a sync PR back to `develop`, and triggers the Jenkins deploy.
- Dependabot PRs are auto-updated against `develop` and auto-merged when checks pass via `.github/workflows/auto-update-dependabot.yml`.

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
- `CONTRIBUTING.md` and `.github/workflows/{cicd-pipeline.yml,auto-update-dependabot.yml}` (branching, release, and automation rules).
- `best_practices.md` (repo-specific review patterns from prior PRs).

