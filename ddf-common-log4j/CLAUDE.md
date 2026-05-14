# CLAUDE.md

## 模块简介

Log4j2 日志配置模块，提供异步日志和日志级别隔离功能。

## 核心配置

| 配置项           | 说明      |
|---------------|---------|
| `log4j2.xml`  | 日志配置文件  |
| `AsyncLogger` | 异步日志记录器 |

## 使用说明

### 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
</dependency>
```

### 异步配置

在启动脚本中添加：

```shell
-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
```

## 注意事项

1. **日志级别隔离**：使用 `Filters` 实现精确的日志级别隔离
2. **排除默认日志**：Web 项目需排除 `spring-boot-starter-logging`
