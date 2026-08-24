# STATE — Buddy API

## Decisions

| ID | Decision | Rationale | Date |
|----|----------|-----------|------|
| AD-001 | Java 21 with Spring Boot 4.1.0 and `spring-boot-starter-webmvc` as the runtime platform | LTS release with virtual threads support, modern language features (records, sealed classes, pattern matching), and the current Spring MVC starter. | 2026-08-12 |
| AD-002 | Domain-Driven package layout under `com.buddy.api.domains.<context>` with layered sub-packages (`.dtos`, `.entities`, `.enums`, `.mappers`, `.repositories`, `.services`, `.specifications`) | Bounded contexts isolate business domains (account, pet, shelter, adoption, terms, authentication, profile, address, image). Each context owns its full vertical slice. | 2026-08-12 |
| AD-003 | Web controllers live in `com.buddy.api.web.<resource>`, separate from domain packages | Clean separation between HTTP concerns and business logic. Controllers handle request/response mapping only. | 2026-08-12 |
| AD-004 | DTOs are Java `record`s — no business or conversion logic inside them | Records enforce immutability and conciseness. Mapping logic belongs exclusively in MapStruct `@Mapper` interfaces with `componentModel = "spring"`. | 2026-08-12 |
| AD-005 | Enum parsing uses `.toUpperCase(Locale.ROOT)` with non-null checks and `UNKNOWN` fallback | Defensive input handling at boundaries prevents NPEs and locale-dependent bugs. | 2026-08-12 |
| AD-006 | Request validation at controller/DTO boundary via Bean Validation (`@NotBlank`, `@PastOrPresent`, `@Unique`) | Fail-fast at the edge; domain services receive already-validated data. | 2026-08-12 |
| AD-007 | Checkstyle (maxWarnings=0), SpotBugs (Effort.MAX + FindSecBugs), PMD 7.22.0 enforced in CI | Zero-tolerance for code quality violations. Pre-commit hooks also run Checkstyle and Gitleaks. | 2026-08-12 |
| AD-008 | Test hierarchy: `units/` (JUnit 5 + Mockito), `integrations/` (SpringBootTest + Testcontainers + WireMock), `builders/` (Test Data Builder pattern), `customverifications/` | Clear separation of test scope. Testcontainers ensure integration tests use real PostgreSQL. WireMock stubs external APIs. | 2026-08-12 |
| AD-009 | PostgreSQL 15 with Flyway migrations under `db/migration/` (naming: `V<date>_<seq>__description.sql`) | Schema versioning via Flyway ensures reproducible database state across environments. | 2026-08-12 |
| AD-010 | Redis for caching, rate limiting, and token blocklist management | In-memory store handles transient state (auth tokens, rate limits) without polluting the relational database. | 2026-08-12 |
| AD-011 | Spring Cloud OpenFeign for external service integration via ACL in `integrations.clients` | Anti-Corruption Layer isolates external API contracts from domain models. External changes don't leak inward. | 2026-08-12 |
| AD-012 | JWT authentication with JJWT 0.13.0, Spring Security filters, and cookie-based token delivery | Stateless auth with secure cookie transport. Token blocklist in Redis enables forced logout. | 2026-08-12 |
| AD-013 | Gitflow branching: `develop` (active), `main` (releases), `release/*`, `hotfix/*`. PRs scoped to single cohesive concern. | Semi-automated release process via GitHub Actions labels. Single-concern PRs simplify review and rollback. | 2026-08-12 |
| AD-014 | Lombok with `addLombokGeneratedAnnotation = true` | Jacoco and static analysis tools skip Lombok-generated code, producing accurate coverage and quality metrics. | 2026-08-12 |
| AD-015 | SpringDoc OpenAPI 2.8.9 for interactive API documentation | Auto-generated Swagger UI from annotations keeps docs synchronized with code. | 2026-08-12 |
| AD-016 | Cross-cutting concerns centralized in `com.buddy.api.commons` (security, cache, exceptions, converters, enums, pagination) | Shared infrastructure code lives in one place. Domain packages never depend on each other — only on `commons`. | 2026-08-12 |
| AD-017 | Rate-limit buckets use independent operation/email and operation/IP keys, with IP buckets disabled when `useIp=false` | Separate dimensions prevent changing one identifier from bypassing the other limit while preserving email-only use cases. | 2026-08-22 |

## Handoff

_No active work session._
