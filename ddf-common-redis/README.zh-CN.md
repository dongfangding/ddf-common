# ddf-common-redis

> Redis 基础设施模块：在 Spring Data Redis 与 Redisson 之上提供统一的 `RedisTemplate`、
> 命令包装器、原子 Lua 脚本、限流器、布隆过滤器、业务榜单、多客户端等能力，
> 让业务代码摆脱样板序列化、key 拼接、并发原子性等细节。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-redis` 不是一层简单封装，它是 ddf-common 中 **承担"分布式状态"职责** 的基础设施模块。
当你的项目出现下列任一诉求，就应该引入它：

| 场景类别 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 缓存/读多写少 | 想要统一 JSON 序列化、避免每个 key 自己写 prefix | `redisTemplate` / `stringRedisTemplate` 默认序列化器 + `ApplicationNamedKeyGenerator` |
| 并发限流 | 接口防刷、登录验证码限频、活动限速 | `RedisTemplateHelper#sliderWindowAccess` / `tokenBucketRateLimitAcquire` / `leakyBucketRateLimitAcquire` |
| 计数器 | 用户每日发文数、订单批次计数，需要"自增 + TTL + 上限校验" 原子完成 | `incrementKeyExpire` / `hashIncreaseCheck` / `stringIncrWithLimit` 等 30+ Lua 脚本 |
| 业务榜单 | 周榜/月榜，要求分数相同时按时间先后排序 | `zSetAddWithMaxCheckSupportBiz` / `zSetRevRangeBizRankingQuery` |
| 防穿透 | 大量空查询击穿到数据库 | `RedisBloomFilter` 基于 Redisson 实现的分布式布隆过滤器 |
| 多 Redis 数据源 | 主业务库 + 风控库 + 缓存库分离 | `customizer.infra.redis.extra-multi` 一份配置即可拉起多套 `RedissonClient` / `RedisTemplate` |
| 地理位置 | 附近的人、附近门店 | `GeoHelper` 基于 Redisson `RGeo` |
| 发布订阅 | 跨实例广播配置变更、踢人下线 | `RedisTopic` 基于 Redisson `RTopic` |

> ⚠️ 本模块不包含 Spring Cache 注解（`@Cacheable`），如果只需要方法级缓存，
> 请自行启用 Spring Boot 原生 `spring-boot-starter-cache`。

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-redis</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

传递依赖：

- `spring-boot-starter-data-redis`（含 Lettuce 客户端）
- `redisson-spring-boot-starter`（Redisson 客户端）
- `hutool-core`、`guava`、`caffeine`

> 如果你只需要 `ApplicationNamedKeyGenerator` 这类纯工具类，可以把依赖标记为 `<optional>true</optional>`，
> 避免把 Redisson 强制传给下游模块。

---

## 3. 最小化配置

模块复用 Spring Boot 原生 `RedisProperties`（前缀 `spring.data.redis`），无需任何自定义命名空间即可跑通。

### 3.1 单机模式

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your_password
      database: 0
      timeout: 3s
```

### 3.2 哨兵模式

```yaml
spring:
  data:
    redis:
      password: your_password
      database: 0
      sentinel:
        master: mymaster
        nodes: 10.0.0.1:26379,10.0.0.2:26379,10.0.0.3:26379
```

### 3.3 集群模式

```yaml
spring:
  data:
    redis:
      password: your_password
      cluster:
        nodes: 10.0.0.1:7001,10.0.0.2:7002,10.0.0.3:7003
