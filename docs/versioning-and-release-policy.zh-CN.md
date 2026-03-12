# 版本与发布策略

[English](./versioning-and-release-policy.md) | [中文](./versioning-and-release-policy.zh-CN.md)

## 版本策略

当前仓库通过根 `revision` 属性统一控制所有模块版本。

建议采用以下发布约定：

- 开发版本：`boot3.5-YYYY.N-SNAPSHOT`
- 稳定版本：`boot3.5-YYYY.N`
- 补丁版本：`boot3.5-YYYY.N.P`

示例：

- `boot3.5-2026.1-SNAPSHOT-SNAPSHOT`
- `boot3.5-2026.1-SNAPSHOT`
- `boot3.5-2026.1-SNAPSHOT.1`

## 发布规则

- 在本地或 CI 中未验证 `mvn -Prelease clean deploy` 之前，不要发布正式版本。
- 发布前必须更新 `CHANGELOG.md`。
- 根 `README.md` 需要与新增或移除的模块保持一致。
- 如果某个模块转为内部使用，应在发布前明确记录该决策。

## 兼容性规则

- 不要轻易破坏已公开 starter 的坐标。
- 模块一旦发布到 Maven Central，就应避免随意重命名。
- 如果自动配置的默认行为发生变化，应记录到 `CHANGELOG.md`。
- 如果模块被标记为废弃，条件允许时应至少保留一个废弃周期再移除。
