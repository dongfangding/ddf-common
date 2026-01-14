# CLAUDE.md

## 模块简介

提供验证码生成和校验功能，支持图形验证码、数学验证码和滑动验证码。

## 核心类

| 类路径                                                                 | 功能     |
|---------------------------------------------------------------------|--------|
| `com.ddf.common.captcha.helper.CaptchaHelper`                       | 验证码工具类 |
| `com.ddf.common.captcha.properties.CaptchaProperties`               | 配置属性   |
| `com.ddf.boot.common.api.model.captcha.request.CaptchaRequest`      | 验证码请求  |
| `com.ddf.boot.common.api.model.captcha.request.CaptchaCheckRequest` | 校验请求   |

## 使用说明

### 1. 配置

```yaml
ddf:
  captcha:
    cache-type: REDIS                    # 缓存类型：REDIS / LOCAL
    key-expired-seconds: 300             # 验证码过期时间（秒）
    kaptcha:
      width: 100                         # 宽度
      height: 40                         # 高度
    aj-captcha:
      type: BLOCKPUZZLE                  # 滑动类型：BLOCKPUZZLE / CLICKWORD
```

### 2. 生成验证码

```java
@Autowired
private CaptchaHelper captchaHelper;

// 生成图形验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder()
        .captchaType(CaptchaType.TEXT)  // 图形字符
        .build()
);
String uuid = result.getUuid();          // 验证码标识
String verifyCode = result.getVerifyCode();  // 验证码答案
String imageBase64 = result.getOriginalImageBase64();  // 图片 Base64

// 生成数学验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder()
        .captchaType(CaptchaType.MATH)
        .build()
);

// 生成滑动验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder()
        .captchaType(CaptchaType.PIC_SLIDE)
        .build()
);

// 生成点选验证码
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder()
        .captchaType(CaptchaType.CLICK_WORDS)
        .build()
);
```

### 3. 校验验证码

```java
// 校验普通验证码
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)                      // 验证码标识
        .verifyCode(verifyCode)          // 用户输入的验证码
        .captchaType(CaptchaType.TEXT)
        .build()
);

// 校验滑动/点选验证码
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)
        .verifyCode(pointJson)           // 点坐标或滑块位置 JSON
        .captchaVerification(captchaVerification)  // 二次验证 token
        .captchaType(CaptchaType.PIC_SLIDE)
        .verification(true)              // 是否进行服务端二次校验
        .build()
);
```

### 4. 返回格式

```json
{
  "uuid": "xxx",
  "verifyCode": "A1B2",           // 验证码答案
  "originalImageBase64": "...",   // 原始图片 Base64
  "imageBase64": "...",           // 滑块图片 Base64（滑动验证码）
  "wordList": ["word1", "word2"], // 点选文字列表
  "width": 100,
  "height": 40
}
```

## 验证码类型

| 类型            | 说明       |
|---------------|----------|
| `TEXT`        | 图形字符验证码  |
| `MATH`        | 数学表达式验证码 |
| `PIC_SLIDE`   | 滑动滑块验证码  |
| `CLICK_WORDS` | 点选文字验证码  |

## 注意事项

1. **缓存类型**：生产环境建议使用 Redis 缓存，单机环境可用 LOCAL
2. **安全性**：验证码校验成功后会自动删除，防止重放攻击
3. **有效期**：默认 5 分钟过期，可通过配置调整
4. **依赖库**：滑动/点选验证码依赖 `anji-plus-captcha` 库