```

模块的 `RedisCustomizeAutoConfiguration` 会自动检测 `sentinel` / `cluster` / 单机三种连接方式并注入到 Redisson。

### 3.4 自动注入的 Bean

启动后默认得到下列 Bean，可直接 `@Resource` / `@Autowired` 使用：

| Bean 名 | 类型 | 说明 |
| ----- | ----- | ----- |
| `redisTemplate` | `RedisTemplate<Object, Object>` | 通用模板，值使用 `GenericJackson2JsonRedisSerializer` |
| `stringRedisTemplate` | `StringRedisTemplate` | 模块默认采用 `ObjectStringRedisSerializer`，自动处理对象 ↔ String |
| `redisTemplateHelper` | `RedisTemplateHelper` | Lua 脚本 + Redisson 复合能力封装 |
| `redisCommandHelper` | `RedisCommandHelper` | Redis 标准命令包装器，约 1980 行覆盖所有数据结构 |
| `geoHelper` | `GeoHelper` | 基于 Redisson `RGeo` 的地理位置工具 |
| `redissonClient` | `RedissonClient` | 由 Redisson Starter 注入，本模块通过 `RedissonAutoConfigurationCustomizer` 注入 Codec 与连接信息 |

---

## 4. 核心 API 使用指南

### 4.1 统一 Key 生成：`ApplicationNamedKeyGenerator`

为了避免不同应用写到同一个 Redis 时 key 冲突，约定所有 key 都以 `applicationName` 开头：

```java
// 假设 spring.application.name=user-service
String key = ApplicationNamedKeyGenerator.genKey("user", "info", String.valueOf(userId));
// → user-service:user:info:1001

// 跨应用共享场景（如全局开关）：用 "global" 前缀
String globalKey = ApplicationNamedKeyGenerator.genKey(true, "config", "feature-flag");
// → global:config:feature-flag

// 不带应用前缀（用于完全自定义命名空间，如三方对接）
String normal = ApplicationNamedKeyGenerator.genNormalKey("third-party", "wechat", openId);
// → third-party:wechat:xxx
```

推荐通过 **枚举 + 模板** 的方式管理项目内 key：

```java
public enum UserRedisKeyEnum implements RedisKeyConstraint {

    USER_INFO("user:info:%s", RedisKeyTypeEnum.STRING, UserInfo.class),
    LOGIN_TOKEN("user:token:%s", RedisKeyTypeEnum.STRING),
    LOGIN_FAIL_COUNT("user:login-fail:%s", RedisKeyTypeEnum.STRING),
    ;

    @Getter private final String template;
    @Getter private final RedisKeyTypeEnum keyType;
    @Getter private final Class<?> clazz;

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType) {
        this(template, keyType, null);
    }

    UserRedisKeyEnum(String template, RedisKeyTypeEnum keyType, Class<?> clazz) {
        this.template = template;
        this.keyType = keyType;
        this.clazz = clazz;
    }

    @Override
    public String getKey(Object... params) {
        // 由 RedisKeyConstraint 提供 ApplicationNamedKeyGenerator 拼接逻辑
        return String.format(template, params);
    }
}
```

> `RedisKeyConstraint` 来自 `ddf-common-core`，详见 [core 模块的 key 规范](../ddf-common-core/CLAUDE.md)。

### 4.2 基础命令封装：`RedisCommandHelper`

`RedisCommandHelper` 是对 `StringRedisTemplate` 的标准命令包装，作用是 **隐藏 byte/序列化细节、统一返回类型**，约 1980 行覆盖：

```java
@Resource
private RedisCommandHelper redisCommandHelper;

// String
redisCommandHelper.setEx("user:token:1001", "abc-token", 3600, TimeUnit.SECONDS);
String token = redisCommandHelper.get("user:token:1001");
boolean acquired = redisCommandHelper.setIfAbsent("user:lock:1001", "v", Duration.ofSeconds(10));
long count = redisCommandHelper.incrBy("user:visit:1001", 1L);

// Hash
redisCommandHelper.hPut("user:profile:1001", "nickname", "Tom");
String nickname = redisCommandHelper.hGet("user:profile:1001", "nickname");
Map<String, String> all = redisCommandHelper.hScan("user:profile:1001", "*");

// ZSet
redisCommandHelper.zAdd("leaderboard:week", "user:1001", 99.5);
Set<TypedTuple<String>> top10 = redisCommandHelper.zReverseRangeWithScores("leaderboard:week", 0, 9);

// Pipeline 批量
redisCommandHelper.executePipelined(conn -> {
    for (Long id : userIds) {
        conn.stringCommands().get(("user:info:" + id).getBytes());
    }
    return null;
});
```

完整方法清单请参考源码 `RedisCommandHelper.java`。

### 4.3 分布式限流：`RedisTemplateHelper`

#### 4.3.1 滑动窗口限流

适合"X 秒内最多调用 Y 次"的接口限频：

```java
@Resource
private RedisTemplateHelper redisTemplateHelper;

