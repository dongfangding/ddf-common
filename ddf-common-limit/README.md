# ddf-common-limit

[English](./README.md) | [中文](./README.zh-CN.md)

Rate limiting and repeat-submission protection module.

Positioning:

- Provides API rate limiting and repeat-submission protection
- Acts as part of `ddf-common-starter-web`
- Works together with authentication, MVC, Redis, and other foundational modules

## Current Capabilities

- `@RateLimit`
- `@MultiRateLimit`
- `@EnableRateLimit`
- `@EnableRepeatable`
- Repeat-submission protection
- Rate-limit key generation extensions

## Usage Recommendation

Direct business dependencies on `ddf-common-limit` are not recommended.

Recommended option:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

If the business service also needs database and governance support, prefer:

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## Notes

`ddf-common-limit` currently still collaborates with:

- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-redis`

This makes it more suitable as an internal starter component than as a standalone business dependency to be manually assembled.

Additional notes:

- `LimitAutoConfiguration` itself is currently lightweight
- Actual rate limiting and repeat-submission protection still depend mainly on the import chain triggered by `@EnableRateLimit` and `@EnableRepeatable`
- Simply importing the dependency is therefore not equivalent to all limiting capabilities becoming active automatically
