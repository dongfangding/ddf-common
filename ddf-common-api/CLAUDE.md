# CLAUDE.md

## 模块简介

提供 REST API 响应包装、异常定义和错误码接口，是整个项目的 API 基础。

## 核心类

| 类路径                                                             | 功能      |
|-----------------------------------------------------------------|---------|
| `com.ddf.boot.common.api.model.common.response.ResponseData<T>` | 统一响应包装类 |
| `com.ddf.boot.common.api.exception.BaseException`               | 异常基类    |
| `com.ddf.boot.common.api.exception.BusinessException`           | 业务异常    |
| `com.ddf.boot.common.api.exception.BaseCallbackCode`            | 错误码接口   |
| `com.ddf.boot.common.api.exception.BaseErrorCallbackCode`       | 预定义错误码  |

## 使用说明

### 1. 统一响应

```java
// 成功响应
ResponseData.success(data);
ResponseData.success(data, "操作成功");
ResponseData.empty();

// 失败响应
ResponseData.failure(BaseCallbackCode);
ResponseData.failure("CODE", "错误信息");

// 判断响应状态
if (responseData.isSuccess()) {
    T data = responseData.getData();
}

// 等同于，强制校验必须成功，然后获取数据。如果失败，会抛出数据里包装的异常
responseData.requireSuccess();

// 不强制校验响应是否成功，如果失败提供默认值，成功则获取原始值
responseData.failureDefault(data);

```

### 2. 抛出业务异常

```java
// 方式1：使用错误码枚举
throw new BusinessException(ErrorCodeEnum.XXX);

// 方式2：使用自定义错误码
throw new BusinessException("CODE", "自定义消息");

// 方式3：带格式化参数, 在错误枚举中定义的消息预留文案格式，如已超过最大数据{0}，不可以操作。
throw new BusinessException(BaseCallbackCode, param);
```

### 3. 定义错误码

```java
import lombok.Data;

public enum MyErrorCode implements BaseCallbackCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "用户不存在"),
    OVER_LIMIT("OVER_LIMIT", "已超过最大数据{0}，不可以操作"),
    PARAM_INVALID("PARAM_INVALID", "参数不合法");

    @Data
    private final String code;
    @Data
    private final String description;

    MyErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
```

## 预定义错误码

| 错误码 | 说明 |
|--------|------|
| `COMPLETE` | 请求成功 |
| `BAD_REQUEST` | 错误请求（会模糊异常详情） |
| `UNAUTHORIZED` | 未认证 |
| `ACCESS_FORBIDDEN` | 权限拒绝 |
| `SERVER_ERROR` | 服务端异常（会模糊异常详情） |
| `BIZ_EXCEPTION` | 业务异常 |

## 响应格式

```json
{
  "code": "200",
  "message": "请求成功",
  "subMessage": "详细错误信息（生产环境忽略）",
  "timestamp": 1700000000000,
  "data": { ... },
  "extra": { ... }
}
```

## 注意事项

1. **异常屏蔽**：生产环境 `isMaskErrorDetails()` 返回 `true` 的异常不会返回详细堆栈
2. **错误码规范**：错误码应保持稳定，避免频繁变更影响客户端
