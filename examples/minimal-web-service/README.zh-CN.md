# 最小 Web 服务示例

[English](./README.md) | [中文](./README.zh-CN.md)

这个示例展示了基于 `ddf-common-starter-web` 构建轻量 Spring Boot Web 服务的最小推荐接入方式。

该目录以文档说明为主，不参与根 Maven reactor 构建。

## pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.9</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>minimal-web-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>17</java.version>
        <ddf-common.version>boot3.5-2026.1</ddf-common.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.github.dongfangding</groupId>
            <artifactId>ddf-common-starter-web</artifactId>
            <version>${ddf-common.version}</version>
        </dependency>
    </dependencies>
</project>
```

## 应用入口

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    static class DemoController {

        @GetMapping("/ping")
        public String ping() {
            return "pong";
        }
    }
}
```

## application.yml

```yaml
spring:
  application:
    name: minimal-web-service
```
