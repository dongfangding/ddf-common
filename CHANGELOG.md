# Changelog

[English](./CHANGELOG.md) | [中文](./CHANGELOG.zh-CN.md)

All notable changes to this project should be documented in this file.

The format is based on Keep a Changelog principles, adapted for this repository.

## [Unreleased]

### Added

### Changed

## [boot3.5-2026.1] - 2026-03-12

### Added

- Expanded the root README into a complete module and starter overview.
- Added Maven Central release guidance in `docs/releasing-to-maven-central.md`.
- Added public module publishing guidance in `docs/public-module-policy.md`.
- Added design and implementation plan documents for the first release-governance pass.
- Added auto-configuration smoke tests for `ddf-common-governance-starter`.
- Added auto-configuration smoke tests for `ddf-common-data-mysql-starter`.
- Added CI and release workflow drafts for GitHub Actions.
- Added a minimal example project under `examples/minimal-web-service`.

### Changed

- Promoted the repository version from `boot3.5-2025.1-SNAPSHOT` to the first release candidate `boot3.5-2026.1`.
- Standardized module `name` and `description` metadata for Maven Central visibility.
- Added root Maven build environment constraints for Java 17 and Maven 3.9.6+.
- Added UTF-8 reporting output encoding in the root build.
- Excluded internal or demo-oriented modules from the default Maven Central publishing set.

## Versioning Policy

- `SNAPSHOT` versions are used for in-progress development.
- Release versions should remove the `-SNAPSHOT` suffix.
- Breaking API changes should be called out explicitly in the release notes.
- New modules, starter aggregation changes, and auto-configuration behavior changes should always be documented here.
