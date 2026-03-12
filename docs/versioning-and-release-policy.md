# Versioning And Release Policy

[English](./versioning-and-release-policy.md) | [中文](./versioning-and-release-policy.zh-CN.md)

## Version Strategy

The repository currently uses the root `revision` property to control all module versions.

Recommended release conventions:

- Development: `boot3.5-YYYY.N-SNAPSHOT`
- Stable release: `boot3.5-YYYY.N`
- Patch release: `boot3.5-YYYY.N.P`

Examples:

- `boot3.5-2026.1-SNAPSHOT-SNAPSHOT`
- `boot3.5-2026.1-SNAPSHOT`
- `boot3.5-2026.1-SNAPSHOT.1`

## Release Rules

- Do not publish a release version unless `mvn -Prelease clean deploy` has been verified locally or in CI.
- Update `CHANGELOG.md` before cutting a release.
- Keep the root `README.md` aligned with newly added or removed modules.
- If a module becomes internal-only, document that decision clearly before release.

## Compatibility Rules

- Avoid breaking public starter coordinates casually.
- Avoid renaming published modules once they are available in Maven Central.
- If an auto-configuration changes default behavior, record it in `CHANGELOG.md`.
- If a module is deprecated, keep it for at least one deprecation cycle before removal when possible.
