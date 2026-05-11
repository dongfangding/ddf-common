# ddf-common-log4j

> Log4j2 日志集成模块。提供 `spring-boot-starter-log4j2` + `disruptor` 依赖聚合，
> 替代 Spring Boot 默认的 Logback 日志栈，支持异步日志、级别隔离、按天滚动等能力。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-log4j` 解决的是 **"高性能、结构化日志输出"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 高并发日志 | 同步日志阻塞业务线程，吞吐量下降 | `disruptor` + `AsyncLogger` 全异步打印，零锁竞争 |
| 日志分级存储 | INFO / WARN / ERROR 混在一个文件，排查困难 | `ThresholdFilter` 按级别分流到独立文件 |
| 链路追踪 | 日志中缺少用户标识和 Trace ID | 支持 `%X{user_id}`、`%X{trace_id}` 等 MDC 变量输出 |
| 日志回滚 | 日志文件无限增长，磁盘被打满 | `TimeBasedTriggeringPolicy` 按天滚动，保留 30 天 |

---

## 2. 依赖引入

直接依赖本模块：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-log4j</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 本模块已排除 `spring-boot-starter-logging`（Logback），无需业务侧再手动排除。

若业务直接依赖 `spring-boot-starter-web`，需排除默认日志：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 3. 最小化配置

引入依赖后，在业务工程的 `src/main/resources` 下创建 `log4j2.xml`（或 `log4j2-spring.xml`）。

模块内提供一份参考配置 `log4j2_demo.xml`，核心结构如下：

```xml
<Configuration status="warn" strict="true">
    <Properties>
        <Property name="LOG_BASE_DIR">/data/logs/myapp</Property>
        <Property name="CONSOLE_LOG_PATTERN">
            %clr{%d{yyyy-MM-dd HH:mm:ss.SSS}} %clr{[%t]} %clr{%-5p} %clr{%c(%L)} %clr{%m%n}
        </Property>
        <Property name="FILE_LOG_PATTERN">
            [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%t] %-5p %c{1.} - %msg%n
        </Property>
    </Properties>

    <Appenders>
        <Console name="CONSOLE" target="SYSTEM_OUT">
            <PatternLayout pattern="${sys:CONSOLE_LOG_PATTERN}"/>
        </Console>

        <RollingRandomAccessFile name="INFO_FILE"
            fileName="${LOG_BASE_DIR}/app.log"
            filePattern="${LOG_BASE_DIR}/app-%d{yyyy-MM-dd}.log">
            <ThresholdFilter level="INFO" onMatch="ACCEPT" onMismatch="DENY"/>
            <PatternLayout pattern="${sys:FILE_LOG_PATTERN}"/>
            <Policies>
                <TimeBasedTriggeringPolicy modulate="true" interval="1"/>
            </Policies>
            <DefaultRolloverStrategy max="30"/>
        </RollingRandomAccessFile>
    </Appenders>

    <Loggers>
        <asyncRoot level="INFO" includeLocation="false">
            <AppenderRef ref="CONSOLE"/>
            <AppenderRef ref="INFO_FILE"/>
        </asyncRoot>
    </Loggers>
</Configuration>
```

### 开启全异步模式

在 JVM 启动参数中添加：

```bash
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

或在 `application.yml` 中：

```yaml
logging:
  config: classpath:log4j2.xml
```

---

## 4. 核心能力说明

### 4.1 异步日志

本模块引入 `disruptor` 依赖，配合 `asyncRoot` / `AsyncAppender` 实现全异步日志：

- 业务线程仅将日志事件写入 `RingBuffer`，不等待磁盘 I/O
- 后台专用线程负责批量刷盘
- 适合高并发场景，可显著提升吞吐量

### 4.2 级别隔离

通过 `ThresholdFilter` 将不同级别日志分流：

| Appender | 过滤级别 | 用途 |
| ----- | ----- | ----- |
| `INFO_FILE` | `INFO`+ | 全量日志，保留 30 天 |
| `WARN_FILE` | `WARN`+ | 告警日志，快速定位问题 |
| `ERROR_FILE` | `ERROR`+ | 错误日志，对接告警系统 |

