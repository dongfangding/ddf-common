# CLAUDE.md

## 模块简介

提供 Redis 集成和 Key 生成规范，包含 Redisson 客户端封装和限流 Lua 脚本。

## 核心类

| 类路径                                                               | 功能         |
|-------------------------------------------------------------------|------------|
| `com.ddf.boot.common.redis.constant.ApplicationNamedKeyGenerator` | Key 生成器    |
| `com.ddf.boot.common.redis.helper.RedisTemplateHelper`            | Redis 操作工具 |
| `com.ddf.boot.common.redis.script.RedisLuaScript`                 | Lua 脚本常量   |
| `com.ddf.boot.common.redis.ext.RedisBloomFilter`                  | 布隆过滤器      |

## 使用说明

### 1. Key 生成规范

```java
// 使用 KeyGenerator 生成规范 Key
String key = ApplicationNamedKeyGenerator.genKey("user", userId);
// 格式: {applicationName}:user:{userId}

// 忽略应用名
String globalKey = ApplicationNamedKeyGenerator.genKey(true, "global", "config");
// 格式: GLOBAL:global:config
```

### 2. 定义 Redis Key 枚举

```java
public enum UserRedisKeyEnum implements RedisKeyConstraint {

    USER_INFO("user:info:%s", RedisKeyTypeEnum.STRING, UserInfo.class),
    USER_TOKEN("user:token:%s", RedisKeyTypeEnum.STRING),
    ;

    @Getter
    private final String template;

    @Getter
    private final RedisKeyTypeEnum keyType;

    @Getter
    private final Class<?> clazz;

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType) {
        this.template = template;
        this.keyType = keyType;
        this.clazz = null;
    }

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class<?> clazz) {
        this.template = template;
        this.keyType = keyType;
        this.clazz = clazz;
    }

    @Override
    public String getKey(Object... params) {
        return String.format(template, params);
    }
}
```

### 3. 限流操作

```java
@Autowired
private RedisTemplateHelper redisHelper;

// 滑动窗口限流
AccessLimitResponse response = redisHelper.sliderWindowAccess(
    "api:limit:%s".formatted(userId),  // Key
    100,                                 // 最大次数
    60                                   // 时间窗口（秒）
);
if (response.isLimited()) {
    throw new BusinessException("访问过于频繁");
}

// 令牌桶限流
boolean acquired = redisHelper.tokenBucketRateLimitAcquire(
    LeakyBucketRateLimitRequest.builder()
        .key("rate:limit:%s".formatted(userId))
        .rate(10)                           // 速率
        .rateIntervalSeconds(1)             // 速率间隔
        .build()
);

// 漏桶限流
boolean acquired = redisHelper.leakyBucketRateLimitAcquire(
    LeakyBucketRateLimitRequest.builder()
        .key("leaky:bucket:%s".formatted(userId))
        .rate(10)
        .capacity(100)
        .build()
);
```

### 4. 布隆过滤器

```java
@Autowired
private RedisTemplateHelper redisHelper;

// 创建布隆过滤器
RedisBloomFilter<String> bloomFilter = redisHelper.createRedisBloomFilter(
    "user:bloom",           // 名称
    100000,                 // 预期插入数
    0.001                   // 误判率
);

// 添加元素
bloomFilter.add("user_001");

// 检查元素是否存在
boolean exists = bloomFilter.contains("user_001");
```

### 5. 发布订阅

```java
@Autowired
private StringRedisTemplate redisTemplate;

// 发布消息
redisTemplate.convertAndSend("channel:user", "user_001");

// 监听消息（需自行实现 RedisMessageListenerContainer）
```

## 配置说明

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 0
```

## 注意事项

1. **Key 规范**：必须使用 `ApplicationNamedKeyGenerator` 生成 Key，避免多应用冲突
2. **序列化**：使用 JSON 序列化，如需特殊序列化请自行配置 `RedisTemplate`
3. **连接池**：Redisson 使用连接池，高并发场景注意配置连接数
4. **Lua 脚本**：限流脚本已预加载，支持集群环境
