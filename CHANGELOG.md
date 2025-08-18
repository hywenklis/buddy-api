## 2025-08-18 [*](https://github.com/hywenklis/buddy-api/pull/191)

### Added
- Expanded tests for email verification, manager client, and error handling with WireMock and Redis
- Custom API client executor and Feign error decoder for external integrations

### Changed
- Authentication uses custom AuthenticatedUser with role prefix and account status checks
- Cache and Redis configurations refined for tokens and rate limiting

### Fixed
- Minor fixes in templates, exception chaining, and mapper/update flows