public void sendSms(String mobile) {
    AccessLimitResponse resp = redisTemplateHelper.sliderWindowAccess(
        "sms:send:" + mobile,
        5,    // 窗口内最多 5 次
        60    // 窗口大小 60 秒
    );
    if (resp.isLimited()) {
        throw new BusinessException("短信发送过于频繁，请稍后再试");
    }
    // 真正发送短信
}
```

返回值 `AccessLimitResponse` 同时返回当前命中次数 + 最大次数，方便前端展示"剩余次数"。

#### 4.3.2 令牌桶限流（基于 Hash 自实现，无 Redisson 依赖）

适合需要"持续以固定速率消耗令牌"的场景，例如 OpenAPI 网关：

```java
boolean acquired = redisTemplateHelper.tokenBucketRateLimitAcquire(
    RateLimitRequest.builder()
        .key("api:rate:" + userId)
        .max(100)   // 桶容量
        .rate(10)   // 每秒补充 10 个令牌
        .build()
);
if (!acquired) {
    throw new BusinessException("访问过于频繁");
}
```

> `RateLimitRequest.builder()` 默认会给 key 加上 `rate_limit:` 前缀，
> 如果你已经在外层拼好命名空间，可调用 `.ignorePrefix(true)`。

#### 4.3.3 漏桶限流（Redisson `RRateLimiter`）

适合需要"恒定速率出水、突发不堆积" 的场景：

```java
boolean acquired = redisTemplateHelper.leakyBucketRateLimitAcquire(
    LeakyBucketRateLimitRequest.builder()
        .key("login:leaky:" + ip)
        .rate(10)                      // 每个间隔补充 10 个令牌
        .rateIntervalSeconds(1)        // 间隔 1 秒
        .permits(1)                    // 本次申请 1 个
        .build()
);
```

### 4.4 计数器 + 边界校验

#### 4.4.1 自增并设置 TTL（首次自增设过期）

```java
// 用户每日发文数，自增到 100 即不再允许
long current = redisTemplateHelper.incrementKeyExpire(
    "user:post-count:" + LocalDate.now(),
    1,                      // 自增步长
    86400                   // 首次自增时设置 TTL=1 天
);
if (current > 100) {
    throw new BusinessException("当日发文已达上限");
}
```

#### 4.4.2 Hash 自增并带上限回滚（原子）

下面这段在并发下也能保证"超过上限时回滚自增、不向调用方返回成功"：

```java
HashIncrementCheckResponse resp = redisTemplateHelper.hashIncreaseCheck(
    "user:quota:" + userId,
    "monthly-export",
    1,
    50    // 月度导出上限 50 次
);
if (!resp.isSuccess()) {
    throw new BusinessException(
        "本月导出次数已达 " + resp.getLimitValue() + " 次上限");
}
```

#### 4.4.3 String 自增并抛业务异常

如果上限触发时直接希望中断业务流，可用 `*CheckException` 系列方法：

```java
redisTemplateHelper.stringIncrWithLimitCheckException(
    "ip:attempt:" + clientIp,
    1,
    10,                              // 上限 10 次
    300,                             // TTL 300 秒
    ErrorCodeEnum.LOGIN_TOO_FREQUENT // 触发上限时抛出 BusinessException
);
```

### 4.5 安全删除（CAS 风格）

避免"读取 → 判断 → 删除"间被其他线程改写：

```java
boolean removed = redisTemplateHelper.stringDeleteWithCheckValue(
    "session:" + sessionId, expectedToken);
boolean hashRemoved = redisTemplateHelper.hashDeleteWithCheckValue(
    "user:lock", userId, expectedOwner);
```

### 4.6 业务榜单（分数相同时按时间排序）

`RedisTemplateHelper` 内置一组业务级 zset 工具，特别处理了"分数相同时按提交时间先后排名"问题（用毫秒作为分数小数位）：

```java
// 写入：同一分数下，先到者排名靠前
redisTemplateHelper.zSetAddWithMaxCheckSupportBiz(
    ZSetAddDoubleWithMaxCheckCommand.builder()
        .key("leaderboard:weekly")
        .member("user:1001")
        .score(99.5)
        .build()
);

