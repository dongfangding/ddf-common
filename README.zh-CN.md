# ddf-common

[English](./README.md) | [中文](./README.zh-CN.md)

`ddf-common` 是一个面向 Spring Boot 3 的多模块通用基础组件库，用于沉淀后端项目中的公共能力、Web 基础设施接入能力，以及常见中间件与第三方集成能力。

它的定位不是单体业务工程模板，而是可复用、可组合、可沉淀的基础能力库。项目既提供底层模块，也提供面向业务接入的 starter，便于在不同类型的服务中按需组合。

## 基础环境

- Java 17
- Spring Boot 3.5.9
- Maven 3.9.6+

## 设计目标

- 面向 Spring Boot 3 的多模块基础组件沉淀
- 以 starter 和自动配置方式降低接入成本
- 兼顾通用能力、基础设施接入和场景化封装
- 面向 Maven Central 发布与长期演进进行标准化治理

## 模块分层

### 1. 基础内核层

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

### 2. 基础设施层

- `ddf-common-redis`
- `ddf-common-distributed-lock`
- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`
- `ddf-common-log4j`
- `ddf-common-sharding`
- `ddf-common-zookeeper`
- `ddf-common-mongo`
- `ddf-common-es`

### 3. 场景与扩展层

- `ddf-common-alarm`
- `ddf-common-captcha`
- `ddf-common-ids-service`
- `ddf-common-rocketmq`
- `ddf-common-ons`
- `ddf-common-mqtt`
- `ddf-common-mqtt-client`
- `ddf-common-websocket`
- `ddf-common-netty-broker`
- `ddf-common-third-party`
- `ddf-common-s3`
- `ddf-common-vps`
- `ddf-common-xxl-executor`
- `ddf-common-canal`
- `ddf-common-script`

### 4. 聚合与依赖管理层

- `ddf-common-dependency`
- `ddf-common-starter-web`
- `ddf-common-starter-default`

## 模块能力总览

### 基础内核层

| 模块 | 功能说明 |
| --- | --- |
| `ddf-common-api` | 提供公共常量、上下文、约束定义、通用枚举、基础接口与部分通用 DTO/约定。 |
| `ddf-common-core` | 提供核心工具类、缓存与加密基础能力、全局属性、事件模型、基础组件支撑。 |
| `ddf-common-mvc` | 提供 MVC 自动配置、统一响应包装、全局异常处理、过滤器、访问日志等 Web 基础能力。 |
| `ddf-common-authentication` | 提供认证过滤器、Token 校验扩展、认证配置和登录态缓存接入。 |
| `ddf-common-limit` | 提供限流与防重复提交能力，包括注解、切面、Key 生成器和相关配置。 |

### 基础设施层

| 模块 | 功能说明 |
| --- | --- |
| `ddf-common-redis` | 提供 Redis/Redisson 自动配置、缓存管理器、本地缓存、Redis 扩展工具。 |
| `ddf-common-distributed-lock` | 提供基于 Redis 和 Zookeeper 的分布式锁实现。 |
| `ddf-common-log4j` | 提供 Log4j2 基础日志能力接入。 |
| `ddf-common-data-mysql-starter` | 聚合 JDBC、MySQL、Druid，作为 MySQL 数据访问 starter 使用。 |
| `ddf-common-governance-starter` | 聚合 Mail 和 Actuator，提供基础治理、告警和可观测能力入口。 |
| `ddf-common-sharding` | 提供分库分表自动配置与规则封装。 |
| `ddf-common-zookeeper` | 提供 Zookeeper 监听、监控与相关辅助能力。 |
| `ddf-common-mongo` | 提供 MongoDB 自动配置和 `MongoTemplate` 辅助工具。 |
| `ddf-common-es` | 提供 Elasticsearch 依赖接入模块。 |

### 场景与扩展层

| 模块 | 功能说明 |
| --- | --- |
| `ddf-common-alarm` | 提供异常、日志与规则相关告警能力，包含钉钉、飞书等通知集成。 |
| `ddf-common-captcha` | 提供图形验证码与行为验证码能力封装。 |
| `ddf-common-ids-service` | 提供分布式 ID 生成相关配置、接口与实现。 |
| `ddf-common-rocketmq` | 提供 RocketMQ 增强封装，如消息包装、环境隔离、生产者封装。 |
| `ddf-common-ons` | 提供阿里云 ONS 接入与监听容器相关能力。 |
| `ddf-common-mqtt` | 提供 MQTT 基础客户端、连接配置、发布能力与扩展监听。 |
| `ddf-common-mqtt-client` | 基于 `ddf-common-mqtt` 提供更上层的话题与消息模型封装。 |
| `ddf-common-websocket` | 提供 WebSocket 相关配置、握手、消息处理和业务支撑能力。 |
| `ddf-common-netty-broker` | 提供基于 Netty 的 Broker/消息代理示例和基础实现。 |
| `ddf-common-third-party` | 提供第三方扩展能力，当前主要包含阿里云 OSS、短信等封装。 |
| `ddf-common-s3` | 提供兼容 S3 协议的对象存储能力封装。 |
| `ddf-common-vps` | 提供图片与文件处理相关能力。 |
| `ddf-common-xxl-executor` | 提供 XXL-Job 执行器模块和基础配置。 |
| `ddf-common-canal` | 提供 Canal 订阅消息分发能力。 |
| `ddf-common-script` | 存放项目内部使用的脚本与离线工具类。 |

### 聚合与依赖管理层

| 模块 | 功能说明 |
| --- | --- |
| `ddf-common-dependency` | 统一管理项目内部依赖版本，适合作为 BOM 使用。 |
| `ddf-common-starter-web` | 聚合 Web 常用基础模块，适合作为轻量 Web 服务的起点。 |
| `ddf-common-starter-default` | 聚合 Web、MySQL、治理等常规业务服务基础能力。 |

## 推荐接入方式

### 1. 轻量 Web 服务

适合：

- 只需要 Web 基础能力
- 暂时不接数据库
- 暂时不接治理能力

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

### 2. 常规业务服务

适合：

- 常规业务后端服务
- 需要 Web、MySQL、Druid、治理能力

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

### 3. 自定义组合

如果不希望直接使用 `starter-default`，也可以按场景组合：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>

<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>

<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Starter 说明

### `ddf-common-starter-web`

聚合模块：

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

适用场景：

- Web API 服务
- 基础后台管理服务
- 需要统一异常、统一响应、基础认证与限流能力的项目

### `ddf-common-data-mysql-starter`

聚合依赖：

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`

