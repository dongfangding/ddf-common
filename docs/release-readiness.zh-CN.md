# 发布就绪度

[English](./release-readiness.md) | [中文](./release-readiness.zh-CN.md)

最后更新：2026-03-12

## 概览

首个公开 Maven Central 发布的当前状态：

- 候选发布版本：`boot3.5-2026.1`
- 根 release profile 校验：已通过
- 根编译校验：已通过
- Maven Central 元数据：已准备
- Sources / Javadocs / 签名流程：已准备
- 公开模块发布边界：已文档化
- 根 README 与主要模块 README：已基本对齐

## 自动配置 Smoke Test 覆盖情况

| 模块 | Auto Configuration | Test Files | Smoke Test |
| --- | --- | ---: | --- |
| ddf-common-alarm | Yes | 0 | - |
| ddf-common-api | Yes | 4 | - |
| ddf-common-authentication | Yes | 1 | AuthenticationAutoConfigurationTest |
| ddf-common-canal | No | 0 | - |
| ddf-common-captcha | Yes | 0 | - |
| ddf-common-core | Yes | 12 | - |
| ddf-common-data-mysql-starter | Yes | 1 | DataMysqlAutoConfigurationTest |
| ddf-common-dependency | No | 0 | - |
| ddf-common-distributed-lock | Yes | 0 | - |
| ddf-common-es | No | 0 | - |
| ddf-common-governance-starter | Yes | 1 | GovernanceAutoConfigurationTest |
| ddf-common-ids-service | Yes | 0 | - |
| ddf-common-limit | Yes | 0 | - |
| ddf-common-log4j | No | 0 | - |
| ddf-common-mongo | Yes | 1 | MongoAutoConfigurationTest |
| ddf-common-mqtt | Yes | 0 | - |
| ddf-common-mqtt-client | Yes | 1 | MqttClientAutoConfigurationTest |
| ddf-common-mvc | Yes | 0 | - |
| ddf-common-netty-broker | Yes | 0 | - |
| ddf-common-ons | Yes | 0 | - |
| ddf-common-redis | Yes | 1 | RedisCustomizeAutoConfigurationTest |
| ddf-common-rocketmq | Yes | 0 | - |
| ddf-common-s3 | Yes | 0 | - |
| ddf-common-script | No | 0 | - |
| ddf-common-sharding | Yes | 0 | - |
| ddf-common-starter-default | No | 0 | - |
| ddf-common-starter-web | No | 0 | - |
| ddf-common-third-party | Yes | 0 | - |
| ddf-common-vps | Yes | 0 | - |
| ddf-common-websocket | Yes | 0 | - |
| ddf-common-xxl-executor | Yes | 0 | - |
| ddf-common-zookeeper | Yes | 0 | - |

## 当前优势

- 根 README 已说明模块分层、starter 组合、发布流程以及公开/非公开发布边界。
- 子模块 Maven 元数据已足够统一，适合仓库索引展示。
- 若干关键自动配置模块已经具备最小 smoke test：
  - authentication
  - data mysql starter
  - governance starter
  - mongo
  - mqtt-client
  - redis
- 非公开模块已明确排除出默认 Central 发布集合：
  - `ddf-common-script`
  - `ddf-common-netty-broker`

## 剩余缺口

- 仍有很多自动配置模块缺少 smoke test。
- `ddf-common-mvc` 不适合超轻量 `ApplicationContextRunner` 测试，需要更完整的 web slice 测试策略。
- `ddf-common-limit` 当前虽然暴露了轻量自动配置入口，但真正能力启用仍依赖显式 enable 注解导入。
- 部分模块在被视为高度稳定的公开契约前，还需要更深入的 API 级审查。

## 建议的下一步发布动作

1. 在真实凭据和 GPG 环境下执行 `mvn -Prelease clean deploy`。
2. 发布成功后创建 `boot3.5-2026.1` Git tag。
3. 在 Maven Central 上验证公开坐标与 README 渲染效果。
4. release 分支或 tag 固化后，将仓库切回下一开发周期的 `SNAPSHOT` 版本。
