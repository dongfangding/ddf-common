# ddf-common

`ddf-common` 是一个面向 Spring Boot 3 的多模块通用基础组件库，用于沉淀后端项目里的通用能力与基础设施接入。

当前基础环境：

- Java 17
- Spring Boot 3.5.9
- Maven 3.9.6+

## 模块分层

核心基础模块：

- `ddf-common-api`
- `ddf-common-core`
- `ddf-common-mvc`
- `ddf-common-authentication`
- `ddf-common-limit`

基础设施模块：

- `ddf-common-data-mysql-starter`
- `ddf-common-governance-starter`

场景聚合 starter：

- `ddf-common-starter-web`
- `ddf-common-starter-default`

## 推荐接入方式

### 1. 轻量 Web 服务

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

适合：

- 只需要 Web 基础能力
- 暂时不接数据库
- 暂时不接治理能力

### 2. 常规业务服务

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-default</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

适合：

- 常规业务后端服务
- 需要 Web、MySQL、Druid、治理能力

### 3. 自定义组合

如果你不希望直接使用 `starter-default`，也可以按场景组合：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-starter-web</artifactId>
    <version>${ddf-common.version}</version>
</dependency>

<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-data-mysql-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>

<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-governance-starter</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

## 新 starter 说明

### `ddf-common-starter-web`

聚合：

- `api`
- `core`
- `mvc`
- `authentication`
- `limit`

### `ddf-common-data-mysql-starter`

聚合：

- `spring-boot-starter-jdbc`
- `mysql-connector-j`
- `druid-spring-boot-3-starter`

统一配置前缀：

```yaml
customizer:
  data:
    mysql:
      enabled: true
      druid:
        usePingMethod: false
```

### `ddf-common-governance-starter`

聚合：

- `spring-boot-starter-mail`
- `spring-boot-starter-actuator`

统一配置前缀：

```yaml
customizer:
  governance:
    mail:
      enabled: true
    observability:
      enabled: true
```

说明：

- 引入治理层 starter 后，未配置 `spring.mail.*` 不会报错
- Mail 能力只有在底层 Bean 存在时才会自动接回

## 构建命令

使用项目指定的 Maven settings：

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests
```

构建指定模块：

```bash
mvn -s E:\apache-maven-3.9.12\conf\settings-snowball.xml clean install -DskipTests -pl ddf-common-starter-default -am
```

## 说明

- 这是一个基础库项目，不是可直接运行的业务应用
- 示例业务项目可参考 `spring-boot-quick`

## License

Apache License 2.0
