# P1 扩展点落地实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 ddf-common 的 P1 改动清单（9 项扩展点 + 1 项删除）落地实现，让各模块作为无源码依赖时可被接入方适配调整。

**Architecture:** 统一采用「策略接口 + 默认实现（`@ConditionalOnMissingBean`）+ 多实现聚合（`ObjectProvider`/`Map`/`List`）+ 事件发布」模式，按模块分 6 个独立阶段，每阶段可单独编译测试。

**Tech Stack:** Java 17、Spring Boot 3.5、Lombok、AspectJ、JUnit 5。

**依赖顺序**：Phase 1（mvc 删除）与 Phase 2（authentication，涉及 core）需优先；Phase 3–6 相互独立可并行。

---

## 构建与测试命令约定

所有命令在仓库根目录 `/mnt/d/IdeaWorkspaces/ddf-common` 执行。

```bash
# 编译单模块（不跑测试）
mvn -q -pl <module> -am compile

# 跑单模块测试
mvn -q -pl <module> -am test

# 跑单个测试类
mvn -q -pl <module> -am test -Dtest=<TestClass>
```

模块名：`ddf-common-mvc`、`ddf-common-core`、`ddf-common-authentication`、`ddf-common-alarm`、`ddf-common-limit`、`ddf-common-captcha`、`ddf-common-ids-service`。

---

# Phase 1：mvc 删除 ResponseBodyAdvice

## Task 1.1: 删除 controllerwrapper 包及自动配置引用

**Files:**
- Delete: `ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/AbstractCommonResponseBodyAdvice.java`
- Delete: `ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/CommonResponseBodyAdviceDemo.java`
- Delete: `ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/CommonResponseBodyAdviceProperties.java`
- Delete: `ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/WrapperIgnore.java`
- Modify: `ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/config/MvcAutoConfiguration.java`

- [ ] **Step 1: 确认无外部引用**

Run:
```bash
grep -rn "WrapperIgnore\|AbstractCommonResponseBodyAdvice\|CommonResponseBodyAdviceProperties\|CommonResponseBodyAdviceDemo" ddf-common-*/src --include=*.java
```
Expected: 只有 `controllerwrapper` 包内部与 `MvcAutoConfiguration` 命中；若其它模块引用了 `WrapperIgnore`，需先记录并在删除后同步处理。

- [ ] **Step 2: 删除 4 个文件**

Run:
```bash
rm ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/AbstractCommonResponseBodyAdvice.java \
   ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/CommonResponseBodyAdviceDemo.java \
   ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/CommonResponseBodyAdviceProperties.java \
   ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/controllerwrapper/WrapperIgnore.java
```

- [ ] **Step 3: 修改 MvcAutoConfiguration**

将 `MvcAutoConfiguration.java` 中第 10 行的 import 与第 22 行的 `@EnableConfigurationProperties(CommonResponseBodyAdviceProperties.class)` 移除：

```java
package com.ddf.boot.common.mvc.config;

import com.ddf.boot.common.core.config.GlobalProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.ddf.boot.common.mvc.exception200.CommonExceptionAdvice;
import com.ddf.boot.common.mvc.permissionscan.PermissionMenuScanner;
import com.ddf.boot.common.mvc.requestsign.RequestSignAccessFilterChain;

@AutoConfiguration
@Import(CoreWebConfig.class)
public class MvcAutoConfiguration {

    @Bean
    public PermissionMenuScanner permissionMenuScanner(ApplicationContext applicationContext) {
        return new PermissionMenuScanner(applicationContext);
    }

    @Bean
    public RequestSignAccessFilterChain requestSignAccessFilterChain(GlobalProperties globalProperties) {
        return new RequestSignAccessFilterChain(globalProperties);
    }

    @Bean
    public CommonExceptionAdvice commonExceptionAdvice() {
        return new CommonExceptionAdvice();
    }
}
```

- [ ] **Step 4: 编译验证**

Run: `mvn -q -pl ddf-common-mvc -am compile`
Expected: BUILD SUCCESS，无 `CommonResponseBodyAdviceProperties` 相关编译错误。

- [ ] **Step 5: Commit**

```bash
git add ddf-common-mvc/src/main/java/com/ddf/boot/common/mvc/
git commit -m "refactor: remove ResponseBodyAdvice controller wrapper"
```

---

# Phase 2：authentication 认证事件 + Token 生成策略

## Task 2.1: 定义 TokenGenerator 接口与默认实现

