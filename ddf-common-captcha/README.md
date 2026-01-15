# ddf-common-captcha

验证码模块，提供多种验证码生成和校验功能。

## 功能特性

- 图形字符验证码
- 数学表达式验证码
- 滑动滑块验证码
- 点选文字验证码

## 依赖引入

```xml
<dependency>
    <groupId>com.ddf.common</groupId>
    <artifactId>ddf-common-captcha</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 验证码类型

| 类型 | 说明 |
|------|------|
| TEXT | 图形字符验证码 |
| MATH | 数学表达式验证码 |
| PIC_SLIDE | 滑动滑块验证码 |
| CLICK_WORDS | 点选文字验证码 |

## 核心类

| 类路径                 | 功能     |
|---------------------|--------|
| `CaptchaHelper`     | 验证码工具类 |
| `CaptchaProperties` | 配置属性   |

## 使用说明

```java
@Autowired
private CaptchaHelper captchaHelper;

// 生成验证码
CaptchaResult result = captchaHelper.generate(request);

// 校验验证码
boolean valid = captchaHelper.check(checkRequest);
```
