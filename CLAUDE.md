# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**ddf-common** —— 面向 Spring Boot 3 的多模块通用基础组件库。**不是可运行业务工程**，模块以 starter + 自动配置形式输出能力，由上层服务按需组合。

- **Group ID**：`io.github.dongfangding`
- **当前版本**：`boot3.5-2026.1-SNAPSHOT`（在根 `pom.xml` 的 `<revision>` 属性集中管理）
- **Java**：17（由 `maven-enforcer-plugin` 强制）
- **Spring Boot**：3.5.9
- **Maven**：3.9.6+（强制）
- **包前缀**：核心组件 `com.ddf.boot.common.*`；服务模块 `com.ddf.common.*`

## 常用命令

```bash
# 全模块构建（日常默认跳过测试以加速）
mvn clean install -DskipTests

# 构建指定模块及其依赖（-am = also make）
mvn clean install -DskipTests -pl ddf-common-starter-default -am

# 测试
mvn test                                                          # 全模块
mvn test -pl ddf-common-core                                      # 单模块
mvn test -pl ddf-common-core -Dtest=ComparatorUtilTest            # 单测试类

# 与 CI 对齐的自动配置 smoke test（见 .github/workflows/ci.yml）
mvn -q -pl ddf-common-governance-starter -Dtest=GovernanceAutoConfigurationTest test
mvn -q -pl ddf-common-data-mysql-starter -Dtest=DataMysqlAutoConfigurationTest test

# 发布到 Maven Central（必须显式启用 release profile，GPG 签名 + Sonatype Central Publisher）
mvn -Prelease clean deploy
```

CI 工作流：`.github/workflows/ci.yml`（编译 + 关键 starter smoke test）、`release-central.yml`（发布）。

## 架构分层

模块按四层组织，依赖方向**只能从下往上**：

```
聚合 / 依赖管理层    ddf-common-dependency (BOM)
                    ddf-common-starter-web
                    ddf-common-starter-default
        ↑
场景 / 扩展层        alarm  captcha  ids-service  rocketmq  ons  mqtt(-client)
                    websocket  netty-broker  s3  vps  xxl-executor  canal
                    third-party  script  es
        ↑
基础设施层          redis  distributed-lock  data-mysql-starter
                   governance-starter  log4j  sharding  zookeeper  mongo
        ↑
基础内核层          api ← core ← mvc / authentication / limit
```

关键边界：

- **`ddf-common-api`** 是所有模块的根，只承载协议、DTO、异常、约束、公共枚举，不引入任何业务/基础设施依赖。
- **`ddf-common-core`** 仅放轻量公共能力（工具类、线程池、Spring 支撑、加密、ID 生成）；JDBC/MySQL/Druid/Mail/Actuator 已迁出至独立 starter，禁止回流。
- **业务接入优先选择场景 starter**，而非手工拼装底层模块。
- 详细模块清单与 starter 聚合关系见 `README.zh-CN.md`。

## Starter 选择决策

| 场景                     | 选用                                                              |
|------------------------|-----------------------------------------------------------------|
| 纯 Web / 不接 DB / 不接治理   | `ddf-common-starter-web`                                        |
| 常规业务（Web + MySQL + 治理） | `ddf-common-starter-default`                                    |
| 需自定义组合                 | `starter-web` + `data-mysql-starter` + `governance-starter` 等按需 |

`ddf-common-script` 与 `ddf-common-netty-broker` 默认**不在** Maven Central 发布集合中（见父 `pom.xml` 的 `excludeArtifacts`）。

## 编码约定

### 异常与响应（详见 `ddf-common-api/CLAUDE.md`）

```java
throw new BusinessException(ErrorCodeEnum.XXX);              // 业务异常
ResponseData.success(data);                                  // 成功
ResponseData.failure(BaseErrorCallbackCode.BAD_REQUEST);     // 失败
responseData.requireSuccess();                               // 调用方强制断言
```

错误码枚举须实现 `BaseCallbackCode`。预定义错误码见 `BaseErrorCallbackCode`，生产环境 `isMaskErrorDetails()=true` 的异常不会回吐堆栈。

### 配置属性

