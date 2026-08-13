# Reset Password Specification

## Problem Statement

When a user forgets their password, they receive an email with a reset token. The system needs an endpoint to accept this token and a new password, validate the token's authenticity and expiration, securely update the password, and invalidate previous sessions to protect compromised accounts.

## Goals

- [ ] Securely update the user's password using a valid reset token.
- [ ] Invalidate the used reset token so it cannot be reused.
- [ ] Revoke all existing refresh tokens for the account to terminate old sessions immediately.

## Out of Scope

| Feature     | Reason         |
| ----------- | -------------- |
| Sending the email | Handled by a previous feature (Forgot Password) |
| Managing access tokens expiration | Access tokens are short-lived; we only revoke refresh tokens |

---

## Assumptions & Open Questions

| Assumption / decision | Chosen default  | Rationale | Confirmed? |
| --------------------- | --------------- | --------- | ---------- |
| Rate limiting         | Same rate limit as login/forgot password | Prevent brute force | y |
| Strong password policy | Reuse existing policy validator | Keep consistency | y |
| Token invalidation | Delete token from DB/cache or mark as used | Simplest secure way | y |
| Refresh token revocation | Delete all refresh tokens for account | Ensures immediate session kill | y |

**Open questions:** none

---

## User Stories

### P1: Reset Password Successfully ⭐ MVP

**User Story**: As a user with a valid reset token, I want to set a new password so that I can regain access to my account securely and terminate any unauthorized access.

**Why P1**: Core security flow for account recovery.

**Acceptance Criteria**:

1. WHEN a valid, non-expired token and a valid new password are submitted THEN the system SHALL update the account password hash.
2. WHEN the password is successfully updated THEN the system SHALL invalidate the used reset token.
3. WHEN the password is successfully updated THEN the system SHALL require all refresh tokens issued before revocation to be rejected, rather than requiring physical deletion of every active token.
4. IF the token is invalid, used, or expired THEN the system SHALL return a 400 Bad Request domain-friendly error.
5. IF the new password does not meet the strong password policy THEN the system SHALL return a 400 Bad Request error.

**Independent Test**: Send a valid token and password, verify DB is updated, token is deleted, and refresh tokens are deleted.

---

## Edge Cases

- IF the account is disabled or locked THEN the system SHALL return an appropriate error (e.g., 403 Forbidden).
- IF the request is rate-limited THEN the system SHALL return a 429 Too Many Requests error.

---

## Requirement Traceability

| Requirement ID | Story       | Phase  | Status  |
| -------------- | ----------- | ------ | ------- |
| RESET-01       | P1: Reset Password | Design | Pending |
| RESET-02       | P1: Reset Password | Design | Pending |
| RESET-03       | P1: Reset Password | Design | Pending |
| RESET-04       | P1: Reset Password | Design | Pending |
| RESET-05       | P1: Reset Password | Design | Pending |

**Coverage:** 5 total, 0 mapped to tasks, 5 unmapped ⚠️

---

## Success Criteria

- [ ] Used or expired tokens return a domain friendly error.
- [ ] Password is updated successfully in the database.
- [ ] Old sessions (refresh tokens) are immediately revoked.