// 查询 Top N
ZRevRangeBizRankingResponse top = redisTemplateHelper.zSetRevRangeBizRankingQuery(
    ZRevRangeBizRankingQuery.builder()
        .key("leaderboard:weekly")
        .start(0L)
        .end(9L)
        .build()
);

// 查询某用户的排名 + 前后 3 名
RankResponse around = redisTemplateHelper.rankAround(
    "leaderboard:weekly", "user:1001", 3);
```

### 4.7 布隆过滤器

```java
RedisBloomFilter<String> filter = redisTemplateHelper.createRedisBloomFilter(
    "user:registered",
    1_000_000,    // 预期插入量
    0.001         // 期望误判率 0.1%
);

filter.add("13800138000");
boolean maybeExists = filter.contains("13800138000");
```

> 布隆过滤器 **必须先初始化数据**，否则"不存在"判断也会失效。
> 通常在应用启动 / 数据变更后批量 `add()` 一次。

### 4.8 发布订阅

```java
RedisTopic topic = RedisTopic.newInstance("config:change", redissonClient);
topic.addListener(ConfigChangeEvent.class, (channel, event) -> {
    log.info("收到配置变更 {}", event);
    configCache.invalidate(event.getKey());
});

topic.publish(new ConfigChangeEvent("feature.new-ui", "true"));
```

---

## 5. 进阶用法 / 扩展点

### 5.1 多 Redis 数据源

当主业务 Redis 之外还需要独立的风控库 / 缓存库时，开启 `customizer.infra.redis.extra-multi`：

```yaml
spring:
  data:
    redis:
      host: 10.0.0.1
      port: 6379
      password: main-pass

customizer:
  infra:
    redis:
      extra-multi:
        enable: true
        map:
          risk:
            host: 10.0.0.2
            port: 6379
            password: risk-pass
            database: 0
          cache:
            host: 10.0.0.3
            port: 6379
            password: cache-pass
            database: 1
```

启动后会注入下列以 **map key 为前缀** 的 Bean：

- `riskRedissonClient` / `riskRedissonConnectionFactory` / `riskStringRedisTemplate` / `riskRedisTemplate` / `riskRedisCommandHelper`
- `cacheRedissonClient` / `cacheRedissonConnectionFactory` / `cacheStringRedisTemplate` / `cacheRedisTemplate` / `cacheRedisCommandHelper`

```java
@Resource(name = "riskRedisCommandHelper")
private RedisCommandHelper riskRedis;
```

> 多客户端模式目前只支持 **单机连接方式**，不支持哨兵 / 集群。

### 5.2 自定义 Redisson Codec

```yaml
spring:
  redis:
    redisson:
      codec: org.redisson.codec.Kryo5Codec
```

未配置时默认使用 `JsonJacksonCodec`。

### 5.3 注册自定义 Lua 脚本

参考 `RedisLuaScript` 接口，把脚本放到 `classpath:lua/xxx.lua`，再以 `RedisScript.of(...)` 声明常量；然后通过 `stringRedisTemplate.execute(script, keys, args)` 调用即可。模块内已提供 30+ 个脚本：

| 分类 | 脚本常量 |
| ----- | ----- |
| 限流 | `TOKEN_BUCKET_RATE_LIMIT`、`SLIDER_WINDOW_COUNT` |
| 计数 | `STRING_KEY_INCREMENT_EXPIRE`、`STRING_KEY_INCREMENT_EXPIRE_AT`、`STRING_INCREMENT_CHECK`、`STRING_TTL_INCR_WITH_LIMIT` |
| Hash | `HASH_INCREMENT_CHECK`、`HASH_DECREMENT_CHECK`、`HASH_BATCH_INCREMENT_CHECK`、`MULTIPLE_HASH_BATCH_INCREMENT_CHECK`、`HASH_INCREMENT_PERSIST_LIMIT_VALUE`、`HASH_INCREASE_ROUNDING_REDUCE`、`HASH_INCR_WITH_FIRST_SET_TTL`、`HASH_INCR_FLOAT_ROUND_DECIMAL`、`HASH_DECREASE_UNTIL_FIRST_LESS_THAN_ZERO`、`HASH_VALUE_UPDATE_SELECTIVE` |
| 安全删除 | `HASH_DELETE_WITH_CHECK_VALUE`、`STRING_DELETE_WITH_CHECK_VALUE` |
| ZSet 业务榜单 | `ZSET_INCR_WITH_TIME`、`ZSET_ZADD_WITH_MAX_CHECK`、`ZSET_ZADD_WITH_TIME_MAX_CHECK`、`ZSET_AROUND_ELEMENT_RANK`、`ZSET_REV_RANGE_BIZ_RANKING_QUERY`、`ZSET_REV_RANGE_USER_BIZ_RANKING_ELEMENT_QUERY`、`ZSET_DELETE_WITH_MAX_SCORE_CHECK`、`ZSET_RANGEBYSCORE_ZREM` |
| 其他 | `MAX_CAPACITY_HISTORY_CONTAINER`、`MAX_ELEMENT_DICT` |

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-core` | 提供 `RedisKeyConstraint` 接口、`BusinessException`、`SpringContextHolder`（`ApplicationNamedKeyGenerator` 通过它获取 `spring.application.name`） |
| `ddf-common-limit` | 注解式限流（`@RateLimit`）底层调用 `RedisTemplateHelper.sliderWindowAccess` |
| `ddf-common-alarm` | 报警去重 / 抑制窗口使用 `AlarmRedisKeyEnum` + `hashIncreaseCheck` 实现 |
| `ddf-common-authentication` | Token / 验证码存储；登录失败次数累计建议使用 `stringIncrWithLimitCheckException` |
| `ddf-common-mq` | 跨实例事件广播可走 `RedisTopic` 而非引入完整 MQ |

