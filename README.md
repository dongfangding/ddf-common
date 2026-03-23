# ddf-common

[English](./README.md) | [中文](./README.zh-CN.md)

`ddf-common` is a multi-module foundational component library for Spring Boot 3. It consolidates shared backend capabilities, web infrastructure integration, and common middleware or third-party integrations.

It is not a runnable business application template. It is intended to be a reusable, composable, and evolving library set. The repository provides both low-level modules and business-facing starters so different services can assemble only what they need.

## Baseline

- Java 17
- Spring Boot 3.5.9
- Maven 3.9.6+

## Design Goals

- Build a Spring Boot 3 oriented multi-module shared foundation
- Reduce adoption cost through starters and auto-configuration
- Cover shared capabilities, infrastructure integrations, and scenario-oriented packaging
- Standardize the repository for Maven Central publication and long-term evolution

## Module Layers

### 1. Core Foundation Layer

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

### 2. Infrastructure Layer

- `ddf-common-redis`
- `ddf-common-distributed-lock`
- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`
- `ddf-common-log4j`
- `ddf-common-sharding`
- `ddf-common-zookeeper`
- `ddf-common-mongo`
- `ddf-common-es`

### 3. Scenario And Extension Layer

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

### 4. Aggregation And Dependency Management Layer

- `ddf-common-dependency`
- `ddf-common-starter-web`
- `ddf-common-starter-default`

## Module Capability Overview

### Core Foundation Layer

| Module | Description |
| --- | --- |
| `ddf-common-api` | Shared constants, context models, validation contracts, common enums, base interfaces, and reusable DTO conventions. |
| `ddf-common-core` | Core utilities, cache and crypto support, global properties, event models, and foundational supporting components. |
| `ddf-common-mvc` | MVC auto-configuration, unified response wrapping, global exception handling, filters, access logs, and web foundation support. |
| `ddf-common-authentication` | Authentication filters, token validation extension points, authentication properties, and login-state cache integration. |
| `ddf-common-limit` | Rate limiting and repeat-submission protection, including annotations, aspects, key generators, and related configuration. |

### Infrastructure Layer

| Module | Description |
| --- | --- |
| `ddf-common-redis` | Redis and Redisson auto-configuration, cache manager support, local cache support, and Redis helper utilities. |
| `ddf-common-distributed-lock` | Distributed lock implementations based on Redis and Zookeeper. |
| `ddf-common-log4j` | Log4j2 logging integration support. |
| `ddf-common-data-mysql-starter` | Aggregates JDBC, MySQL, and Druid as the MySQL data-access starter. |
| `ddf-common-governance-starter` | Aggregates Mail, Actuator, and Prometheus registry support as the entry point for governance, alerting, and observability capabilities. |
| `ddf-common-sharding` | Sharding auto-configuration and rule packaging. |
| `ddf-common-zookeeper` | Zookeeper listeners, monitoring support, and related helpers. |
| `ddf-common-mongo` | MongoDB auto-configuration and `MongoTemplate` helper utilities. |
| `ddf-common-es` | Elasticsearch dependency integration module. |

### Scenario And Extension Layer

| Module | Description |
| --- | --- |
| `ddf-common-alarm` | Alerting for exceptions, logs, and rule-driven scenarios, including DingTalk and Lark notification integrations. |
| `ddf-common-captcha` | Image captcha and behavior captcha integration support. |
| `ddf-common-ids-service` | Distributed ID generation configuration, API contracts, and implementations. |
| `ddf-common-rocketmq` | RocketMQ enhancement layer for message wrapping, environment isolation, and producer support. |
| `ddf-common-ons` | Alibaba Cloud ONS integration and listener container support. |
| `ddf-common-mqtt` | Core MQTT client connectivity, publishing support, and related connection settings. |
| `ddf-common-mqtt-client` | Higher-level topic and message model abstractions on top of `ddf-common-mqtt`. |
| `ddf-common-websocket` | WebSocket configuration, handshake support, message handling, and related business support points. |
| `ddf-common-netty-broker` | Netty-based broker or message proxy samples and foundational implementation. |
| `ddf-common-third-party` | Third-party integration utilities, currently focused on Alibaba Cloud OSS and SMS support. |
| `ddf-common-s3` | Object storage abstractions compatible with the S3 protocol. |
| `ddf-common-vps` | File and image processing support. |
| `ddf-common-xxl-executor` | XXL-Job executor module and baseline configuration. |
| `ddf-common-canal` | Canal subscription message dispatch support. |
| `ddf-common-script` | Internal scripts and offline utility code. |

### Aggregation And Dependency Management Layer

| Module | Description |
| --- | --- |
| `ddf-common-dependency` | Centralized dependency version management suitable for BOM import. |
| `ddf-common-starter-web` | Aggregates common web foundation modules as the starting point for lightweight web services. |
| `ddf-common-starter-default` | Aggregates web, MySQL, governance, and other baseline capabilities for standard business services. |

## Recommended Integration Patterns

### 1. Lightweight Web Service

Suitable when:

- Only web foundation support is needed
- No database integration is needed yet
- No governance integration is needed yet

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

### 2. Standard Business Service

Suitable when:

- Building a regular backend business service
- Web, MySQL, Druid, and governance capabilities are all needed

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

### 3. Custom Composition

If you do not want to use `starter-default`, you can combine modules by scenario:

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

## Starter Notes

### `ddf-common-starter-web`

Aggregated modules:

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

Typical scenarios:

- Web API services
- Basic admin services
- Projects that need unified exception handling, unified responses, basic authentication, and rate limiting

### `ddf-common-data-mysql-starter`

Aggregated dependencies:

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`

