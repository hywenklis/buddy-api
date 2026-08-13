# Reset Password Tasks

## Execution Protocol (MANDATORY -- do not skip)

Implement these tasks with the `tlc-spec-driven` skill: **activate it by name and follow its Execute flow and Critical Rules.** Do not search for skill files by filesystem path. The skill is the source of truth for the full flow (per-task cycle, sub-agent delegation, adequacy review, Verifier, discrimination sensor).

---

**Design**: `.specs/features/reset-password/design.md`
**Status**: Draft

---

## Test Coverage Matrix

> Generated from codebase, project guidelines, and spec - confirm before Execute. Guidelines found: none - strong defaults applied.

| Code Layer | Required Test Type | Coverage Expectation | Location Pattern | Run Command |
| ---------- | ------------------ | -------------------- | ---------------- | ----------- |
| Service | unit | All branches; 1:1 to spec ACs; all listed edge cases | `src/test/java/**/units/**/*.java` | `./gradlew test` |
| Controller | integration | All routes: happy + edge + error | `src/test/java/**/integrations/**/*.java` | `./gradlew test` |
| Utility/Config | unit | All branches | `src/test/java/**/units/**/*.java` | `./gradlew test` |
| Entity/DTO | none | - (build gate only) | - | `./gradlew build` |

## Gate Check Commands

> Generated from codebase - confirm before Execute.

| Gate Level | When to Use | Command |
| ---------- | ----------- | ------- |
| Quick | After tasks with unit tests only | `./gradlew test` |
| Full | After tasks with e2e/integration tests | `./gradlew test` |
| Build | After phase completion or config/entity-only tasks | `./gradlew build` |

---

## Execution Plan

Phases are ordered and run sequentially - each phase completes before the next begins, and tasks within a phase execute in order.

### Phase 1: Foundation (Global Token Revocation)

Tasks that must be done first, in order.

```
T1
T2
T1 → T3
T2 → T3
```

### Phase 2: Core Implementation (Reset Password Endpoint)

Builds on the foundation.

```
T4
T5
T1 → T6
T4 → T6
T5 → T6
T6 → T7
```

---

## Task Breakdown

### T1: [Add Global Token Revocation Support]

**What**: Update `TokenBlocklistService` to support setting a global revocation timestamp for a user.
**Where**: `src/main/java/com/buddy/api/commons/configurations/security/jwt/TokenBlocklistService.java`
**Depends on**: None
**Reuses**: `StringRedisTemplate`
**Requirement**: RESET-03

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] `revokeAllUserTokens(email)` and `isUserTokensRevoked(email, issuedAtTimestamp)` added.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick
**Commit**: `feat(auth): add global token revocation support`

---

### T2: [Add getIssuedAt to JwtUtil]

**What**: Add a method to extract the `issuedAt` claim from a JWT.
**Where**: `src/main/java/com/buddy/api/commons/configurations/security/jwt/JwtUtil.java`
**Depends on**: None
**Reuses**: Existing parseClaims
**Requirement**: RESET-03

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] `getIssuedAtFromToken` method added and verified by unit test.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick
**Commit**: `feat(auth): extract issuedAt from JWT`

---

### T3: [Enforce Global Revocation in AuthServiceImpl]

**What**: Check global token revocation when refreshing tokens.
**Where**: `src/main/java/com/buddy/api/domains/authentication/services/impl/AuthServiceImpl.java`
**Depends on**: T1, T2
**Reuses**: `TokenBlocklistService`, `JwtUtil`
**Requirement**: RESET-03

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] `refreshToken` checks `isUserTokensRevoked` and throws exception if revoked.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass (no silent deletions)

**Tests**: unit
**Gate**: quick
**Commit**: `feat(auth): enforce global token revocation during refresh`

---

### T4: [Create ResetPasswordRequest DTO]

**What**: Create DTO with token and newPassword fields and validation.
**Where**: `src/main/java/com/buddy/api/web/accounts/requests/ResetPasswordRequest.java`
**Depends on**: None
**Reuses**: Existing validation annotations
**Requirement**: RESET-05

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] DTO created with `@NotBlank` and strong password regex.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass

**Tests**: unit
**Gate**: quick
**Commit**: `feat(account): create ResetPasswordRequest DTO`

---

### T5: [Add Token Management to ForgotPasswordTokenManager]

**What**: Add ability to delete tokens and retrieve emails by token.
**Where**: `src/main/java/com/buddy/api/commons/configurations/cache/ForgotPasswordTokenManager.java`
**Depends on**: None
**Reuses**: Existing cache logic
**Requirement**: RESET-01, RESET-02

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] `getEmailByToken` and `deleteToken` added and tested.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass

**Tests**: unit
**Gate**: quick
**Commit**: `feat(account): add read and delete to forgot password cache`

---

### T6: [Implement ResetPasswordService]

**What**: Create service that ties token validation, password update, and global token revocation.
**Where**: `src/main/java/com/buddy/api/domains/account/email/services/impl/ResetPasswordServiceImpl.java`
**Depends on**: T1, T4, T5
**Reuses**: `FindAccount`, `AccountRepository`, `PasswordEncoder`
**Requirement**: RESET-01, RESET-02, RESET-03, RESET-04

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] Validates token exists, updates password, deletes token, revokes refresh tokens.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass

**Tests**: unit
**Gate**: quick
**Commit**: `feat(account): implement ResetPasswordService logic`

---

### T7: [Create ResetPasswordController]

**What**: Create the REST endpoint for reset password.
**Where**: `src/main/java/com/buddy/api/web/accounts/controllers/ResetPasswordController.java`
**Depends on**: T6
**Reuses**: Standard web layer patterns
**Requirement**: RESET-01, RESET-02, RESET-03, RESET-04, RESET-05

**Tools**:
- MCP: `filesystem`
- Skill: NONE

**Done when**:
- [ ] `POST /v1/accounts/password/reset` created and tested with integrations.
- [ ] Gate check passes: `./gradlew test`
- [ ] Test count: Tests pass

**Tests**: integration
**Gate**: full
**Commit**: `feat(account): create reset password controller endpoint`

---

## Phase Execution Map

```
Phase 1 → Phase 2

Phase 1:
T1
T2
T1 → T3
T2 → T3

Phase 2:
T4
T5
T1 → T6
T4 → T6
T5 → T6
T6 → T7
```

---

## Task Granularity Check

| Task                            | Scope         | Status       |
| ------------------------------- | ------------- | ------------ |
| T1: Global Revocation           | 1 component   | ✅ Granular  |
| T2: JwtUtil getIssuedAt         | 1 function    | ✅ Granular  |
| T3: Enforce Revocation Auth     | 1 function    | ✅ Granular  |
| T4: ResetPasswordRequest DTO    | 1 class       | ✅ Granular  |
| T5: ForgotPasswordTokenManager  | 2 methods     | ✅ Granular  |
| T6: ResetPasswordService        | 1 service     | ✅ Granular  |
| T7: ResetPasswordController     | 1 endpoint    | ✅ Granular  |

---

## Diagram-Definition Cross-Check

| Task | Depends On (task body) | Diagram Shows | Status |
| ---- | ---------------------- | ------------- | ------ |
| T1   | None                   | None          | ✅ Match |
| T2   | None                   | None          | ✅ Match |
| T3   | T1, T2                 | T1, T2        | ✅ Match |
| T4   | None                   | None          | ✅ Match |
| T5   | None                   | None          | ✅ Match |
| T6   | T1, T4, T5             | T1, T4, T5    | ✅ Match |
| T7   | T6                     | T6            | ✅ Match |

---

## Test Co-location Validation

| Task | Code Layer Created/Modified | Matrix Requires | Task Says | Status |
| ---- | --------------------------- | --------------- | --------- | ------ |
| T1   | Service                     | unit            | unit      | ✅ OK  |
| T2   | Utility                     | unit            | unit      | ✅ OK  |
| T3   | Service                     | unit            | unit      | ✅ OK  |
| T4   | Entity/DTO                  | none            | unit      | ✅ OK  |
| T5   | Utility/Config              | unit            | unit      | ✅ OK  |
| T6   | Service                     | unit            | unit      | ✅ OK  |
| T7   | Controller                  | integration     | integration | ✅ OK  |
