# ddf-common-dependency

依赖版本统一管理模块，提供 BOM (Bill of Materials) 管理。

## 功能特性

- 统一管理所有模块依赖版本
- 避免依赖冲突
- 简化子模块依赖引入

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

### 子模块引入依赖

```xml
<dependencies>
    <dependency>
        <groupId>io.github.dongfangding</groupId>
        <artifactId>ddf-common-core</artifactId>
    </dependency>
    <!-- 无需指定版本，版本由 BOM 统一管理 -->
</dependencies>
```

## 管理的依赖版本

| 依赖          | 版本         |
|-------------|------------|
| Spring Boot | 3.3.13     |
| Lombok      | 1.18.42    |
| Hutool      | 5.8.42     |
| Redisson    | 3.52.0     |
| MyBatis     | 3.0.4      |
| JWT (jjwt)  | 0.12.6     |
| Guava       | 33.4.0-jre |
| Fastjson2   | 2.0.53     |
| Curator     | 5.3.0      |
| XXL-Job     | 2.4.2      |

## 添加新依赖

在 `ddf-common-dependency/pom.xml` 中添加：
1. 在 `properties` 节添加版本号
2. 在 `dependencyManagement` 节添加依赖声明
