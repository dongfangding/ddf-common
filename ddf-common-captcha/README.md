# ddf-common-captcha

> Captcha verification module. Supports four types: text characters, math expressions, slider puzzles, and click-on-words.
> Provides unified generation and verification APIs with both Redis and local-memory caching modes.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-captcha` solves the **"human verification and anti-bot protection"** problem.

| Scenario | Typical Problem | What the Module Provides |
| --- | --- | --- |
| Login / registration anti-bot | Interfaces brute-forced by scripts | Graphical captcha pre-check |
| SMS bombing protection | Send API called at high frequency | Math / slider captcha interception |
| High-security scenarios | Regular captchas easily OCR'd | Slider + click-on-words interactive verification |
| Distributed deployment | Captcha state out of sync across instances | Redis cache shares verification state |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-captcha</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

---

## 3. Minimum Configuration

```yaml
ddf:
  captcha:
    cache-type: REDIS              # Cache type: REDIS / LOCAL
    key-expired-seconds: 300       # Captcha expiration in seconds
    kaptcha:
      width: 100                   # Image width
      height: 40                   # Image height
    aj-captcha:
      type: BLOCKPUZZLE            # Slider type: BLOCKPUZZLE / CLICKWORD
```

> `cache-type=REDIS` requires `ddf-common-redis` to be imported and Redis to be reachable.
> `LOCAL` mode is for single-node environments only.

---

## 4. Core API

### 4.1 Generate captcha

```java
@Autowired
private CaptchaHelper captchaHelper;

// Text character captcha
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.TEXT).build()
);
String uuid = result.getUuid();
String base64Image = result.getOriginalImageBase64();

// Math expression captcha
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.MATH).build()
);

// Slider puzzle captcha
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.PIC_SLIDE).build()
);

// Click-on-words captcha
CaptchaResult result = captchaHelper.generate(
    CaptchaRequest.builder().captchaType(CaptchaType.CLICK_WORDS).build()
);
```

### 4.2 Verify captcha

```java
// Regular captcha (TEXT / MATH)
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)
        .verifyCode(userInput)
        .captchaType(CaptchaType.TEXT)
        .build()
);

// Slider / click-on-words (requires secondary verification)
boolean valid = captchaHelper.check(
    CaptchaCheckRequest.builder()
        .uuid(uuid)
        .verifyCode(pointJson)
        .captchaType(CaptchaType.PIC_SLIDE)
        .captchaVerification(token)
        .verification(true)
        .build()
);
```

### 4.3 Captcha type comparison

| Type | Interaction | Anti-OCR | Suitable For |
| --- | --- | --- | --- |
| `TEXT` | Type characters | Moderate | Ordinary login / registration |
| `MATH` | Type calculation result | Good | Anti-simple-OCR |
| `PIC_SLIDE` | Drag slider | Strong | High-security login / payment |
| `CLICK_WORDS` | Click words | Strong | High-security scenarios |

---

## 5. Advanced Usage / Extension Points

### 5.1 Frontend-backend separation integration

The generation endpoint returns JSON; the frontend renders different interactive components based on `captchaType`:

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

### 5.2 Captcha paired with login endpoint

```java
@PostMapping("/login")
public ResponseData<String> login(@RequestBody LoginRequest request) {
    // Verify captcha first
    boolean valid = captchaHelper.check(
        CaptchaCheckRequest.builder()
            .uuid(request.getCaptchaUuid())
            .verifyCode(request.getCaptchaCode())
            .captchaType(CaptchaType.TEXT)
            .build()
    );
    if (!valid) {
        throw new BusinessException("Invalid captcha");
    }
    // Then perform login logic
    return ResponseData.success(tokenService.create(request));
}
```

### 5.3 Customize captcha parameters

Adjust noise lines, character set, font, etc. via `CaptchaProperties`:

```yaml
ddf:
  captcha:
    kaptcha:
      char-length: 4
      char-space: "abcdefghjkmnpqrstuvwxyz23456789"
      noise-color: "black"
```

---

## 6. Interplay with Other Modules

| Module | How They Cooperate |
| --- | --- |
| `ddf-common-redis` | Required when `cache-type=REDIS` for shared captcha state |
| `ddf-common-limit` | Can work with `@RateLimit` to relax rate limits only after captcha verification passes |

---

## 7. FAQ

**Q1: What is `captchaVerification` in slider captcha?**
A secondary verification token generated by the frontend SDK after the slider drag completes. The server uses it to validate the drag trace and prevent direct coordinate forgery.

**Q2: Can a captcha be reused after successful verification?**
No. The cached captcha record is deleted immediately after successful verification to prevent replay attacks.

**Q3: What happens with LOCAL cache mode in a cluster?**
Each instance has independent cache, so a captcha generated on instance A cannot be verified on instance B. Always use REDIS mode in production.

**Q4: Are math captcha expressions random?**
Yes. The module randomly generates simple addition/subtraction/multiplication/division expressions. The result is cached on the backend; the user only needs to input the calculated result.

---

## 8. References

- Source: `CaptchaHelper`, `CaptchaProperties`, `CaptchaAutoConfiguration`
- Slider captcha dependency: `anji-plus-captcha`