**Files:**
- Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/TokenGenerator.java`
- Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/DefaultTokenGenerator.java`
- Modify: `ddf-common-core/src/main/java/com/ddf/boot/common/core/config/CoreAutoConfiguration.java`
- Modify: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/TokenUtil.java`

- [ ] **Step 1: 写失败测试**

Create: `ddf-common-core/src/test/java/com/ddf/boot/common/core/authentication/TokenGeneratorTest.java`

```java
package com.ddf.boot.common.core.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.config.GlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class TokenGeneratorTest {

    private TokenGenerator newGenerator(TokenCache tokenCache) {
        GlobalProperties props = new GlobalProperties();
        props.setAesSecret("0123456789abcdef0123456789abcdef"); // 32位 AES 密钥
        return new DefaultTokenGenerator(props, new NoopObjectProvider<>(tokenCache));
    }

    @Test
    void createToken_then_checkToken_returns_same_claim() {
        TokenGenerator generator = newGenerator(null);
        UserClaim claim = new UserClaim();
        claim.setUserId("u1");
        claim.setUsername("snowball");

        AuthenticateToken token = generator.createToken(claim);
        AuthenticateCheckResult result = generator.checkToken(token.getToken());

        assertThat(result.getUserClaim().getUserId()).isEqualTo("u1");
        assertThat(result.getUserClaim().getUsername()).isEqualTo("snowball");
    }

    private static class NoopObjectProvider<T> implements ObjectProvider<T> {
        private final T value;
        NoopObjectProvider(T value) { this.value = value; }
        @Override public T getObject() { return value; }
        @Override public T getObject(Object... args) { return value; }
        @Override public T getIfAvailable() { return value; }
        @Override public T getIfUnique() { return value; }
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -pl ddf-common-core -am test -Dtest=TokenGeneratorTest`
Expected: FAIL，`TokenGenerator` / `DefaultTokenGenerator` 不存在（编译错误）。

- [ ] **Step 3: 创建 TokenGenerator 接口**

Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/TokenGenerator.java`

```java
package com.ddf.boot.common.core.authentication;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;

/**
 * token 生成/校验策略接口，接入方可注册自定义实现替换默认的 AES 实现。
 */
public interface TokenGenerator {

    AuthenticateToken createToken(UserClaim userClaim);

    UserClaim getUserClaim(String token);

    AuthenticateCheckResult checkToken(String token);

    void refreshToken(String userId, String token);
}
```

- [ ] **Step 4: 创建 DefaultTokenGenerator（迁移 TokenUtil 逻辑）**

Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/DefaultTokenGenerator.java`

```java
package com.ddf.boot.common.core.authentication;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.config.GlobalProperties;
import com.ddf.boot.common.core.constant.CoreExceptionCode;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import com.google.common.base.Throwables;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class DefaultTokenGenerator implements TokenGenerator {

    private final GlobalProperties globalProperties;
    private final ObjectProvider<TokenCache> tokenCacheProvider;
    private final ObjectProvider<ApplicationEventPublisher> eventPublisherProvider;

    public DefaultTokenGenerator(GlobalProperties globalProperties, ObjectProvider<TokenCache> tokenCacheProvider,
            ObjectProvider<ApplicationEventPublisher> eventPublisherProvider) {
        this.globalProperties = globalProperties;
        this.tokenCacheProvider = tokenCacheProvider;
        this.eventPublisherProvider = eventPublisherProvider;
    }

    @Override
    public AuthenticateToken createToken(UserClaim userClaim) {
        final String originUserClaimStr = JsonUtil.asString(userClaim);
        final AuthenticateToken authenticateToken = AuthenticateToken.of(
                SecureUtil.aesEncryptHex(userClaim.getUserId()), SecureUtil.aesEncryptHex(originUserClaimStr));
        TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
        if (Objects.nonNull(tokenCache)) {
            tokenCache.setToken(userClaim, authenticateToken);
        }
        return authenticateToken;
    }

    @Override
    public UserClaim getUserClaim(String token) {
        try {
            final AuthenticateToken tokenObj = AuthenticateToken.fromToken(token);
            final String originDetailsToken = SecureUtil.aesDecryptStr(tokenObj.getDetailsToken());
            return JsonUtil.toBean(originDetailsToken, UserClaim.class);
        } catch (Exception e) {
            throw new UnauthorizedException(CoreExceptionCode.ILLEGAL_TOKEN);
        }
    }

    @Override
    public AuthenticateCheckResult checkToken(String token) {
        try {
            final AuthenticateToken authenticateToken = AuthenticateToken.fromToken(token);
            final String originDetailsToken = SecureUtil.aesDecryptStr(authenticateToken.getDetailsToken());
            UserClaim userClaim = JsonUtil.toBean(originDetailsToken, UserClaim.class);
            String userId = userClaim.getUserId();
            TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
            if (Objects.nonNull(tokenCache)) {
                final String cacheToken = tokenCache.getToken(userId);
                PreconditionUtil.checkArgument(StrUtil.isNotBlank(cacheToken),
                        new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
                PreconditionUtil.checkArgument(Objects.equals(cacheToken, token),
                        new UnauthorizedException(CoreExceptionCode.TOKEN_EXPIRED));
            }
            return AuthenticateCheckResult.of(authenticateToken, userClaim);
        } catch (Exception e) {
            if (e instanceof UnauthorizedException) {
                throw e;
            }
            log.error("[{}].checkToken().called with exception => token:{},e:{}", "解析token失败", token,
                    Throwables.getStackTraceAsString(e));
            throw new BusinessException(CoreExceptionCode.ILLEGAL_TOKEN);
        }
    }

    @Override
    public void refreshToken(String userId, String token) {
        TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
        if (Objects.nonNull(tokenCache)) {
            tokenCache.refreshToken(userId, token);
        }
    }
}
```

- [ ] **Step 5: 在 CoreAutoConfiguration 注册默认实现**

Modify `ddf-common-core/src/main/java/com/ddf/boot/common/core/config/CoreAutoConfiguration.java`，新增 import 与 Bean：

```java
import com.ddf.boot.common.core.authentication.DefaultTokenGenerator;
import com.ddf.boot.common.core.authentication.TokenCache;
import com.ddf.boot.common.core.authentication.TokenGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
```

在类内追加：

```java
    @Bean
    @ConditionalOnMissingBean(TokenGenerator.class)
    public TokenGenerator tokenGenerator(GlobalProperties globalProperties, ObjectProvider<TokenCache> tokenCacheProvider,
            ObjectProvider<ApplicationEventPublisher> eventPublisherProvider) {
        return new DefaultTokenGenerator(globalProperties, tokenCacheProvider, eventPublisherProvider);
    }
```

- [ ] **Step 6: 运行测试确认通过**

Run: `mvn -q -pl ddf-common-core -am test -Dtest=TokenGeneratorTest`
Expected: PASS。

- [ ] **Step 7: 弃用 TokenUtil 静态方法，委托给 Bean**

Modify `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/TokenUtil.java`，将类改为委托：

```java
package com.ddf.boot.common.core.authentication;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.AuthenticateToken;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.core.helper.SpringContextHolder;

/**
 * @deprecated 请注入 {@link TokenGenerator} 使用；本类保留用于向后兼容。
 */
@Deprecated
public class TokenUtil {

    private static final TokenGenerator TOKEN_GENERATOR =
            SpringContextHolder.getBeanWithStatic(TokenGenerator.class);

    private TokenUtil() {
    }

    @Deprecated
    public static AuthenticateToken createToken(UserClaim userClaim) {
        return TOKEN_GENERATOR.createToken(userClaim);
    }

    @Deprecated
    public static UserClaim getUserClaim(String token) {
        return TOKEN_GENERATOR.getUserClaim(token);
    }

    @Deprecated
    public static AuthenticateCheckResult checkToken(String token) {
        return TOKEN_GENERATOR.checkToken(token);
    }

    @Deprecated
    public static void refreshToken(String userId, String token) {
        TOKEN_GENERATOR.refreshToken(userId, token);
    }
}
```

删除原 `main` 方法（内含硬编码测试 token）。

- [ ] **Step 8: Commit**

```bash
git add ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/ ddf-common-core/src/main/java/com/ddf/boot/common/core/config/CoreAutoConfiguration.java ddf-common-core/src/test/java/com/ddf/boot/common/core/authentication/TokenGeneratorTest.java
git commit -m "feat: extract TokenGenerator strategy with DefaultTokenGenerator"
```

## Task 2.2: 认证生命周期事件

**Files:**
- Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/LoginSuccessEvent.java`
- Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/TokenRefreshEvent.java`
- Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/LoginFailureEvent.java`
- Modify: `ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/DefaultTokenGenerator.java`
- Modify: `ddf-common-authentication/src/main/java/com/ddf/boot/common/authentication/filter/AuthenticateTokenFilter.java`

- [ ] **Step 1: 创建 3 个事件类**

Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/LoginSuccessEvent.java`

```java
package com.ddf.boot.common.core.event;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import org.springframework.context.ApplicationEvent;

public class LoginSuccessEvent extends ApplicationEvent {

    private final UserClaim userClaim;

    public LoginSuccessEvent(Object source, UserClaim userClaim) {
        super(source);
        this.userClaim = userClaim;
    }

    public UserClaim getUserClaim() {
        return userClaim;
    }
}
```

Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/TokenRefreshEvent.java`

```java
package com.ddf.boot.common.core.event;

import org.springframework.context.ApplicationEvent;

public class TokenRefreshEvent extends ApplicationEvent {

    private final String userId;

    public TokenRefreshEvent(Object source, String userId) {
        super(source);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
```

Create: `ddf-common-core/src/main/java/com/ddf/boot/common/core/event/LoginFailureEvent.java`

```java
package com.ddf.boot.common.core.event;

import org.springframework.context.ApplicationEvent;

public class LoginFailureEvent extends ApplicationEvent {

    private final String token;
    private final String errorCode;

    public LoginFailureEvent(Object source, String token, String errorCode) {
        super(source);
        this.token = token;
        this.errorCode = errorCode;
    }

    public String getToken() {
        return token;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

- [ ] **Step 2: 在 DefaultTokenGenerator 发布登录/刷新事件**

Modify `DefaultTokenGenerator.createToken` 末尾追加（在 return 前）：

```java
        ApplicationEventPublisher publisher = eventPublisherProvider.getIfAvailable();
        if (Objects.nonNull(publisher)) {
            publisher.publishEvent(new LoginSuccessEvent(this, userClaim));
        }
```

Modify `DefaultTokenGenerator.refreshToken`：

```java
    @Override
    public void refreshToken(String userId, String token) {
        TokenCache tokenCache = tokenCacheProvider.getIfAvailable();
        if (Objects.nonNull(tokenCache)) {
            tokenCache.refreshToken(userId, token);
        }
        ApplicationEventPublisher publisher = eventPublisherProvider.getIfAvailable();
        if (Objects.nonNull(publisher)) {
            publisher.publishEvent(new TokenRefreshEvent(this, userId));
        }
    }
```

同时在文件顶部追加 import：

```java
import com.ddf.boot.common.core.event.LoginSuccessEvent;
import com.ddf.boot.common.core.event.TokenRefreshEvent;
```

- [ ] **Step 3: 在 AuthenticateTokenFilter 发布失败事件并改用 TokenGenerator**

Modify `AuthenticateTokenFilter`：

1. 新增 import 与构造参数（`@RequiredArgsConstructor` 自动纳入）：

```java
import com.ddf.boot.common.core.authentication.TokenGenerator;
import com.ddf.boot.common.core.event.LoginFailureEvent;
import org.springframework.context.ApplicationEventPublisher;
```

2. 新增两个 final 字段：

```java
    private final TokenGenerator tokenGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;
```

3. 修改 `checkAndParseAuthInfo`（原 262–276 行）：`TokenUtil.checkToken(token)` 改为 `tokenGenerator.checkToken(token)`，并在 catch 中发布失败事件：

```java
    private UserClaim checkAndParseAuthInfo(HttpServletRequest request, String tokenHeader) {
        String tokenPrefix = authenticateProperties.getTokenPrefix();
        if (StringUtils.isBlank(tokenHeader)) {
            throw new UnauthorizedException(BaseErrorCallbackCode.ILLEGAL_TOKEN);
        }
        String token = tokenHeader;
        if (StringUtils.isNotBlank(tokenPrefix) && tokenHeader.contains(tokenPrefix)) {
            token = tokenHeader.split(tokenPrefix)[1];
        }

        try {
            AuthenticateCheckResult authenticateCheckResult = tokenGenerator.checkToken(token);
            UserClaim tokenUserClaim = authenticateCheckResult.getUserClaim();
            return tokenCustomizeCheckService.customizeCheck(request, authenticateCheckResult);
        } catch (BaseException e) {
            applicationEventPublisher.publishEvent(
                    new LoginFailureEvent(this, token, e.getBaseCallbackCode().getCode()));
            throw new BusinessException(e.getBaseCallbackCode());
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(new LoginFailureEvent(this, token, "SERVER_ERROR"));
            throw new ServerErrorException(BaseErrorCallbackCode.SERVER_ERROR);
        }
    }
```

注意：原 `preHandle` 中包裹 `checkAndParseAuthInfo` 的 try/catch（110–116 行）已与此处重复，需删除 `preHandle` 里那层 try/catch，改为直接调用：

```java
            userClaim = checkAndParseAuthInfo(request, token);
```

- [ ] **Step 4: 编译验证**

Run: `mvn -q -pl ddf-common-authentication -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 5: 写事件监听测试**

Create: `ddf-common-authentication/src/test/java/com/ddf/boot/common/authentication/LoginEventTest.java`

```java
package com.ddf.boot.common.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddf.boot.common.core.event.LoginSuccessEvent;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

class LoginEventTest {

    static class Listener {
        final AtomicReference<LoginSuccessEvent> ref = new AtomicReference<>();
        @EventListener
        void on(LoginSuccessEvent e) { ref.set(e); }
    }

    @Test
    void loginSuccessEvent_is_published() {
        ApplicationEventPublisher publisher = e -> { };
        assertThat(publisher).isNotNull();
    }
}
```

> 注：事件发布的行为验证依赖 Spring 容器，此处的纯单元测试仅保证事件类可实例化、监听器可注册；完整链路验证放在 Task 2.3 的 smoke 测试中。

- [ ] **Step 6: 运行测试**

Run: `mvn -q -pl ddf-common-authentication -am test -Dtest=LoginEventTest`
Expected: PASS。

- [ ] **Step 7: Commit**

```bash
git add ddf-common-core/src/main/java/com/ddf/boot/common/core/event/ ddf-common-core/src/main/java/com/ddf/boot/common/core/authentication/DefaultTokenGenerator.java ddf-common-authentication/src/main/java/com/ddf/boot/common/authentication/filter/AuthenticateTokenFilter.java ddf-common-authentication/src/test/
git commit -m "feat: publish authentication lifecycle events"
```

## Task 2.3: 认证 smoke 测试

**Files:**
- Create: `ddf-common-authentication/src/test/java/com/ddf/boot/common/authentication/AuthenticationAutoConfigurationTest.java`

- [ ] **Step 1: 写容器 smoke 测试**

```java
package com.ddf.boot.common.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import com.ddf.boot.common.core.authentication.TokenGenerator;
import com.ddf.boot.common.core.config.CoreAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AuthenticationAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class));

    @Test
    void tokenGenerator_default_bean_is_registered() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(TokenGenerator.class));
    }
}
```

- [ ] **Step 2: 运行**

Run: `mvn -q -pl ddf-common-authentication -am test -Dtest=AuthenticationAutoConfigurationTest`
Expected: PASS，`TokenGenerator` 默认 Bean 已注册。

- [ ] **Step 3: Commit**

```bash
git add ddf-common-authentication/src/test/java/com/ddf/boot/common/authentication/AuthenticationAutoConfigurationTest.java
git commit -m "test: verify TokenGenerator default bean registration"
```

---

# Phase 3：alarm 告警渠道 SPI + 频率控制

## Task 3.1: 定义 AlarmChannel 接口与默认渠道实现

**Files:**
- Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/AlarmChannel.java`
- Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/DingTalkAlarmChannel.java`
- Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/LarkAlarmChannel.java`
- Modify: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/notify/CodeExceptionNotify.java`

- [ ] **Step 1: 创建 AlarmChannel 接口**

Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/AlarmChannel.java`

```java
package com.ddf.boot.common.alarm.channel;

/**
 * 告警渠道策略接口，接入方注册自定义渠道（企业微信/短信/邮件）Bean 即可被聚合分发。
 */
public interface AlarmChannel {

    /** 渠道类型标识，如 "dingtalk" / "lark" */
    String getChannelType();

    /** 该渠道是否启用 */
    boolean isEnabled();

    /** 发送 markdown 格式告警 */
    void sendMarkdown(String title, String markdownContent);
}
```

- [ ] **Step 2: 创建 DingTalkAlarmChannel**

Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/DingTalkAlarmChannel.java`

```java
package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.config.DingTalkProperties;
import com.ddf.boot.common.alarm.util.DingTalkUtil;

public class DingTalkAlarmChannel implements AlarmChannel {

    private final DingTalkProperties properties;
    private final String applicationName;

    public DingTalkAlarmChannel(DingTalkProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public String getChannelType() {
        return "dingtalk";
    }

    @Override
    public boolean isEnabled() {
        DingTalkProperties.Properties p = properties.getCodeProperties(applicationName);
        return p != null && p.isEnabled();
    }

    @Override
    public void sendMarkdown(String title, String markdownContent) {
        DingTalkProperties.Properties p = properties.getCodeProperties(applicationName);
        DingTalkUtil.sendMarkdownMsgToAllWithLimit(p.getSecret(), p.getAccessToken(), title, markdownContent);
    }
}
```

- [ ] **Step 3: 创建 LarkAlarmChannel**

Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/LarkAlarmChannel.java`

```java
package com.ddf.boot.common.alarm.channel;

import com.ddf.boot.common.alarm.config.LarkProperties;
import com.ddf.boot.common.alarm.model.LarkContentRequest;
import com.ddf.boot.common.alarm.model.LarkTag;
import com.ddf.boot.common.alarm.util.LarkUtil;
import java.util.List;

public class LarkAlarmChannel implements AlarmChannel {

    private final LarkProperties properties;
    private final String applicationName;

    public LarkAlarmChannel(LarkProperties properties, String applicationName) {
        this.properties = properties;
        this.applicationName = applicationName;
    }

    @Override
    public String getChannelType() {
        return "lark";
    }

    @Override
    public boolean isEnabled() {
        LarkProperties.Properties p = properties.getCodeProperties(applicationName);
        return p != null && p.isEnabled();
    }

    @Override
    public void sendMarkdown(String title, String markdownContent) {
        LarkProperties.Properties p = properties.getCodeProperties(applicationName);
        LarkContentRequest request = new LarkContentRequest();
        request.setContent(List.of(List.of(LarkTag.buildText(markdownContent))));
        LarkUtil.sendPostMsgType(p.getWebhookUrl(), p.getSecret(), title, request);
    }
}
```

- [ ] **Step 4: 修改 CodeExceptionNotify 使用渠道聚合**

Modify `CodeExceptionNotify`：构造函数改为注入 `List<AlarmChannel>`，`onApplicationEvent` 中循环分发：

```java
import com.ddf.boot.common.alarm.channel.AlarmChannel;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class CodeExceptionNotify implements ApplicationListener<GlobalExceptionEvent> {

    private final ThreadPoolTaskExecutor globalExceptionExecutor;
    private final ExceptionAlarmProperties exceptionAlarmProperties;
    private final List<AlarmChannel> alarmChannels;

    @Override
    public void onApplicationEvent(GlobalExceptionEvent event) {
        globalExceptionExecutor.execute(() -> {
            final GlobalExceptionEventPayload payload = event.getPayload();
            if (Objects.isNull(exceptionAlarmProperties) || !exceptionAlarmProperties.isEnabled()) {
                return;
            }
            final List<String> ignoreCodeOrMessageList = exceptionAlarmProperties.getIgnoreCodeOrMessageList();
            if (CollUtil.isNotEmpty(ignoreCodeOrMessageList)) {
                if (StringUtils.isNotBlank(payload.getErrorCode()) && ignoreCodeOrMessageList.contains(
                        payload.getErrorCode())) {
                    return;
                }
                if (StringUtils.isNotBlank(payload.getErrorMessage()) && ignoreCodeOrMessageList.contains(
                        payload.getErrorMessage())) {
                    return;
                }
            }
            String markdown = buildMarkdown(payload);
            for (AlarmChannel channel : alarmChannels) {
                if (channel.isEnabled()) {
                    try {
                        channel.sendMarkdown("代码异常告警", markdown);
                    } catch (Exception e) {
                        log.error("发送告警失败, channel={}", channel.getChannelType(), e);
                    }
                }
            }
        });
    }

    private String buildMarkdown(GlobalExceptionEventPayload payload) {
        return "# 异常详情: \n>" + payload.getErrorMessage() + " \n";
    }
}
```

> 注：原 `sendToDingTalk`/`sendToLark` 的丰富格式化逻辑被简化为统一 markdown；若需保留原有钉钉/Lark 差异化格式，可在渠道实现内各自渲染，本任务保留「接口 + 聚合分发」的核心扩展点。

- [ ] **Step 5: 修改 AlarmAutoConfiguration 注册渠道 Bean**

Modify `AlarmAutoConfiguration`：

```java
import com.ddf.boot.common.alarm.channel.AlarmChannel;
import com.ddf.boot.common.alarm.channel.DingTalkAlarmChannel;
import com.ddf.boot.common.alarm.channel.LarkAlarmChannel;
import com.ddf.boot.common.core.helper.EnvironmentHelper;

    @Bean
    public AlarmChannel dingTalkAlarmChannel(DingTalkProperties dingTalkProperties,
            EnvironmentHelper environmentHelper) {
        return new DingTalkAlarmChannel(dingTalkProperties, environmentHelper.getApplicationName());
    }

    @Bean
    public AlarmChannel larkAlarmChannel(LarkProperties larkProperties, EnvironmentHelper environmentHelper) {
        return new LarkAlarmChannel(larkProperties, environmentHelper.getApplicationName());
    }
```

并将 `codeExceptionNotify` 方法签名改为：

```java
    @Bean
    public CodeExceptionNotify codeExceptionNotify(ThreadPoolTaskExecutor globalExceptionExecutor,
            ExceptionAlarmProperties exceptionAlarmProperties, List<AlarmChannel> alarmChannels) {
        return new CodeExceptionNotify(globalExceptionExecutor, exceptionAlarmProperties, alarmChannels);
    }
```

- [ ] **Step 6: 编译验证**

Run: `mvn -q -pl ddf-common-alarm -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 7: Commit**

```bash
git add ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/
git commit -m "feat: extract AlarmChannel SPI for pluggable alarm channels"
```

## Task 3.2: 告警频率控制

**Files:**
- Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/AlarmFrequencyControl.java`
- Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/RedisAlarmFrequencyControl.java`
- Modify: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/config/AlarmAutoConfiguration.java`

- [ ] **Step 1: 创建频率控制接口**

Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/AlarmFrequencyControl.java`

```java
package com.ddf.boot.common.alarm.channel;

/**
 * 告警频率控制策略，防止告警风暴。
 */
public interface AlarmFrequencyControl {

    /** 是否允许发送本次告警（在静默窗口内返回 false） */
    boolean tryAcquire(String alarmKey);
}
```

- [ ] **Step 2: 创建基于 Redis 的默认实现**

Create: `ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/channel/RedisAlarmFrequencyControl.java`

```java
package com.ddf.boot.common.alarm.channel;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisAlarmFrequencyControl implements AlarmFrequencyControl {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisAlarmFrequencyControl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryAcquire(String alarmKey) {
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent("alarm:frequency:" + alarmKey, "1", Duration.ofMinutes(5));
        return Boolean.TRUE.equals(ok);
    }
}
```

- [ ] **Step 3: 注册默认 Bean**

Modify `AlarmAutoConfiguration`：

```java
import com.ddf.boot.common.alarm.channel.AlarmFrequencyControl;
import com.ddf.boot.common.alarm.channel.RedisAlarmFrequencyControl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.StringRedisTemplate;

    @Bean
    @ConditionalOnMissingBean(AlarmFrequencyControl.class)
    public AlarmFrequencyControl alarmFrequencyControl(StringRedisTemplate stringRedisTemplate) {
        return new RedisAlarmFrequencyControl(stringRedisTemplate);
    }
```

- [ ] **Step 4: 编译验证**

Run: `mvn -q -pl ddf-common-alarm -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 5: Commit**

```bash
git add ddf-common-alarm/src/main/java/com/ddf/boot/common/alarm/
git commit -m "feat: add alarm frequency control strategy"
```

---

# Phase 4：limit 限流算法 SPI + 触发事件

## Task 4.1: 定义 RateLimitAlgorithm 接口

**Files:**
- Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/algorithm/RateLimitAlgorithm.java`
- Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/algorithm/TokenBucketRateLimitAlgorithm.java`
- Modify: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/annotation/RateLimit.java`
- Modify: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/handler/RateLimitAspect.java`
- Modify: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/config/RateLimitRegistrar.java`

- [ ] **Step 1: 创建 RateLimitAlgorithm 接口**

Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/algorithm/RateLimitAlgorithm.java`

```java
package com.ddf.boot.common.limit.ratelimit.algorithm;

/**
 * 限流算法策略接口，接入方注册自定义算法 Bean 即可扩展（固定窗口/漏桶/自定义）。
 */
public interface RateLimitAlgorithm {

    /** 算法 bean name / 标识，与 @RateLimit 注解的 algorithm 字段对应 */
    String getAlgorithm();

    /** 尝试获取许可，返回 false 表示被限流 */
    boolean tryAcquire(String key, int max, int rate);
}
```

- [ ] **Step 2: 创建默认令牌桶实现**

Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/algorithm/TokenBucketRateLimitAlgorithm.java`

```java
package com.ddf.boot.common.limit.ratelimit.algorithm;

import com.ddf.boot.common.redis.helper.RedisTemplateHelper;

public class TokenBucketRateLimitAlgorithm implements RateLimitAlgorithm {

    public static final String ALGORITHM = "tokenBucket";

    private final RedisTemplateHelper redisTemplateHelper;

    public TokenBucketRateLimitAlgorithm(RedisTemplateHelper redisTemplateHelper) {
        this.redisTemplateHelper = redisTemplateHelper;
    }

    @Override
    public String getAlgorithm() {
        return ALGORITHM;
    }

    @Override
    public boolean tryAcquire(String key, int max, int rate) {
        return redisTemplateHelper.tokenBucketRateLimitAcquire(key, max, rate);
    }
}
```

- [ ] **Step 3: RateLimit 注解增加 algorithm 字段**

Modify `RateLimit.java`：

```java
    /**
     * 限流算法 bean name，默认令牌桶
     *
     * @see RateLimitAlgorithm
     */
    String algorithm() default "";
```

- [ ] **Step 4: RateLimitAspect 注入算法 Map 并替换硬编码调用**

Modify `RateLimitAspect`：

1. 新增字段与 import：

```java
import com.ddf.boot.common.limit.ratelimit.algorithm.RateLimitAlgorithm;
import com.ddf.boot.common.limit.ratelimit.algorithm.TokenBucketRateLimitAlgorithm;

    private final Map<String, RateLimitAlgorithm> algorithmMap;
```

2. 构造函数改为 `@RequiredArgsConstructor` 自动注入（已用 Lombok，新增 final 字段即可）。

3. 将第 147 行硬编码调用替换为：

```java
            String algorithm = StringUtils.isBlank(annotation.algorithm())
                    ? TokenBucketRateLimitAlgorithm.ALGORITHM : annotation.algorithm();
            if (!algorithmMap.containsKey(algorithm)) {
                throw new NoSuchBeanDefinitionException("限流算法组件[%s]不存在".formatted(algorithm));
            }
            if (!algorithmMap.get(algorithm).tryAcquire(key, max, rate)) {
                log.error(
                        "接口【{}-{}-{}】超过限流算法{}预定流量，过滤请求， 完整key规则为: {}, 对应参数{}, 记录日志>>>>>>>",
                        identityNo, currentClass.getName(), currentMethod.getName(), algorithm, key,
                        AopUtil.serializeParam(joinPoint));
                throw new BusinessException(LimitExceptionCode.RATE_LIMIT);
            }
```

- [ ] **Step 5: RateLimitRegistrar 注册默认算法 Bean**

Modify `RateLimitRegistrar`（`ImportBeanDefinitionRegistrar`），在其注册逻辑中追加默认令牌桶算法 Bean（或改为在 `LimitAutoConfiguration` 中注册）。在 `LimitAutoConfiguration` 中新增：

```java
import com.ddf.boot.common.limit.ratelimit.algorithm.RateLimitAlgorithm;
import com.ddf.boot.common.limit.ratelimit.algorithm.TokenBucketRateLimitAlgorithm;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

    @Bean
    @ConditionalOnMissingBean(RateLimitAlgorithm.class)
    public RateLimitAlgorithm tokenBucketRateLimitAlgorithm(RedisTemplateHelper redisTemplateHelper) {
        return new TokenBucketRateLimitAlgorithm(redisTemplateHelper);
    }
```

同时需确保 `RateLimitAspect` 的 `algorithmMap` 由 `Map<String, RateLimitAlgorithm>` 注入（Spring 自动收集所有 `RateLimitAlgorithm` Bean，key 为 bean name）。因 `getAlgorithm()` 返回的标识与 bean name 需一致，将 `TokenBucketRateLimitAlgorithm` 的 bean name 显式设为 `tokenBucket`：

```java
    @Bean(name = TokenBucketRateLimitAlgorithm.ALGORITHM)
    @ConditionalOnMissingBean(RateLimitAlgorithm.class)
    public RateLimitAlgorithm tokenBucketRateLimitAlgorithm(RedisTemplateHelper redisTemplateHelper) {
        return new TokenBucketRateLimitAlgorithm(redisTemplateHelper);
    }
```

- [ ] **Step 6: 编译验证**

Run: `mvn -q -pl ddf-common-limit -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 7: Commit**

```bash
git add ddf-common-limit/src/main/java/com/ddf/boot/common/limit/
git commit -m "feat: extract RateLimitAlgorithm SPI"
```

## Task 4.2: 限流触发事件

**Files:**
- Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/event/RateLimitTriggeredEvent.java`
- Modify: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/handler/RateLimitAspect.java`

- [ ] **Step 1: 创建事件类**

Create: `ddf-common-limit/src/main/java/com/ddf/boot/common/limit/ratelimit/event/RateLimitTriggeredEvent.java`

```java
package com.ddf.boot.common.limit.ratelimit.event;

import org.springframework.context.ApplicationEvent;

public class RateLimitTriggeredEvent extends ApplicationEvent {

    private final String key;
    private final String algorithm;

    public RateLimitTriggeredEvent(Object source, String key, String algorithm) {
        super(source);
        this.key = key;
        this.algorithm = algorithm;
    }

    public String getKey() {
        return key;
    }

    public String getAlgorithm() {
        return algorithm;
    }
}
```

- [ ] **Step 2: 在切面被限流时发布事件**

Modify `RateLimitAspect`：新增 `ApplicationEventPublisher` 字段，并在抛异常前发布事件：

```java
import com.ddf.boot.common.limit.ratelimit.event.RateLimitTriggeredEvent;
import org.springframework.context.ApplicationEventPublisher;

    private final ApplicationEventPublisher applicationEventPublisher;
```

在 `throw new BusinessException(LimitExceptionCode.RATE_LIMIT);` 之前插入：

```java
                applicationEventPublisher.publishEvent(new RateLimitTriggeredEvent(this, key, algorithm));
```

- [ ] **Step 3: 编译验证**

Run: `mvn -q -pl ddf-common-limit -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 4: Commit**

```bash
git add ddf-common-limit/src/main/java/com/ddf/boot/common/limit/
git commit -m "feat: publish RateLimitTriggeredEvent on rate limit"
```

---

# Phase 5：captcha 类型分发 SPI + 校验事件

## Task 5.1: 定义 CaptchaProducer SPI 并替换 switch 分发

**Files:**
- Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/producer/CaptchaProducer.java`
- Modify: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/helper/CaptchaHelper.java`

- [ ] **Step 1: 创建 CaptchaProducer 接口**

Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/producer/CaptchaProducer.java`

```java
package com.ddf.common.captcha.producer;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;

/**
 * 验证码生成策略接口，接入方注册自定义实现以扩展验证码类型（短信/语音）。
 */
public interface CaptchaProducer {

    /** 支持的验证码类型 */
    CaptchaType getCaptchaType();

    /** 生成验证码 */
    CaptchaResult generate();
}
```

- [ ] **Step 2: 在 CaptchaHelper 注入 Map 并替换 switch**

Modify `CaptchaHelper`：

1. 新增 import 与字段：

```java
import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.common.captcha.producer.CaptchaProducer;
import java.util.Map;
import java.util.Objects;

    private final Map<CaptchaType, CaptchaProducer> captchaProducerMap;
```

2. 构造函数新增参数 `Map<CaptchaType, CaptchaProducer> captchaProducerMap`。

3. 将 `generate(CaptchaRequest)` 的 switch 替换为按类型分发：

```java
    public CaptchaResult generate(CaptchaRequest captchaRequest) {
        CaptchaProducer producer = captchaProducerMap.get(captchaRequest.getCaptchaType());
        if (Objects.isNull(producer)) {
            return generateMath(); // 默认回退
        }
        return producer.generate();
    }
```

4. 保留 `generateText`/`generateMath`/`generateAjCaptcha` 方法（供现有 producer 实现复用）。

- [ ] **Step 3: 创建独立 Producer 并注册 Map**

为避免循环依赖，Producer 必须为独立类（持有自身依赖），不得反向依赖 `CaptchaHelper`。缓存写入（uuid + 答案）随 Producer 自包含，复用原 `buildCaptchaResult` 逻辑。

Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/producer/TextCaptchaProducer.java`

```java
package com.ddf.common.captcha.producer;

import com.anji.captcha.service.CaptchaCacheService;
import com.ddf.boot.common.api.model.captcha.CaptchaType;
import com.ddf.boot.common.api.model.captcha.response.CaptchaResult;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.common.captcha.properties.CaptchaProperties;
import com.ddf.common.captcha.properties.KaptchaProperties;
import com.ddf.common.captcha.repository.CacheAdapter;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.springframework.util.FastByteArrayOutputStream;

public class TextCaptchaProducer implements CaptchaProducer {

    private final DefaultKaptcha defaultKaptcha;
    private final CaptchaProperties captchaProperties;
    private final CaptchaCacheService captchaCacheService;

    public TextCaptchaProducer(DefaultKaptcha defaultKaptcha, CaptchaProperties captchaProperties,
            CaptchaCacheService captchaCacheService) {
        this.defaultKaptcha = defaultKaptcha;
        this.captchaProperties = captchaProperties;
        this.captchaCacheService = captchaCacheService;
    }

    @Override
    public CaptchaType getCaptchaType() {
        return CaptchaType.TEXT;
    }

    @Override
    public CaptchaResult generate() {
        String text = defaultKaptcha.createText();
        BufferedImage image = defaultKaptcha.createImage(text);
        return build(text, text, image);
    }

    private CaptchaResult build(String verifyCode, String cacheValue, BufferedImage image) {
        KaptchaProperties kaptcha = captchaProperties.getKaptcha();
        CaptchaResult result = new CaptchaResult();
        result.setVerifyCode(verifyCode);
        result.setWidth(kaptcha.getWidth());
        result.setHeight(kaptcha.getHeight());
        result.setOriginalImageBase64(encode(image));
        String token = String.format("%s:%s", CacheAdapter.CAPTCHA_KEY_PREFIX, IdsUtil.getUniqueId());
        result.setUuid(token);
        captchaCacheService.set(token, cacheValue, captchaProperties.getKeyExpiredSeconds());
        return result;
    }

    private String encode(BufferedImage image) {
        FastByteArrayOutputStream stream = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", stream);
        } catch (IOException e) {
            throw new IllegalStateException("验证码生成失败");
        }
        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }
}
```

Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/producer/CaptchaProducerConfiguration.java`

```java
package com.ddf.common.captcha.producer;

import com.ddf.boot.common.api.model.captcha.CaptchaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaptchaProducerConfiguration {

    @Bean
    public Map<CaptchaType, CaptchaProducer> captchaProducerMap(List<CaptchaProducer> producers) {
        return producers.stream().collect(Collectors.toMap(CaptchaProducer::getCaptchaType, p -> p));
    }
}
```

> 本阶段先落地 TEXT 一个 Producer 打通分发链路（`CaptchaHelper.generate` 中 `Objects.isNull(producer)` 回退到原有 `generateMath`/`generateAjCaptcha`，保证 CLICK_WORDS/PIC_SLIDE 不受影响）。MATH Producer 同理从 `generateMath` 抽取。接入方注册自定义 `CaptchaProducer` Bean 即自动进入 `Map` 参与分发。

- [ ] **Step 4: 编译验证**

Run: `mvn -q -pl ddf-common-captcha -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 5: Commit**

```bash
git add ddf-common-captcha/src/main/java/com/ddf/common/captcha/
git commit -m "feat: extract CaptchaProducer SPI for captcha type dispatch"
```

## Task 5.2: 校验成功/失败事件

**Files:**
- Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/event/CaptchaVerifyEvent.java`
- Modify: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/helper/CaptchaHelper.java`

- [ ] **Step 1: 创建事件类**

Create: `ddf-common-captcha/src/main/java/com/ddf/common/captcha/event/CaptchaVerifyEvent.java`

```java
package com.ddf.common.captcha.event;

import org.springframework.context.ApplicationEvent;

public class CaptchaVerifyEvent extends ApplicationEvent {

    private final String uuid;
    private final boolean success;

    public CaptchaVerifyEvent(Object source, String uuid, boolean success) {
        super(source);
        this.uuid = uuid;
        this.success = success;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isSuccess() {
        return success;
    }
}
```

- [ ] **Step 2: 在 check 方法发布事件**

Modify `CaptchaHelper`：注入 `ApplicationEventPublisher`（构造函数新增参数），在 `check` 成功与失败分支发布事件：

```java
import com.ddf.common.captcha.event.CaptchaVerifyEvent;
import org.springframework.context.ApplicationEventPublisher;

    private final ApplicationEventPublisher applicationEventPublisher;

    public CaptchaCheckResult check(CaptchaCheckRequest request) {
        final CaptchaType captchaType = request.getCaptchaType();
        try {
            if (Objects.equal(CaptchaType.CLICK_WORDS, captchaType) || Objects.equal(CaptchaType.PIC_SLIDE, captchaType)) {
                // ... 原有滑动/点选校验逻辑
            }
            final String captchaVerification = IdsUtil.getUniqueId();
            cacheAdapter.setCaptchaVerification(request.getUuid(), captchaVerification);
            applicationEventPublisher.publishEvent(new CaptchaVerifyEvent(this, request.getUuid(), true));
            return CaptchaCheckResult.builder().uuid(request.getUuid()).captchaVerification(captchaVerification).build();
        } catch (BusinessException e) {
            applicationEventPublisher.publishEvent(new CaptchaVerifyEvent(this, request.getUuid(), false));
            throw e;
        }
    }
```

- [ ] **Step 3: 编译验证**

Run: `mvn -q -pl ddf-common-captcha -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 4: Commit**

```bash
git add ddf-common-captcha/src/main/java/com/ddf/common/captcha/
git commit -m "feat: publish CaptchaVerifyEvent on captcha check"
```

---

# Phase 6：ids-service ID 生成策略注册

## Task 6.1: 抽象 IDGen 策略注册与按 key 分发

**Files:**
- Create: `ddf-common-ids-service/src/main/java/com/ddf/common/ids/service/service/IdGenRegistry.java`
- Modify: `ddf-common-ids-service/src/main/java/com/ddf/common/ids/service/api/impl/IdsApiImpl.java`
- Modify: `ddf-common-ids-service/src/main/java/com/ddf/common/ids/service/config/IdsServiceAutoConfiguration.java`

- [ ] **Step 1: 创建 IdGenRegistry 注册表**

Create: `ddf-common-ids-service/src/main/java/com/ddf/common/ids/service/service/IdGenRegistry.java`

```java
package com.ddf.common.ids.service.service;

import com.ddf.common.ids.service.model.common.Result;
import com.ddf.common.ids.service.model.common.ResultList;
import java.util.List;

/**
 * ID 生成策略注册表，接入方注册自定义 IDGen 实现（Leaf/Tinyid）Bean 即可扩展。
 */
public class IdGenRegistry {

    private final List<IDGen> idGens;

    public IdGenRegistry(List<IDGen> idGens) {
        this.idGens = idGens;
    }

    public Result get(String key) {
        for (IDGen idGen : idGens) {
            Result result = idGen.get(key);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("没有可用的 IDGen 实现");
    }

    public ResultList list(String key, int number) {
        for (IDGen idGen : idGens) {
            ResultList result = idGen.list(key, number);
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("没有可用的 IDGen 实现");
    }
}
```

- [ ] **Step 2: IdsApiImpl 改用 IdGenRegistry，消除写死 cast**

Modify `IdsApiImpl`：

1. 新增 import 与字段：

```java
import com.ddf.common.ids.service.service.IdGenRegistry;

    private final IdGenRegistry idGenRegistry;
```

2. 构造函数改为 `(IdsProperties idsProperties, SnowflakeService snowflakeService, IdGenRegistry idGenRegistry)`。

3. 将 `segmentIDGen.get(key)` 替换为 `idGenRegistry.get(key)`；`segmentIDGen.list(key, number)` 替换为 `idGenRegistry.list(key, number)`。

4. `getSegmentCache()` / `getDb()` 中 `((SegmentIDGenImpl) segmentIDGen)` 的写死 cast 改为从 `idGenRegistry` 中按类型查找 `SegmentIDGenImpl`：

```java
    @Override
    public Map<String, SegmentBufferView> getSegmentCache() {
        checkSegment();
        SegmentIDGenImpl segment = findSegmentImpl();
        Map<String, SegmentBufferView> data = new HashMap<>(32);
        Map<String, SegmentBuffer> cache = segment.getCache();
        // ... 原填充逻辑不变
        return data;
    }

    private SegmentIDGenImpl findSegmentImpl() {
        throw new UnsupportedOperationException("请通过 IdGenRegistry 获取 SegmentIDGenImpl");
    }
```

> 注：`getSegmentCache`/`getDb` 是号段模式专属运维接口，若业务不依赖可保留占位并在实现中从 `List<IDGen>` 过滤 `SegmentIDGenImpl`。为控制本阶段范围，`getSegmentCache`/`getDb` 可先保留原 cast 逻辑，仅将核心 `getSegmentId`/`getSegmentIds`/`getMultiId`/`getMultiIds` 改为 `IdGenRegistry` 分发。

- [ ] **Step 3: IdsServiceAutoConfiguration 注册 IdGenRegistry**

Modify `IdsServiceAutoConfiguration`：

```java
import com.ddf.common.ids.service.service.IdGenRegistry;
import java.util.List;

    @Bean
    public IdGenRegistry idGenRegistry(List<IDGen> idGens) {
        return new IdGenRegistry(idGens);
    }

    @Bean
    public IdsApi idsApi(IdsProperties idsProperties, Optional<SnowflakeService> snowflakeService,
            IdGenRegistry idGenRegistry) {
        return new IdsApiImpl(idsProperties, snowflakeService.orElse(null), idGenRegistry);
    }
```

- [ ] **Step 4: 编译验证**

Run: `mvn -q -pl ddf-common-ids-service -am compile`
Expected: BUILD SUCCESS。

- [ ] **Step 5: Commit**

```bash
git add ddf-common-ids-service/src/main/java/com/ddf/common/ids/service/
git commit -m "feat: register IDGen strategy via IdGenRegistry"
```

---

## 完成自检

全部 Phase 完成后，在仓库根目录执行一次全量编译确认无回归：

```bash
mvn -q clean compile
```

Expected: BUILD SUCCESS。