统一配置前缀：

```yaml
customizer:
  data:
    mysql:
      enabled: true
      druid:
        usePingMethod: false
```

### `ddf-common-governance-starter`

聚合依赖：

- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`

统一配置前缀：

```yaml
customizer:
  governance:
    mail:
      enabled: true
    observability:
      enabled: true
```

说明：

- 引入治理层 starter 后，未配置 `spring.mail.*` 不会报错
- Mail 能力只有在底层 Bean 存在时才会自动接回

### `ddf-common-starter-default`

聚合模块：

- `ddf-common-starter-web`
- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`

适用场景：

- 标准业务后端服务
- 需要快速具备 Web、数据库、治理与基础中间件接入能力的项目

## 面向 Maven Central 的发布说明

项目当前已按 Maven Central 发布方向进行整理，重点包括：

- 统一父 POM 元数据
- 统一子模块名称与描述
- 发布时附带源码包与 Javadoc
- 使用 GPG 对发布产物进行签名
- 使用 Sonatype Central Publisher Portal 插件执行中央仓库发布

建议发布前确认：

- `settings.xml` 中已配置中央仓库认证信息
- GPG 环境已配置完成
- 使用 `release` profile 执行发布

相关文档：

- `docs/releasing-to-maven-central.md`
- `docs/versioning-and-release-policy.md`
- `docs/public-module-policy.md`
- `docs/release-readiness.md`
- `CHANGELOG.md`

## 构建命令

使用项目指定的 Maven settings：

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests
```

构建指定模块：

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests -pl ddf-common-starter-default -am
```

执行发布构建：

```bash
mvn -Prelease clean deploy
```

## 项目说明

- 这是一个基础库项目，不是可直接运行的业务应用
- `ddf-common-script` 更偏向内部离线工具，不建议作为常规业务依赖默认引入
- `ddf-common-netty-broker` 当前更偏示例/特定协议场景模块，默认不纳入 Maven Central 发布集合
- 示例业务项目可参考 `spring-boot-quick`
- 轻量示例接入可参考 `examples/minimal-web-service/README.md`

## GitHub Actions 发布准备

如果使用仓库内的 GitHub Actions 发布流程，需要在仓库 Secrets 中准备：

- `MAVEN_USERNAME`
- `MAVEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

对应工作流：

- `.github/workflows/ci.yml`
- `.github/workflows/release-central.yml`

## 后续优化方向

- 持续收敛模块命名与职责边界
- 补充每个 starter 的最小接入示例
- 增强自动配置条件与测试覆盖
- 完善 Maven Central 发布文档与版本发布流程

## License

Apache License 2.0

## 治理与线程池监控补充说明

`ddf-common-governance-starter` 现在除了 `spring-boot-starter-mail`、`spring-boot-starter-actuator` 之外，
还聚合了 `micrometer-registry-prometheus`，可以直接暴露 `/actuator/prometheus` 供 Prometheus 抓取。

线程池监控相关配置示例：

```yaml
customizer:
  governance:
    observability:
      enabled: true
      thread-pool:
        enabled: true
        metric-name: custom.thread.pool
        scan-all: false
        include-bean-name-patterns:
          - "*Executor"
          - "*Pool"
          - "*Scheduler"
        exclude-bean-name-patterns:
          - "applicationTaskExecutor"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

说明：

- 支持按 Bean 名通配符规则自动扫描并绑定线程池 Micrometer 指标
- 支持的线程池类型包括 `ThreadPoolTaskExecutor`、`ThreadPoolTaskScheduler`、`ThreadPoolExecutor`、`ScheduledThreadPoolExecutor`
- Prometheus、Grafana 和告警模板见 `docs/thread-pool-observability.md`
