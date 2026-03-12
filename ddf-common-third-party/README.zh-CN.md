# ddf-common-third-party

[English](./README.md) | [中文](./README.zh-CN.md)

第三方服务集成模块。

## 当前定位

- 提供第三方服务接入的统一封装入口
- 当前主要包含阿里云 OSS 与短信相关能力
- 为上层业务模块提供可复用的集成支撑

## 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-third-party</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 自动配置

- `com.ddf.boot.common.ext.ExtAutoConfiguration`
- `com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration`

## 主要类型

- `OssProperties`
- `OssHelper`
- `AliYunSmsProperties`

## 说明

- 当前公开能力以 OSS、SMS 为主
- 如果后续扩展更多第三方能力，建议继续按服务域拆分子包与文档
