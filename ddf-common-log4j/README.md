# ddf-common-log4j

> Log4j2 logging integration module. Aggregates `spring-boot-starter-log4j2` + `disruptor` dependencies,
> replacing Spring Boot's default Logback stack with support for async logging, level isolation, and daily log rolling.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-log4j` solves the **"high-performance, structured log output"** problem.

| Category | Typical Problem | What the Module Provides |
| ----- | ----- | ----- |
| High-concurrency logging | Synchronous logging blocks business threads, reducing throughput | `disruptor` + `AsyncLogger` for fully async, lock-free log output |
| Log level separation | INFO / WARN / ERROR mixed in one file makes troubleshooting hard | `ThresholdFilter` routes each level to independent files |
| Distributed tracing | Logs lack user identity and trace IDs | Supports `%X{user_id}`, `%X{trace_id}` and other MDC variables |
| Log rotation | Log files grow unbounded, filling up disk | `TimeBasedTriggeringPolicy` daily rolling with 30-day retention |

---

## 2. Maven Dependency

Direct dependency:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-log4j</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> This module already excludes `spring-boot-starter-logging` (Logback), so no manual exclusion is needed on the consumer side.

If your application directly depends on `spring-boot-starter-web`, exclude the default logger:

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

## 3. Minimum Configuration

After adding the dependency, create `log4j2.xml` (or `log4j2-spring.xml`) in `src/main/resources` of your application.

The module provides a reference configuration `log4j2_demo.xml` with the following core structure:

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

### Enable fully-async mode

Add to JVM startup arguments:

```bash
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

Or in `application.yml`:

```yaml
logging:
  config: classpath:log4j2.xml
```

---

## 4. Core Capabilities

### 4.1 Asynchronous logging

This module includes `disruptor`, enabling fully async logging via `asyncRoot` / `AsyncAppender`:

- Business threads only write log events to the `RingBuffer`, without waiting for disk I/O
- A dedicated background thread handles batch disk flushing
- Ideal for high-concurrency scenarios, significantly improving throughput

### 4.2 Level isolation

Routes different log levels to separate files via `ThresholdFilter`:

| Appender | Filter level | Purpose |
| ----- | ----- | ----- |
| `INFO_FILE` | `INFO`+ | Full logs, retained for 30 days |
| `WARN_FILE` | `WARN`+ | Alert logs for quick issue localization |
| `ERROR_FILE` | `ERROR`+ | Error logs for alerting system integration |

### 4.3 MDC variables

Supports MDC context variables in the pattern:

```xml
<Property name="FILE_LOG_PATTERN">
  [%d{yyyy-MM-dd HH:mm:ss.SSS}] [%t] %-5p [%X{user_id}#%X{trace_id}] %c{1.} - %msg%n
</Property>
```

Set MDC in business code:

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

Output example:
```
[2024-01-15 10:23:45.123] [http-nio-8080-exec-1] INFO [1001#abc123] OrderService - Processing order 12345
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Custom Appender

Implement a custom Appender to integrate with log platforms (e.g. Kafka, ELK):

```xml
<Appender name="KAFKA" class="com.example.log4j.KafkaAppender">
    <Property name="topic">app-logs</Property>
    <PatternLayout pattern="%m"/>
</Appender>

<asyncRoot level="INFO">
    <AppenderRef ref="KAFKA"/>
</asyncRoot>
```

### 5.2 Environment-specific configuration

Spring Boot supports profile-specific log configurations:

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

### 5.3 Dynamic log level adjustment

Modify levels dynamically via Actuator endpoint (requires `ddf-common-governance-starter`):

```bash
curl -X POST "http://localhost:8080/actuator/loggers/com.ddf.boot.common" \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| ----- | ----- |
| `ddf-common-mvc` | User ID from `UserContextUtil` can be injected into the log context via MDC |
| `ddf-common-governance-starter` | Actuator exposes `/actuator/loggers` endpoint for dynamic level adjustment |
| `ddf-common-starter-web` / `ddf-common-starter-default` | Defaults to Logback; to use Log4j2, exclude `spring-boot-starter-logging` and import this module |

---

## 7. FAQ

**Q1: No logs are output after importing this module?**  
1. Confirm `log4j2.xml` is placed under `src/main/resources`
2. Confirm `spring-boot-starter-logging` (Logback) has been excluded
3. Check `Configuration status="warn"` for configuration parsing errors printed to console

**Q2: Some logs are lost with async logging?**  
If the `RingBuffer` still has unflushed events at application shutdown, they may be lost. For production:
- Increase `shutdownTimeout` (AsyncAppender attribute)
- Sleep for a few hundred milliseconds in Spring's `ContextClosedEvent` before exiting

**Q3: Log files are not rolling daily?**  
Confirm `filePattern` contains a date format (e.g. `%d{yyyy-MM-dd}`), and `TimeBasedTriggeringPolicy interval="1"` is configured. Note that `interval` unit follows the last time unit in `filePattern`.

**Q4: Can Log4j2 coexist with Logback?**  
Not recommended. SLF4J can only bind to one implementation (`log4j-slf4j-impl` or `logback-classic`). Having both causes conflicts. When switching to Log4j2, always exclude Logback.

**Q5: What is the impact of `includeLocation="false"`?**  
Disabling location info (class name, line number) improves async logging performance by ~5-10x. If you need line numbers for troubleshooting, set it to `true`, but this sacrifices some performance.

---

## 8. References

- Reference config: `src/main/resources/log4j2_demo.xml`
- Log4j2 docs: https://logging.apache.org/log4j/2.x/manual/
- Disruptor docs: https://lmax-exchange.github.io/disruptor/
