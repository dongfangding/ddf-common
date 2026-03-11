# ddf-common-mvc

MVC 基础能力模块。

定位：

- 为 Web 场景提供 MVC 相关公共能力
- 作为 `ddf-common-starter-web` 的组成部分
- 不再承载数据库连接池等基础设施实现

## 当前提供能力

- 全局异常处理
- 跨域配置
- 参数解析扩展
- 请求体缓存过滤
- Web 工具类
- AOP 辅助能力

## 不再承载的能力

- Druid
- MySQL 相关数据源行为

这些能力已迁移到：

- `ddf-common-data-mysql-starter`

## 使用建议

不建议业务项目直接依赖 `ddf-common-mvc`。

推荐方式：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

如果需要数据库能力，再叠加：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```
