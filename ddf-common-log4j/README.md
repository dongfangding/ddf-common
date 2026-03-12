# ddf-common-log4j

[English](./README.md) | [中文](./README.zh-CN.md)

Log4j2 logging integration module.

## Current Positioning

- Provides `spring-boot-starter-log4j2` dependency integration
- Provides the `disruptor` dependency required for asynchronous logging
- Works as the base module for replacing Spring Boot's default Logback stack

## Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-log4j</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Usage Recommendation

If the business application already imports the default web starter, exclude `spring-boot-starter-logging`:

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

## Notes

- This module mainly provides dependency wiring and logging stack switching support
- Concrete log file splitting, filters, and formatting conventions should still be maintained centrally on the business side
