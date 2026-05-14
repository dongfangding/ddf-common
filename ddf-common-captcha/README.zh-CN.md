# ddf-common-captcha

> 验证码模块。支持图形字符、数学表达式、滑动滑块、点选文字四种验证码类型，
> 提供统一的生成与校验接口，缓存支持 Redis 和本地内存两种模式。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-captcha` 解决的是 **"人机验证与安全防刷"** 问题。

| 场景      | 典型问题           | 模块提供的能力           |
|---------|----------------|-------------------|
| 登录注册防机器 | 接口被脚本暴力破解      | 图形验证码前置校验         |
| 防短信轰炸   | 发送接口被高频调用      | 数学/滑动验证码拦截        |
| 高安全场景   | 普通验证码易被 OCR 识别 | 滑动滑块 + 点选文字，交互式验证 |
| 分布式部署   | 多实例间验证码状态不同步   | Redis 缓存共享校验状态    |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-captcha</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. 最小化配置

```yaml
ddf:
  captcha:
    cache-type: REDIS              # 缓存类型：REDIS / LOCAL
    key-expired-seconds: 300       # 验证码有效期（秒）
    kaptcha:
      width: 100                   # 图形验证码宽度
      height: 40                   # 图形验证码高度
    aj-captcha:
      type: BLOCKPUZZLE            # 滑动类型：BLOCKPUZZLE / CLICKWORD
```

> `cache-type=REDIS` 时需确保 `ddf-common-redis` 已引入且 Redis 可连接；`LOCAL` 模式仅适用于单机环境。

---

## 4. 核心 API

### 4.1 生成验证码

```java
@Autowired
private CaptchaHelper captchaHelper;

// 图形字符验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder()
        .captchaType(CaptchaType.TEXT)
        .build()
);
String uuid = result.getUuid();
String base64Image = result.getOriginalImageBase64();

// 数学表达式验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.MATH).build()
);

// 滑动滑块验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.PIC_SLIDE).build()
);

// 点选文字验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.CLICK_WORDS).build()
);
```

### 4.2 校验验证码

```java
// 普通验证码（TEXT / MATH）
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)
        .verifyCode(userInput)          // 用户输入的验证码内容
        .captchaType(CaptchaType.TEXT)
        .build()
);

// 滑动/点选验证码（需二次校验）
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)
        .verifyCode(pointJson)          // 点坐标或滑块位置 JSON
        .captchaType(CaptchaType.PIC_SLIDE)
        .captchaVerification(token)     // 二次验证 token
        .verification(true)             // 是否服务端二次校验
        .build()
);
```

### 4.3 验证码类型对比

| 类型            | 交互方式   | 防 OCR | 适用场景     |
|---------------|--------|-------|----------|
| `TEXT`        | 输入字符   | 一般    | 普通登录、注册  |
| `MATH`        | 输入计算结果 | 较好    | 防简单 OCR  |
| `PIC_SLIDE`   | 拖动滑块   | 强     | 高安全登录、支付 |
| `CLICK_WORDS` | 点选文字   | 强     | 高安全场景    |

---

## 5. 进阶用法 / 扩展点

### 5.1 前后端分离集成

生成接口返回 JSON，前端根据 `captchaType` 渲染不同交互组件：

```json
{
  "uuid": "xxx",
  "originalImageBase64": "...",
  "imageBase64": "...",
  "wordList": ["word1", "word2"],
  "width": 100,
  "height": 40
}
```

### 5.2 验证码与登录接口配合

```java
@PostMapping("/login")
public ResponseData<String> login(@RequestBody LoginRequest request) {
    // 先校验验证码
    boolean valid = captchaHelper.check(
        CaptchaCheckRequest.builder()
            .uuid(request.getCaptchaUuid())
            .verifyCode(request.getCaptchaCode())
            .captchaType(CaptchaType.TEXT)
            .build()
    );
    if (!valid) {
        throw new BusinessException("验证码错误");
    }
    // 再执行登录逻辑
    return ResponseData.success(tokenService.create(request));
}
```

### 5.3 自定义验证码参数

通过 `CaptchaProperties` 调整图形验证码的干扰线、字符集、字体等：

```yaml
ddf:
  captcha:
    kaptcha:
      char-length: 4
      char-space: "abcdefghjkmnpqrstuvwxyz23456789"
      noise-color: "black"
```

---

## 6. 与其他模块协作

| 模块                 | 协作方式                                 |
|--------------------|--------------------------------------|
| `ddf-common-redis` | `cache-type=REDIS` 时依赖 Redis 存储验证码状态 |
| `ddf-common-limit` | 可与 `@RateLimit` 配合，在验证码校验通过后再放开接口限流  |

---

## 7. FAQ

**Q1：滑动验证码的 `captchaVerification` 是什么？**
由前端 SDK 在滑块拖动完成后生成的二次校验 token，用于服务端验证拖动轨迹的合法性，防止直接伪造坐标。

**Q2：验证码校验成功后还能再次使用吗？**
不能。校验成功后会立即删除缓存中的验证码记录，防止重放攻击。

**Q3：LOCAL 缓存模式在集群环境下会怎样？**
各实例缓存独立，导致 A 实例生成的验证码无法在 B 实例校验。生产环境务必使用 REDIS 模式。

**Q4：数学验证码的表达式是随机的吗？**
是。由模块随机生成简单的加减乘除表达式，结果缓存于后端，用户只需输入计算结果。

---

## 8. 参考

- 源码：`CaptchaHelper`、`CaptchaProperties`、`CaptchaAutoConfiguration`
- 滑动验证码依赖：`anji-plus-captcha`