### 4.3 MDC 变量

支持在 Pattern 中输出 MDC 上下文变量：

```xml
<Property name="FILE_LOG_PATTERN">
  [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%t] %-5p [%X{user_id}#%X{trace_id}] %c{1.} - %msg%n
</Property>
```

业务代码中设置 MDC：

```java
import org.slf4j.MDC;

MDC.put("user_id", userId);
MDC.put("trace_id", traceId);
try {
    log.info("Processing order {}", orderId);
} finally {
    MDC.clear();
}
```

输出示例：
```
[2024-01-15 10:23:45.123] [http-nio-8080-exec-1] INFO [1001#abc123] OrderService - Processing order 12345
```

---

## 5. 进阶用法 / 扩展点

### 5.1 自定义 Appender

实现自定义 Appender 对接日志平台（如 Kafka、ELK）：

```xml
<Appender name="KAFKA" class="com.example.log4j.KafkaAppender">
    <Property name="topic">app-logs</Property>
    <PatternLayout pattern="%m"/>
</Appender>

<asyncRoot level="INFO">
    <AppenderRef ref="KAFKA"/>
</asyncRoot>
```

### 5.2 按环境切换配置

Spring Boot 支持按 Profile 加载不同日志配置：

```yaml
---
spring:
  config:
    activate:
      on-profile: dev
logging:
  config: classpath:log4j2-dev.xml
---
spring:
  config:
    activate:
      on-profile: prod
logging:
  config: classpath:log4j2-prod.xml
```

### 5.3 动态调整日志级别

通过 Actuator 端点动态修改（需引入 `ddf-common-governance-starter`）：

```bash
curl -X POST "http://localhost:8080/actuator/loggers/com.ddf.boot.common" \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-mvc` | `UserContextUtil` 中的用户 ID 可通过 MDC 自动注入日志上下文 |
| `ddf-common-governance-starter` | Actuator 暴露 `/actuator/loggers` 端点，支持动态调整日志级别 |
| `ddf-common-starter-web` / `ddf-common-starter-default` | 默认使用 Logback；若需要 Log4j2，需排除 `spring-boot-starter-logging` 并引入本模块 |

---

## 7. FAQ

**Q1：引入后日志完全不输出怎么办？**  
1. 确认 `log4j2.xml` 放在 `src/main/resources` 目录下
2. 确认已排除 `spring-boot-starter-logging`（Logback）
3. 检查 `Configuration status="warn"` 是否有配置解析错误输出到控制台

**Q2：异步日志丢失了部分日志怎么办？**  
应用关闭时若 `RingBuffer` 中仍有未刷盘的事件，可能丢失。生产环境建议：
- 增加 `shutdownTimeout`（AsyncAppender 属性）
- 在 Spring 的 `ContextClosedEvent` 中休眠几百毫秒再退出

**Q3：日志文件没有按天滚动？**  
确认 `filePattern` 中包含日期格式（如 `%d{yyyy-MM-dd}`），且 `TimeBasedTriggeringPolicy interval="1"` 已配置。注意 `interval` 的单位跟随 `filePattern` 中最后一个时间单位。

**Q4：是否可以与 Logback 共存？**  
不建议。SLF4J 绑定只能有一个实现（`log4j-slf4j-impl` 或 `logback-classic`），同时存在会导致冲突。切换为 Log4j2 时务必排除 Logback。

**Q5：`includeLocation="false"` 有什么影响？**  
关闭位置信息（类名、行号）可提升异步日志性能约 5~10 倍。若排查问题需要行号，可设为 `true`，但会牺牲部分性能。

---

## 8. 参考

- 参考配置：`src/main/resources/log4j2_demo.xml`
- Log4j2 官方文档：https://logging.apache.org/log4j/2.x/manual/
- Disruptor 文档：https://lmax-exchange.github.io/disruptor/
