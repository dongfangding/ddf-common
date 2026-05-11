# ddf-common-dependency

> 依赖版本统一管理模块（BOM）。集中声明所有 ddf-common 模块及第三方依赖的版本号，
> 子模块和下游业务工程引用时无需再写版本号，避免依赖冲突。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-dependency` 解决的是 **"依赖版本碎片化与冲突"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 多模块版本对齐 | 20+ 子模块各自声明版本，升级时改漏 | 一处声明，全局生效 |
| 依赖冲突 | A 模块用 Redisson 3.20，B 模块用 3.50 | BOM 统一锁定版本 |
| 业务接入简化 | 业务工程需要记忆大量版本号 | 引入 BOM 后，版本号全省略 |
| 技术栈升级 | Spring Boot 3.3 → 3.5 需要改几十个 pom | 改 BOM 中一个属性即可 |

---

## 2. 依赖引入

`ddf-common-dependency` 是 **Maven BOM（Bill of Materials）**，通过 `dependencyManagement` 引入：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.dongfangding</groupId>
            <artifactId>ddf-common-dependency</artifactId>
            <version>${ddf-common.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

引入后，子模块或业务工程可以直接使用依赖而不写版本：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-core</artifactId>
        <!-- 版本由 BOM 统一管理，无需声明 -->
    </dependency>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

## 3. 核心设计

### 3.1 版本属性

BOM 中所有版本号集中在 `<properties>` 中管理：

| 属性 | 当前版本 | 说明 |
| ----- | ----- | ----- |
| `spring-boot.version` | `3.5.9` | Spring Boot 基础版本 |
| `revision` | `boot3.5-2026.1-SNAPSHOT` | ddf-common 全局版本 |
| `java.version` | `17` | JDK 版本 |
| `mysql.version` | `9.1.0` | MySQL 驱动 |
| `druid.version` | `1.2.27` | Druid 连接池 |
| `redisson.version` | `3.52.0` | Redisson |
| `mybatis.version` | `3.0.4` | MyBatis Spring Boot Starter |
| `jwt.version` | `0.12.6` | JJWT |
| `hutool.version` | `5.8.42` | Hutool |
| `curator.version` | `5.3.0` | Curator (Zookeeper) |
| `rocketmq.version` | `2.3.3` | RocketMQ Spring Boot Starter |
| `fastjson2.version` | `2.0.58` | Fastjson2 |
| `guava.version` | `33.4.8-jre` | Guava |
| `xxl-job-version` | `3.3.0` | XXL-Job |

### 3.2 依赖管理范围

BOM 管理两类依赖：

1. **ddf-common 内部模块**：所有 `ddf-common-*` 模块的版本统一使用 `${revision}`
2. **第三方依赖**：Spring Boot、数据库、缓存、消息队列、工具库等

> 添加新依赖前，先检查 `spring-boot-dependencies` 中是否已有该依赖。如有，直接使用 Spring Boot 管理的版本，避免重复声明。

---

## 4. 进阶用法 / 扩展点

### 4.1 在业务工程中覆盖版本

业务工程可以在自己的 `pom.xml` 中覆盖 BOM 中的版本：

```xml
<properties>
    <!-- 覆盖 Redisson 版本 -->
    <redisson.version>3.55.0</redisson.version>
</properties>
```

> 覆盖优先级：业务工程 `pom.xml` > BOM `properties` > `spring-boot-dependencies`

### 4.2 添加新依赖到 BOM

在 `ddf-common-dependency/pom.xml` 中执行两步：

1. 在 `properties` 节添加版本号：
```xml
<my-new-lib.version>1.0.0</my-new-lib.version>
```

2. 在 `dependencyManagement` 节添加依赖声明：
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-new-lib</artifactId>
    <version>${my-new-lib.version}</version>
</dependency>
```

### 4.3 发布 BOM

BOM 随 ddf-common 一起发布到 Maven Central：

```bash
mvn -Prelease clean deploy
```

发布后，业务工程只需修改 `${ddf-common.version}` 即可升级全部依赖。

---

## 5. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| 所有 `ddf-common-*` 模块 | 版本由 BOM 的 `${revision}` 统一管理 |
| `spring-boot-dependencies` | BOM 先 import Spring Boot BOM，再在其上补充自定义依赖 |
| 业务工程 | 通过 `scope=import` 引入 BOM，省略所有版本号 |

---

## 6. FAQ

**Q1：BOM 和父 POM 有什么区别？**  
- 父 POM（`ddf-common/pom.xml`）：管理模块聚合、构建插件、发布配置
- BOM（`ddf-common-dependency/pom.xml`）：只管理依赖版本，不管理构建生命周期
- 业务工程通常只引入 BOM，不继承父 POM

**Q2：为什么有些依赖在 BOM 中没有版本号？**  
`spring-boot-dependencies` 已经管理的依赖（如 `spring-boot-starter-web`、`lombok`），BOM 直接复用其版本，不再重复声明。

**Q3：如何查看当前 BOM 中所有管理的依赖？**  
```bash
cd ddf-common-dependency && mvn dependency:tree
```
或查看 `.flattened-pom.xml` 中的完整 `dependencyManagement` 列表。

**Q4：子模块可以声明自己的版本吗？**  
可以但不推荐。子模块应信任 BOM 的版本管理。若确有特殊需求，可在子模块 `pom.xml` 中显式声明版本，该版本会覆盖 BOM 中的定义。

---

## 7. 参考

- 源码：`pom.xml`
- Maven BOM 文档：https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms
