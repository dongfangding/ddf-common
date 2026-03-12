# ddf-common-log4j

[English](./README.md) | [中文](./README.zh-CN.md)

Log4j2 日志接入模块。

## 当前定位

- 提供 `spring-boot-starter-log4j2` 依赖接入
- 提供异步日志所需的 `disruptor` 依赖
- 适合作为项目统一替换默认 Logback 的基础模块

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-log4j</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 使用建议

如果业务应用已引入默认 Web starter，需要排除 `spring-boot-starter-logging`：

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

## 说明

- 当前模块主要提供依赖与日志体系切换支持
- 具体日志文件切分、过滤器与格式规范仍建议在业务侧统一维护
