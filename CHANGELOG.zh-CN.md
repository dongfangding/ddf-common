# 变更日志

[English](./CHANGELOG.md) | [中文](./CHANGELOG.zh-CN.md)

本文件记录项目中所有值得关注的变更。

格式参考 Keep a Changelog，并结合当前仓库的实际情况进行了调整。

## [Unreleased]

### Added

### Changed

## [boot3.5-2026.1-SNAPSHOT] - 2026-03-12

### Added

- 扩展根 README，形成完整的模块与 starter 总览文档。
- 新增 Maven Central 发布指引 `docs/releasing-to-maven-central.md`。
- 新增公开模块发布边界说明 `docs/public-module-policy.md`。
- 新增第一轮发布治理的设计与实施计划文档。
- 为 `ddf-common-governance-starter` 新增自动配置 smoke test。
- 为 `ddf-common-data-mysql-starter` 新增自动配置 smoke test。
- 新增 GitHub Actions CI 与发布工作流草案。
- 在 `examples/minimal-web-service` 下新增最小示例项目文档。

### Changed

- 将仓库版本从 `boot3.5-2025.1-SNAPSHOT` 提升为首个正式发布候选 `boot3.5-2026.1-SNAPSHOT`。
- 统一子模块的 `name` 与 `description`，提升 Maven Central 展示质量。
- 在根构建中加入 Java 17 与 Maven 3.9.6+ 的环境约束。
- 在根构建中加入 UTF-8 reporting 输出编码。
- 将内部或示例导向模块排除在默认 Maven Central 发布集合之外。

## 版本策略

- `SNAPSHOT` 版本用于开发过程中的持续演进。
- 正式发布版本应移除 `-SNAPSHOT` 后缀。
- 破坏性 API 变更应在发布说明中显式记录。
- 新模块、starter 聚合变化和自动配置行为变化都应在此记录。
