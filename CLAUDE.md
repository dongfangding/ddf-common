# CLAUDE.md

此文件为 Claude Code 提供项目开发指导。

## 项目概述

**ddf-common** - Spring Boot 3.3 多模块通用组件库，为 Java 后端开发提供可复用组件。这是一个依赖库，不是可运行的应用。

- **Java 版本**: 17
- **框架**: Spring Boot 3.3.13
- **构建工具**: Maven 3.9.6+
- **Group ID**: com.ddf.common

## 项目结构

```
ddf-common/
├── pom.xml                          # 父 POM
├── ddf-common-dependency/           # 依赖版本管理 (BOM)
├── ddf-common-api/                  # 通用 API 定义和枚举
├── ddf-common-core/                 # 核心功能（基础配置）
├── ddf-common-mvc/                  # MVC 相关工具
├── ddf-common-authentication/       # JWT 认证授权
├── ddf-common-redis/                # Redis 集成 (Redisson)
├── ddf-common-distributed-lock/     # 分布式锁 (Redis/ZK)
├── ddf-common-limit/                # 限流和防重复提交
├── ddf-common-captcha/              # 验证码 (Google kaptcha + 安吉)
├── ddf-common-ids-service/          # ID 生成 (雪花算法/号段模式)
├── ddf-common-rocketmq/             # RocketMQ 集成
├── ddf-common-ons/                  # 阿里云 ONS
├── ddf-common-mqtt/                 # MQTT 协议支持
├── ddf-common-mqtt-client/          # MQTT 客户端
├── ddf-common-canal/                # Canal 数据库同步
├── ddf-common-mongo/                # MongoDB 集成
├── ddf-common-es/                   # Elasticsearch 集成
├── ddf-common-zookeeper/            # Zookeeper 服务发现
├── ddf-common-websocket/            # WebSocket 支持
├── ddf-common-netty-broker/         # Netty 自定义协议实现
├── ddf-common-xxl-executor/         # XXL-JOB 执行器
├── ddf-common-log4j/                # Log4j2 配置
├── ddf-common-sharding/             # ShardingSphere 集成
├── ddf-common-alarm/                # 告警通知模块
├── ddf-common-third-party/          # 第三方集成 (OSS, SMS)
├── ddf-common-vps/                  # VPS 工具（文件上传）
└── ddf-common-script/               # 工具脚本
```

## 核心依赖

在 `ddf-common-dependency/pom.xml` 中统一管理：

| 依赖                   | 版本         |
|----------------------|------------|
| Spring Boot          | 3.3.13     |
| Spring Cloud Context | 4.1.6      |
| Lombok               | 1.18.42    |
| Hutool               | 5.8.42     |
| MySQL Connector      | 9.1.0      |
| Druid                | 1.2.24     |
| Redisson             | 3.52.0     |
| MyBatis              | 3.0.4      |
| PageHelper           | 2.1.0      |
| JWT (jjwt)           | 0.12.6     |
| Guava                | 33.3.1-jre |
| Fastjson2            | 2.0.53     |
| Curator (Zookeeper)  | 5.1.0      |
| Canal                | 1.1.2      |
| XXL-Job              | 2.4.2      |
| Disruptor            | 3.4.4      |

## 包命名规范

- **核心模块**: `com.ddf.boot.common.*`
- **服务模块**: `com.ddf.common.*`

常用包结构：
```
config/         # Spring 自动配置类
properties/     # 配置属性类
model/          # 数据模型（请求/响应/DTO）
service/        # 服务接口和实现
api/            # API 定义（供外部模块使用）
annotation/     # 自定义注解
aspect/         # AOP 切面
handler/        # 事件/消息处理器
util/           # 工具类
enum/           # 枚举
exception/      # 自定义异常
```

## 编码规范

### 异常处理
- 异常抛出需抛出`com.ddf.boot.common.api.exception.BusinessException`
- 错误码定义在枚举中，实现统一接口`com.ddf.boot.common.api.exception.BaseCallbackCode`
- API 响应统一使用 `com.ddf.boot.common.api.model.common.response.ResponseData<T>` 包装
- 使用lombok，替代setter/getter

### 配置属性类
- 使用 `@ConfigurationProperties` 注解，prefix 命名规范
- 示例：`XxxProperties` 类使用 `@ConfigurationProperties(prefix = "xxx")`
- 放在 `properties/` 包下

