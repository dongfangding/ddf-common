# ddf-common-mvc

MVC 相关工具模块，提供 Web 开发常用工具。

## 功能特性

- 全局异常处理
- 跨域配置
- 请求日志

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-mvc</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 核心类

| 类路径 | 功能 |
|-------|------|
| `AbstractExceptionHandler` | 异常处理器 |
| `GlobalCorsConfig` | 跨域配置 |
| `WebUtil` | Web 工具类 |
| `AopUtil` | AOP 工具类 |

## 使用说明

自动配置生效，无需额外配置即可使用。