Unified configuration prefix:

```yaml
customizer:
  data:
    mysql:
      enabled: true
      druid:
        usePingMethod: false
```

### `ddf-common-governance-starter`

Aggregated dependencies:

- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`
- `micrometer-registry-prometheus`

Unified configuration prefix:

```yaml
customizer:
  governance:
    mail:
      enabled: true
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
```

Notes:

- Importing the governance starter does not fail if `spring.mail.*` is absent.
- Mail-related beans are only wired when the underlying mail beans exist.
- Supported thread pool beans can be auto-bound to Micrometer metrics by bean-name wildcard rules.
- Expose `management.endpoints.web.exposure.include=prometheus` to let Prometheus scrape `/actuator/prometheus`.
- See `docs/thread-pool-observability.md` for Prometheus, Grafana, and alert rule templates.

### `ddf-common-starter-default`

Aggregated modules:

- `ddf-common-starter-web`
- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`

Typical scenarios:

- Standard backend business services
- Services that need rapid access to web, database, governance, and baseline middleware capabilities

## Maven Central Publishing Notes

The project has been prepared for Maven Central publication with the following focus areas:

- Standardized parent POM metadata
- Standardized child module names and descriptions
- Attached sources and Javadocs during release
- GPG signing for release artifacts
- Sonatype Central Publisher Portal plugin integration

Recommended pre-release checks:

- Central repository credentials are configured in `settings.xml`
- GPG is configured and available
- Releases are executed with the `release` profile

Related documents:

- `docs/releasing-to-maven-central.md`
- `docs/versioning-and-release-policy.md`
- `docs/public-module-policy.md`
- `docs/release-readiness.md`
- `CHANGELOG.md`

## Build Commands

Build with the repository-specific Maven settings:

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests
```

Build a specific module:

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests -pl ddf-common-starter-default -am
```

Run a release build:

```bash
mvn -Prelease clean deploy
```

## Project Notes

- This repository is a shared library project, not a runnable business application.
- `ddf-common-script` is primarily an internal offline tooling module and is not recommended as a default business dependency.
- `ddf-common-netty-broker` is currently more demo-oriented or protocol-specific and is excluded from the default Maven Central publishing set.
- A business example project can reference `spring-boot-quick`.
- For lightweight integration, see `examples/minimal-web-service/README.md`.

## GitHub Actions Release Preparation

If you use the repository GitHub Actions release flow, prepare these repository secrets:

- `MAVEN_USERNAME`
- `MAVEN_PASSWORD`
- `MAVEN_GPG_PRIVATE_KEY`
- `MAVEN_GPG_PASSPHRASE`

Related workflows:

- `.github/workflows/ci.yml`
- `.github/workflows/release-central.yml`

## Next Improvement Areas

- Continue tightening module naming and responsibility boundaries
- Add minimal integration examples for each starter
- Improve auto-configuration conditions and test coverage
- Refine Maven Central release guidance and versioning workflow

## License

Apache License 2.0
