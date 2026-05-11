# ddf-common-dependency

> Dependency version unified management module (BOM). Centralizes version declarations for all ddf-common modules and third-party dependencies,
> so child modules and downstream applications don't need to specify versions, avoiding dependency conflicts.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-dependency` solves the **"dependency version fragmentation and conflicts"** problem.

| Category | Typical Problem | What the Module Provides |
| ----- | ----- | ----- |
| Multi-module version alignment | 20+ submodules each declare their own versions; easy to miss one during upgrade | Declare once, effective globally |
| Dependency conflicts | Module A uses Redisson 3.20, Module B uses 3.50 | BOM locks versions uniformly |
| Business onboarding simplification | Business projects need to remember many version numbers | With BOM imported, all version numbers are omitted |
| Tech stack upgrades | Spring Boot 3.3 → 3.5 requires editing dozens of poms | Change one property in the BOM |

---

## 2. Maven Dependency

`ddf-common-dependency` is a **Maven BOM (Bill of Materials)**, imported via `dependencyManagement`:

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

After importing, child modules or business projects can use dependencies without specifying versions:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-core</artifactId>
        <!-- Version managed by BOM, no need to declare -->
    </dependency>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-starter-web</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Core Design

### 3.1 Version properties

All versions in the BOM are centrally managed in `<properties>`:

| Property | Current version | Description |
| ----- | ----- | ----- |
| `spring-boot.version` | `3.5.9` | Spring Boot base version |
| `revision` | `boot3.5-2026.1-SNAPSHOT` | ddf-common global version |
| `java.version` | `17` | JDK version |
| `mysql.version` | `9.1.0` | MySQL driver |
| `druid.version` | `1.2.27` | Druid connection pool |
| `redisson.version` | `3.52.0` | Redisson |
| `mybatis.version` | `3.0.4` | MyBatis Spring Boot Starter |
| `jwt.version` | `0.12.6` | JJWT |
| `hutool.version` | `5.8.42` | Hutool |
| `curator.version` | `5.3.0` | Curator (Zookeeper) |
| `rocketmq.version` | `2.3.3` | RocketMQ Spring Boot Starter |
| `fastjson2.version` | `2.0.58` | Fastjson2 |
| `guava.version` | `33.4.8-jre` | Guava |
| `xxl-job-version` | `3.3.0` | XXL-Job |

### 3.2 Dependency management scope

The BOM manages two categories:

1. **Internal ddf-common modules**: All `ddf-common-*` modules use `${revision}` uniformly
2. **Third-party dependencies**: Spring Boot, databases, caches, message queues, utility libraries, etc.

> Before adding a new dependency, check whether `spring-boot-dependencies` already manages it.
> If so, use the Spring Boot managed version directly to avoid duplicate declarations.

---

## 4. Advanced Usage / Extension Points

### 4.1 Overriding versions in business projects

Business projects can override BOM versions in their own `pom.xml`:

```xml
<properties>
    <!-- Override Redisson version -->
    <redisson.version>3.55.0</redisson.version>
</properties>
```

> Override precedence: business `pom.xml` > BOM `properties` > `spring-boot-dependencies`

### 4.2 Adding a new dependency to the BOM

In `ddf-common-dependency/pom.xml`, perform two steps:

1. Add the version in the `properties` section:
```xml
<my-new-lib.version>1.0.0</my-new-lib.version>
```

2. Add the dependency declaration in the `dependencyManagement` section:
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-new-lib</artifactId>
    <version>${my-new-lib.version}</version>
</dependency>
```

### 4.3 Publishing the BOM

The BOM is published to Maven Central together with ddf-common:

```bash
mvn -Prelease clean deploy
```

After publication, business projects only need to change `${ddf-common.version}` to upgrade all dependencies.

---

## 5. Interplay with Other Modules

| Module | How They Cooperate |
| ----- | ----- |
| All `ddf-common-*` modules | Versions managed uniformly by the BOM's `${revision}` |
| `spring-boot-dependencies` | The BOM imports Spring Boot BOM first, then supplements custom dependencies on top |
| Business projects | Import the BOM via `scope=import`, omitting all version numbers |

---

## 6. FAQ

**Q1: What's the difference between BOM and parent POM?**  
- Parent POM (`ddf-common/pom.xml`): manages module aggregation, build plugins, and release configuration
- BOM (`ddf-common-dependency/pom.xml`): only manages dependency versions, not build lifecycle
- Business projects typically only import the BOM, not inherit the parent POM

**Q2: Why do some dependencies lack version numbers in the BOM?**  
Dependencies already managed by `spring-boot-dependencies` (e.g. `spring-boot-starter-web`, `lombok`) reuse their versions directly; no duplicate declaration is needed in the BOM.

**Q3: How do I view all dependencies managed by the current BOM?**  
```bash
cd ddf-common-dependency && mvn dependency:tree
```
Or check the full `dependencyManagement` list in `.flattened-pom.xml`.

**Q4: Can child modules declare their own versions?**  
Yes, but not recommended. Child modules should trust the BOM's version management. If there is a genuine special need, explicitly declaring a version in the child module's `pom.xml` will override the BOM definition.

---

## 7. References

- Source: `pom.xml`
- Maven BOM docs: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#bill-of-materials-bom-poms
