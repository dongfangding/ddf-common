# CLAUDE.md

此文件为 Claude Code 提供项目开发指导。

## 项目概述

**ddf-common** - Spring Boot 3.3 多模块通用组件库，为 Java 后端开发提供可复用组件。这是一个依赖库，不是可运行的应用。

- **Java 版本**: 17
- **框架**: Spring Boot 3.3.13
- **构建工具**: Maven 3.9.6+
- **Group ID**: com.ddf.common

## 常用命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 构建指定模块（包含依赖）
mvn clean install -DskipTests -pl ddf-common-core -am

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -pl ddf-common-core -Dtest=ComparatorUtilTest

# 运行指定模块测试
mvn test -pl ddf-common-core

# 部署到仓库
mvn clean deploy -DskipTests
```

## 模块依赖关系

```
ddf-common-api           <- 所有模块依赖
    |
ddf-common-core          <- mvc, authentication, redis, limit 等
    |
ddf-common-dependency    <- (BOM，由父 pom 导入)
```

## 包命名规范

| 模块类型 | 包前缀                     |
|------|-------------------------|
| 核心模块 | `com.ddf.boot.common.*` |
| 服务模块 | `com.ddf.common.*`      |

常用包结构：`config/`, `properties/`, `model/`, `service/`, `api/`, `annotation/`, `aspect/`, `handler/`, `util/`, `enum/`, `exception/`

## 编码规范

### 异常与响应
```java
// 抛出业务异常
throw new BusinessException(ErrorCodeEnum.XXX);

// API 响应包装
ResponseData.success(data);
ResponseData.failure(BaseCallbackCode.XXX);
```

### 配置属性类
- 使用 `@ConfigurationProperties(prefix = "xxx")`
- 放在 `properties/` 包下

### 自动配置类
- 命名为 `*AutoConfiguration`
- 通过 `META-INF/spring/*.imports` 导入 (Spring Boot 3.x)

### Redis Key
- 必须实现 `RedisKeyConstraint` 接口, 参考示例`com.ddf.boot.common.alarm.enums.AlarmRedisKeyEnum`
- 模式：`{prefix}:{module}:{action}:{identifier}`

## 添加新模块

1. 创建目录 `ddf-common-{module-name}/`
2. 在根 `pom.xml` 的 `modules` 列表中添加模块名
3. 创建 `pom.xml`，设置 parent 引用
4. 新依赖在 `ddf-common-dependency/pom.xml` 中添加版本管理

## 注意事项

1. **Spring Boot 3.x**: 使用 Jakarta EE（非 javax）
2. **Guava 版本冲突**: 不同模块可能使用不同版本
3. **ZooKeeper**: Curator 5.1.0 要求 ZooKeeper 3.4.x+
4. **不可直接运行**: 这是一个库项目，使用示例参考 [spring-boot-quick](https://github.com/dongfangding/spring-boot-quick)

<!-- code-review-graph MCP tools -->
## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
| ------ | ---------- |
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.
