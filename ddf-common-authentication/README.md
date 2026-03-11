# ddf-common-authentication

认证基础能力模块。

定位：

- 提供认证相关公共能力
- 作为 `ddf-common-starter-web` 的组成部分
- 与 MVC、限流等基础 Web 能力协同工作

## 当前提供能力

- 认证自动配置
- 用户上下文处理
- Token 校验扩展点
- 认证相关配置项

## 使用建议

不建议业务项目直接依赖 `ddf-common-authentication`。

推荐方式：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

如果是常规业务服务，推荐直接使用：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 扩展点

如果业务需要自定义认证逻辑，重点关注：

- `UserClaimService`
- `TokenCustomizeCheckService`

认证配置见：

- `AuthenticationProperties`