---

## 7. 常见问题（FAQ）

**Q1：value 中文乱码？**  
默认 `stringRedisTemplate` 使用 UTF-8，业务上若误用了 JDK 默认 `RedisTemplate` 的 `JdkSerializationRedisSerializer`，把 key 序列化成了二进制，会出现 `redis-cli` 中乱码 / 控制字符。请始终通过 `stringRedisTemplate` 或 `redisTemplateHelper` 操作。

**Q2：为什么 hashIncreaseCheck 触发上限后还能再加 1？**  
Lua 脚本里"先 incr 再比较再回滚"，期间已经产生了一次自增。脚本会在超过上限时回滚到 `limit`，所以 **不会持久越界**，但客户端看到的 `currentValue` 可能等于 `limit` 而非 `limit-1`，这是脚本设计行为。

**Q3：为什么 zSet 业务榜单的 score 出现了小数位？**  
为了在分数相同时按时间先后排序，模块用 `9999999999 - 当前时间戳` 作为小数位填入分数，越早提交小数越大、整体分数越靠前。
通过 `zSetRevRangeBizRankingQuery` 返回的 `score` 会自动还原为业务真实分数。

**Q4：可以直接使用 `RedisTemplate` 操作模块创建的 key 吗？**  
可以。模块的 `redisTemplate` 默认 `GenericJackson2JsonRedisSerializer`，与 `RedisTemplateHelper` 写入的 JSON 结构兼容。
但请避免混用 `RedisTemplate` + `StringRedisTemplate` 读写同一个 key（序列化器不同会造成读取失败）。

**Q5：连接池怎么调？**  
Redisson 与 Lettuce 各有连接池配置：

- Lettuce：`spring.data.redis.lettuce.pool.*`
- Redisson：`spring.data.redis.redisson.config`（YAML 字符串）+ `spring.redis.redisson.codec`

高并发场景建议 `connectionPoolSize` ≥ 64、`connectionMinimumIdleSize` ≥ 24。

**Q6：怎样关闭本模块的自动装配？**  
在 `application.yml` 中显式排除：

```yaml
spring:
  autoconfigure:
    exclude:
      - com.ddf.boot.common.redis.config.RedisCustomizeAutoConfiguration
      - com.ddf.boot.common.redis.config.ExtraRedissonAutoConfiguration
```

---

## 8. 参考

- 源码 `RedisTemplateHelper.java`：Lua 脚本驱动的高阶 API
- 源码 `RedisCommandHelper.java`：标准命令包装器
- Redisson 配置文档：<https://github.com/redisson/redisson/wiki/2.-配置方法>
- Spring Data Redis 文档：<https://docs.spring.io/spring-data/redis/reference/>