- 类名 `XxxProperties`，放在模块的 `properties/` 包下。
- 使用 `@ConfigurationProperties(prefix = "customizer.<scope>.<feature>")`，由 `@EnableConfigurationProperties` 在对应 `*AutoConfiguration` 中显式启用。
- 已沉淀的前缀示例：`customizer.data.mysql`、`customizer.governance.mail`、`customizer.governance.observability`。

### 自动配置（Spring Boot 3.x）

- 类名 `*AutoConfiguration`，放在 `config/` 包下。
- 注册位置：`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。**禁止**使用 `spring.factories`。
- 一个模块通常只暴露一个顶层 `AutoConfiguration` 入口，内部用 `@Import` 拆分组件。
- **避免 `@ComponentScan` 做自动装配**，应显式声明 Bean —— 这是当前阶段治理方向（参考 `基础脚手架优化执行计划.md`）。

### Redis Key

- 所有 Key 必须由实现 `RedisKeyConstraint` 接口的枚举生成，参考 `com.ddf.boot.common.alarm.enums.AlarmRedisKeyEnum`。
- 模式 `{applicationName}:{module}:{action}:{identifier}`，通过 `ApplicationNamedKeyGenerator.genKey(...)` 拼接。

## 添加新模块

1. 创建目录 `ddf-common-{name}/`，复制相邻模块的 `pom.xml` 作为骨架。
2. 在根 `pom.xml` 的 `<modules>` 中追加。
3. 新依赖在 `ddf-common-dependency/pom.xml` 的 `dependencyManagement` 中声明版本；子模块引用时**不写版本**。
4. 若包含 Spring Bean，新增 `*AutoConfiguration` 并在 `META-INF/spring/...AutoConfiguration.imports` 中登记。
5. 编写模块级 `CLAUDE.md`，对齐已有结构（模块简介 / 核心类表 / 使用示例 / 注意事项）。
6. 若需 Maven Central 发布，确认未被父 `pom.xml` 的 `excludeArtifacts` 排除。

## 注意事项

1. **Jakarta EE**：Spring Boot 3.x 使用 `jakarta.*`，不是 `javax.*`。
2. **不可直接运行**：业务示例参考 `examples/minimal-web-service/` 与外部仓库 `spring-boot-quick`。
3. **测试默认跳过**：日常 `-DskipTests` 加速构建，但 PR 前应执行受影响模块的测试。
4. **发布治理**：发布前阅读 `docs/releasing-to-maven-central.zh-CN.md`、`docs/versioning-and-release-policy.zh-CN.md`、`docs/public-module-policy.zh-CN.md`。

## 模块级文档

每个子模块根目录都有自己的 `CLAUDE.md`，记录其核心类、配置示例、注意事项。**修改某模块前先读对应文件**，避免重复探索。

## MCP Tools: code-review-graph

**本项目已构建知识图谱**（约 4067 节点 / 25562 边 / 718 文件）。**探索代码前优先使用 code-review-graph 工具**，而非 Grep / Glob / Read —— 图谱更快、token 更省，并能给出 Grep 无法覆盖的结构信息（调用者、依赖、测试覆盖）。

### 何时优先使用图谱

- **探索代码**：`semantic_search_nodes` / `query_graph` 而非 Grep
- **影响分析**：`get_impact_radius` 而非手工追 import
- **代码评审**：`detect_changes` + `get_review_context` 而非整文件读
- **关系查询**：`query_graph` pattern=`callers_of` / `callees_of` / `imports_of` / `tests_for`
- **架构概览**：`get_architecture_overview` + `list_communities`

只有当图谱无法覆盖时才回退到 Grep / Glob / Read。图谱由 `.claude/settings.json` 的 PostToolUse hook 增量更新。

### 关键工具速查

| 工具                          | 何时使用                                       |
|-----------------------------|--------------------------------------------|
| `detect_changes`            | 评审改动 —— 给出风险评分                             |
| `get_review_context`        | 评审需要源码片段 —— token 友好                       |
| `get_impact_radius`         | 衡量改动的爆炸半径                                  |
| `get_affected_flows`        | 找受影响的执行路径                                  |
| `query_graph`               | 追 callers / callees / imports / tests / 依赖 |
| `semantic_search_nodes`     | 按名字 / 关键词找函数或类                             |
| `get_architecture_overview` | 看高层架构                                      |
| `refactor_tool`             | 规划重命名、找死代码                                 |
