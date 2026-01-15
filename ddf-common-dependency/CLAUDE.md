# CLAUDE.md

## 模块简介

依赖版本统一管理模块，提供 BOM (Bill of Materials) 管理。

## 使用说明

### 引入 BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.ddf.common</groupId>
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
        <groupId>com.ddf.common</groupId>
        <artifactId>ddf-common-core</artifactId>
    </dependency>
</dependencies>
```

## 添加新依赖

1. 在 `properties` 节添加版本号
2. 在 `dependencyManagement` 节添加依赖声明