### 自动配置类
- 所有自动配置类命名为 `*AutoConfiguration`
- 使用 `@AutoConfiguration` 或 `@Configuration` + `@Bean`
- 通过 `META-INF/spring.factories` 或 `META-INF/spring/*.imports` 导入 (Spring Boot 3.x)

### Redis Key
- redis key定义必须实现规范接口`com.ddf.boot.common.api.constraint.redis.RedisKeyConstraint`
- 遵循模式：`{prefix}:{module}:{action}:{identifier}`
如使用枚举实现接口示例
```java
public enum GameRedisKeyEnum implements RedisKeyConstraint {

    /**
     * 金猪-全局配置
     */
    GOLDEN_PIG_PRIZE_CONFIG("game:{golden_pig}:config", RedisKeyTypeEnum.HASH),
    ;

    /**
     * key模板，变量使用%s代替
     * 如sms_code:%s:%s
     */
    @Getter
    private final String template;

    /**
     * 过期秒数,这里不会根据这个做什么事情，自己定义自己使用就行，这里主要是一些固定业务使用的key过期时间是固定的，就在这里当常量定义了
     * 如短信验证码，需要的是一个常量的过期时间，那就在这里定义，用的时候引用这里就行，其它情况下意义不大
     */
    @Getter
    private Duration ttl;

    @Getter
    private final RedisKeyTypeEnum keyType;

    @Getter
    private Class clazz;

    /**
     * key的分片规则
     */
    @Getter
    private RedisShardingRule redisShardingRule;

    GameRedisKeyEnum(String template, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.ttl = Duration.ofSeconds(-1);
        this.keyType = keyType;
    }

    GameRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class clazz) {
        this.template = template;
        this.ttl = Duration.ofSeconds(-1);
        this.keyType = keyType;
        this.clazz = clazz;
    }

    GameRedisKeyEnum(String template, Duration ttl, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.ttl = ttl;
        this.clazz = clazz;
        this.keyType = keyType;
    }

    GameRedisKeyEnum(String template, Duration ttl, Class clazz, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.ttl = ttl;
        this.clazz = clazz;
        this.keyType = keyType;
    }

    GameRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class clazz, RedisShardingRule redisShardingRule) {
        this.template = template;
        this.keyType = keyType;
        this.clazz = clazz;
        this.redisShardingRule = redisShardingRule;
    }
}
```

### REST API 响应
```java
// 所有 API 响应使用 ResponseData<T> 包装
ResponseData.success(data);
ResponseData.failure(BaseCallbackCode.XXX);
ResponseData.empty();
```

## 构建命令

```bash
# 构建所有模块
mvn clean install -DskipTests

# 构建指定模块
mvn clean install -DskipTests -pl ddf-common-core -am

# 部署到仓库
mvn clean deploy -DskipTests

# 运行测试
mvn test
```

## 模块依赖关系

```
ddf-common-api           <- 所有模块依赖
    |
ddf-common-core          <- mvc, authentication, redis, limit 等
    |
ddf-common-dependency    <- (BOM，由父 pom 导入)
```

## 注意事项

1. **Spring Boot 3.x 迁移**: 当前为 `boot3.3` 分支，使用 Spring Boot 3.3.x 和 Jakarta EE（非 javax）
2. **第三方 Guava 版本冲突**: 不同模块可能使用不同版本的 Guava（因 Leaf/Elastic-Job 导致）
3. **Zookeeper 版本**: Curator 5.1.0 要求 ZooKeeper 3.4.x+
4. **不可直接运行**: 这是一个库项目，使用示例请参考 [spring-boot-quick](https://github.com/dongfangding/spring-boot-quick)

## 常用开发任务

### 添加新模块
1. 创建目录 `ddf-common-{module-name}/`
2. 在根 `pom.xml` 的 `modules` 列表中添加模块名
3. 创建 `pom.xml`，设置 parent 引用
4. 如有新依赖，在 `ddf-common-dependency/pom.xml` 中添加

### 添加依赖
1. 在 `ddf-common-dependency/pom.xml` 的 properties 节添加版本号
2. 如不在 Spring Boot dependencies 中，在 `dependencyManagement` 节添加
3. 在模块中使用时无需指定版本

### 测试
- 测试类放在 `src/test/java/`
- 使用 Spring Boot Test 注解
- 使用 Mockito 模拟依赖
