# ddf-common-captcha 接入指南

> 验证码生成与校验：统一 `CaptchaHelper` 入口，按 `CaptchaType` 分发到 `CaptchaProducer` 策略，内置图形 / 数学 / 滑动 / 点选，校验时发布事件。

## 核心能力

| 能力 | 说明 | 关键类 / 入口 |
|------|------|--------------|
| 统一入口 | 生成 / 校验 / 二次校验 | `helper.CaptchaHelper` |
| 生成策略 SPI | 按 `CaptchaType` 分发到 `Map<CaptchaType, CaptchaProducer>` | `producer.CaptchaProducer` |
| 图形字符验证码 | `TEXT` 类型默认实现 | `producer.TextCaptchaProducer` |
| 数学 / 滑动 / 点选 | 数学表达式、滑块、点选文字 | `CaptchaHelper.generateMath()` / `generateAjCaptcha()` |
| 校验事件 | 校验成功 / 失败时发布 | `event.CaptchaVerifyEvent` |
| 配置属性 | 缓存类型、有效期、Kaptcha 样式 | `properties.CaptchaProperties` |

## 接入方式

### 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-captcha</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 传递依赖 `ddf-common-core`、`ddf-common-redis`；滑动 / 点选验证码依赖 `com.anji-plus:captcha` 与 `com.github.penggle:kaptcha`。

验证码组件由 `CaptchaAutoConfiguration` 通过 `META-INF/spring/...AutoConfiguration.imports` 自动装配，引入依赖后**无需额外注解**。

### 关键配置

配置前缀 `customizer.infra.captcha`（对应 `CaptchaProperties`）：

```yaml
customizer:
  infra:
    captcha:
      cache-type: redis              # 缓存类型：redis / local
      key-expired-seconds: 300       # 验证码有效期（秒）
      kaptcha:                       # Kaptcha 图形样式
        width: 200
        height: 50
        content:
          length: 4                  # 字符长度
          source: "abcdefghjklmnopqrstuvwxyz23456789"
      aj:                            # 滑动 / 点选（anji-captcha）样式
        ...
```

### 生成验证码

```java
@Autowired
private CaptchaHelper captchaHelper;

// 图形字符验证码
CaptchaResult text = captchaHelper.generate(
        CaptchaRequest.builder().captchaType(CaptchaType.TEXT).build());

// 数学表达式验证码（默认类型）
CaptchaResult math = captchaHelper.generate(
        CaptchaRequest.builder().captchaType(CaptchaType.MATH).build());

// 滑动 / 点选验证码
CaptchaResult slide = captchaHelper.generate(
        CaptchaRequest.builder().captchaType(CaptchaType.PIC_SLIDE).build());
CaptchaResult click = captchaHelper.generate(
        CaptchaRequest.builder().captchaType(CaptchaType.CLICK_WORDS).build());

String uuid = text.getUuid();                        // 回传表单的唯一标识
String image = text.getOriginalImageBase64();        // 图片 Base64（含 prefix 前缀）
```

### 校验验证码

```java
// 普通 / 数学验证码校验，返回 CaptchaCheckResult（携带二次校验凭证）
CaptchaCheckResult result = captchaHelper.check(
        CaptchaCheckRequest.builder()
                .uuid(uuid)
                .verifyCode(userInput)     // 字符验证码传字符，数学验证码传计算结果
                .captchaType(CaptchaType.TEXT)
                .build());
String captchaVerification = result.getCaptchaVerification();

// 服务端二次校验（登录等关键业务接口）
captchaHelper.serverSecondCheck(
        CaptchaSecondCheckRequest.builder()
                .uuid(uuid)
                .captchaVerification(captchaVerification)
                .build());
```

## 扩展点

### 1. CaptchaProducer 类型分发 SPI

实现 `CaptchaProducer` 并注册 Bean，`CaptchaProducerConfiguration` 会自动聚合为 `Map<CaptchaType, CaptchaProducer>`，`CaptchaHelper.generate` 优先按类型命中该 Map（如短信验证码）：

```java
@Component
public class SmsCaptchaProducer implements CaptchaProducer {

    @Override
    public CaptchaType getCaptchaType() {
        // 返回本生产者支持的验证码类型
        return CaptchaType.TEXT;
    }

    @Override
    public CaptchaResult generate() {
        // 自包含：生成验证码、写入缓存、返回 CaptchaResult
        return new CaptchaResult();
    }
}
```

> 未命中 `Map` 的类型会回退到 `CaptchaHelper` 内置的 `switch` 逻辑（MATH / CLICK_WORDS / PIC_SLIDE）。

### 2. CaptchaVerifyEvent 校验事件

校验成功 / 失败（`BusinessException`）时发布 `CaptchaVerifyEvent`，可用于记录日志、风控：

```java
@EventListener
public void onCaptchaVerify(CaptchaVerifyEvent event) {
    log.info("验证码校验, uuid={}, success={}", event.getUuid(), event.isSuccess());
}
```

## 注意事项

1. **缓存类型**：生产建议 `redis`，单机可用 `local`。
2. **答案不返回前端**：`CaptchaResult.verifyCode` 标注 `@JsonIgnore`，仅写入缓存用于服务端比对，不会序列化给客户端。
3. **二次校验**：`check()` 返回 `CaptchaCheckResult`（非 boolean），其中 `captchaVerification` 是二次校验凭证；滑动 / 点选校验失败会抛 `BusinessException(CaptchaErrorCode.VERIFY_CODE_NOT_MAPPING)`。
4. **有效性与防重放**：验证码默认 5 分钟过期；校验成功后需按业务自行处理缓存的清理，避免重放。
5. **滑动 / 点选依赖**：依赖 `anji-plus-captcha` 三方库，`generateAjCaptcha` 固定宽高 310×155。
