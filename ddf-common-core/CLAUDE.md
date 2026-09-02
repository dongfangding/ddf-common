# CLAUDE.md

## 模块简介

提供核心功能配置和工具类，是其他所有模块的基础依赖。

## 核心类

| 类路径                                                             | 功能                    |
|-----------------------------------------------------------------|-----------------------|
| `com.ddf.boot.common.core.config.GlobalProperties`              | 全局配置属性                |
| `com.ddf.boot.common.core.config.GlobalApiConfig`               | API 基础配置              |
| `com.ddf.boot.common.core.util.SecureUtil`                      | 加密工具类（AES/RSA/BCrypt） |
| `com.ddf.boot.common.core.util.IdsUtil`                         | ID 生成工具               |
| `com.ddf.boot.common.core.helper.ThreadBuilderHelper`           | 线程池构建器                |
| `com.ddf.boot.common.core.helper.SpringContextHolder`           | Spring 上下文持有者         |
| `com.ddf.boot.common.core.authentication.TokenGenerator`        | Token 生成/校验策略接口       |
| `com.ddf.boot.common.core.authentication.DefaultTokenGenerator` | 默认 Token 生成实现         |
| `com.ddf.boot.common.core.authentication.TokenUtil`             | Token 工具类（已弃用）        |
| `com.ddf.boot.common.core.event.LoginSuccessEvent`              | 登录成功事件                |
| `com.ddf.boot.common.core.event.TokenRefreshEvent`              | token 刷新事件            |
| `com.ddf.boot.common.core.event.LoginFailureEvent`              | 登录失败事件                |

## 使用说明

### 1. 全局配置

```yaml
customizer:
  infra:
    global-properties:
      snowflake-worker-id: 1           # 雪花算法 workerId
      snowflake-data-center-id: 1      # 雪花算法 dataCenterId
      rsa-private-key: "..."           # RSA 私钥（必须配置）
      rsa-public-key: "..."            # RSA 公钥（必须配置）
      aes-secret: "32位随机密钥"         # AES 密钥（必须配置）
```

### 2. 线程池构建

```java
// 构建默认线程池
ThreadPoolTaskExecutor executor = ThreadBuilderHelper.buildThreadExecutor(
    "custom",    // 线程名前缀
    60,          // 空闲线程存活时间（秒）
    100          // 队列容量
);

// 构建自定义线程池
ThreadPoolTaskExecutor customExecutor = ThreadBuilderHelper.buildThreadExecutor(
    "custom",
    10,          // 核心线程数
    20,          // 最大线程数
    60,          // 空闲线程存活时间
    100          // 队列容量
);
```

### 3. 加密工具

```java
// AES 加密/解密
String encrypted = SecureUtil.encryptHexByAES("明文");
String decrypted = SecureUtil.decryptFromHexByAES(encrypted);

// BCrypt 密码加密
String encoded = SecureUtil.bCryptEncoder("password");
boolean matches = SecureUtil.bCryptMatch("password", encoded);

// RSA 加密/解密
String encrypted = SecureUtil.publicEncryptBcd(data);
String decrypted = SecureUtil.privateDecryptFromBcd(encryptedData);
```

### 4. ID 生成

```java
// 获取雪花算法 ID
long id = IdsUtil.getNextId();

// 获取字符串格式 ID
String idStr = IdsUtil.getNextStrId();
```

## 认证扩展点

### TokenGenerator

默认注册 `DefaultTokenGenerator`（AES 加密 + 可选 TokenCache），接入方注入自定义 `TokenGenerator` Bean 即可替换（`@ConditionalOnMissingBean(TokenGenerator.class)`）：

```java
@Component
public class MyTokenGenerator implements TokenGenerator {

    @Override
    public AuthenticateToken createToken(UserClaim userClaim) {
        // 生成 token
    }

    @Override
    public UserClaim getUserClaim(String token) {
        // 解析 token
    }

    @Override
    public AuthenticateCheckResult checkToken(String token) {
        // 校验 token
    }

    @Override
    public void refreshToken(String userId, String token) {
        // 刷新 token
    }
}
```

> `TokenUtil` 已 `@Deprecated`，内部委托给 `TokenGenerator`，新代码请注入 `TokenGenerator` 使用。

### 认证事件

| 事件类（`com.ddf.boot.common.core.event` 包） | 触发时机          | 关键字段                |
|-----------------------------------------|---------------|---------------------|
| `LoginSuccessEvent`                     | 登录成功/生成 token | `userClaim`         |
| `TokenRefreshEvent`                     | token 刷新      | `userId`            |
| `LoginFailureEvent`                     | 认证失败          | `token`、`errorCode` |

```java
@EventListener
public void onLoginSuccess(LoginSuccessEvent event) {
    UserClaim userClaim = event.getUserClaim();
    // 记录登录日志等
}
```

## 注意事项

1. **密钥配置**：生产环境必须通过配置中心管理密钥，禁止硬编码
2. **雪花算法**：多实例部署时确保 `workerId` 和 `dataCenterId` 唯一
3. **线程池**：使用 `ThreadBuilderHelper` 构建的线程池会自动注册优雅关闭
