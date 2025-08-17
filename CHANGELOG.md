## 2025-08-17 [*](https://github.com/hywenklis/buddy-api/pull/188)

### Added
- Email verification flow with request/confirm endpoints, Redis caches, and email template integration
- External email service integration via manager API with token caching and error handling

### Changed
- Authentication now considers account status (blocked/deleted) and roles with prefix
- Account update service supports verifying status updates

### Fixed
- Standardized domain exceptions to preserve original causes
- Minor test and configuration adjustments for email verification and external integration
