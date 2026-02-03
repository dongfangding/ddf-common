# ddf-common-xxl-executor

XXL-JOB 执行器模块，提供定时任务执行功能。

## 功能特性

- 定时任务管理
- 任务调度
- 执行日志

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-xxl-executor</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径                | 功能   |
|--------------------|------|
| `XxlJobExecutor`   | 执行器  |
| `XxlJobProperties` | 配置属性 |

## 使用说明

### 配置

```yaml
ddf:
  xxl:
    executor:
      app-name: xxx-executor
      admin-address: http://localhost:8080/xxl-job-admin
      ip:
      port: 9999
```

### 定义任务

```java
@XxlJob("myTask")
public ReturnT<String> execute(String param) {
    // 任务逻辑
    return ReturnT.SUCCESS;
}
```
