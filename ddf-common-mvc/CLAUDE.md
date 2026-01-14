# CLAUDE.md

## 模块简介

提供 MVC 相关配置，包括全局异常处理、参数解析器、跨域配置等。

## 核心类

| 类路径                                                             | 功能       |
|-----------------------------------------------------------------|----------|
| `com.ddf.boot.common.mvc.config.CoreWebConfig`                  | Web 核心配置 |
| `com.ddf.boot.common.mvc.exception200.AbstractExceptionHandler` | 异常处理器    |
| `com.ddf.boot.common.mvc.filter.CachingRequestBodyFilter`       | 请求体缓存过滤器 |

## 功能说明

### 1. 全局异常处理

```java
// 异常会被自动捕获并返回统一格式
throw new BusinessException(ErrorCodeEnum.XXX);
throw new BadRequestException("参数错误");
throw new ServerErrorException("系统错误");
```

### 2. 请求体缓存

支持多次读取请求体（用于签名验证等场景）：

```java
// CachingRequestBodyFilter 自动处理
// 第一次读取后缓存在 ThreadLocal 中
```

### 3. 跨域配置

```java
// 通过 CoreWebConfig 配置（已标记废弃）
// 建议使用 Spring Cloud Gateway 或 Nginx 处理跨域
```

## 已启用功能

| 功能                             | 说明     |
|--------------------------------|--------|
| `@EnableTransactionManagement` | 事务管理   |
| `@EnableAspectJAutoProxy`      | AOP 支持 |
| `@EnableAsync`                 | 异步支持   |
| `@EnableScheduling`            | 定时任务   |
| `@EnableCaching`               | 缓存支持   |

## 注意事项

1. **跨域处理**：建议在网关层处理跨域，而非应用层
2. **异常屏蔽**：生产环境部分异常会屏蔽详细信息
3. **线程池**：异步任务使用 `ThreadBuilderHelper` 构建的默认线程池
