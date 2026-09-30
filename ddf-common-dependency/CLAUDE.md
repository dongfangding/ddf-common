# CLAUDE.md

## 模块简介

依赖版本统一管理模块，提供 BOM (Bill of Materials) 管理。

## 使用说明

### 引入 BOM

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

### 子模块引入依赖（无需指定版本）

```xml
<dependencies>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-core</artifactId>
    </dependency>
</dependencies>
```

本 BOM 已统管 ddf-common 全部自有模块坐标（版本统一为 `${revision}`，发布时由 flatten 物化）。
**例外**：`ddf-common-script`、`ddf-common-netty-broker` 不发布到 Maven Central，不在 BOM 管理范围，需要时须显式指定版本。

## 添加新依赖

1. 在 `properties` 节添加版本号
2. 在 `dependencyManagement` 节添加依赖声明
3. 新增 ddf-common 子模块时，记得在本 pom 的 `dependencyManagement` 登记其坐标，否则消费方引入时仍需手写版本
