# ddf-common-api

> API 协议与约束根模块。承载所有 ddf-common 模块共享的 **响应契约、异常体系、分页模型、签名接口、
> Redis Key 约束、树形结构约束** 等纯协议类，**不依赖任何业务基础设施**，也不包含 Spring Bean。
> 它是整个 ddf-common 体系的依赖根，被 `ddf-common-core` 以及所有下游模块直接引用。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-api` 是四层架构中**最底层**的模块。当你需要在多个服务之间共享一组不依赖 Spring 容器的
纯 Java 契约时，应该把它放到 `pom.xml` 的第一行。

| 场景 | 典型问题 | 模块提供的能力 |
| ----- | ----- | ----- |
| 跨服务统一响应格式 | 各服务返回 JSON 结构不一致，网关聚合困难 | `ResponseData<T>` 统一响应体 |
| 跨服务统一异常语义 | A 服务抛 `USER_NOT_FOUND`，B 服务抛 `用户不存在`，网关无法映射 | `BaseCallbackCode` + `BaseErrorCallbackCode` 枚举 |
| 分页协议 | MyBatis、JPA、Mongo 各自返回不同分页结构 | `PageRequest` 接口 + `PageResult<T>` 实体 |
| 接口防篡改 | 开放网关需要验签，请求 DTO 需携带签名字段 | `BaseSign` 签名接口（`sign` + `nonceTimestamp`） |
| Redis Key 治理 | 各模块 key 格式混乱、拼接出错 | `RedisKeyConstraint` 模板约束 + 分片规则 |
| 树形结构 | 部门/菜单/地区列表需递归组装 | `ITreeTagCollection<K, T>` 树节点约束 |
| 序列化兼容 | 前后端对 `LocalDateTime` / `Duration` 序列化行为不一致 | Jackson 定制模块（JSR-310 + MsgPack） |
| 敏感数据脱敏 | 日志打印时不小心输出手机号 / 身份证号 | `@SensitiveField` + 脱敏序列化器 |

> ⚠️ 本模块**不包含**任何 Spring Bean、自动配置、数据库访问或 Redis 操作。需要基础设施时，
> 请向上依赖 `ddf-common-core` 或具体 starter。

---

## 2. 依赖引入

作为纯协议层，本模块的依赖极轻，只引入必要的校验、序列化与工具库：

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-api</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

传递依赖：

- `spring-boot-starter-validation`（含 `jakarta.validation-api`、`hibernate-validator`）
- `jackson-datatype-jsr310`、`jackson-dataformat-msgpack`
- `pagehelper-spring-boot-starter`（分页接口适配）
- `hutool-core`、`commons-lang3`、`commons-codec`、`guava`
- `lombok`

> 没有 Spring Boot 启动器（`spring-boot-starter`），也没有数据库驱动、Redis 客户端等重型依赖。

---

## 3. 核心 API 使用指南

### 3.1 统一响应：`ResponseData<T>`

```java
// Controller 返回（全局异常处理与 AOP 包装会自动处理，业务代码通常直接返回原始对象）
ResponseData.success(userInfo);                              // 成功
ResponseData.success(userInfo, "登录成功");                   // 成功 + 自定义提示
ResponseData.failure(BaseErrorCallbackCode.UNAUTHORIZED);    // 失败
ResponseData.empty();                                        // 空响应
```

结构：

```json
{
  "code": "200",
  "message": "请求成功",
  "subMessage": "详细错误信息（生产环境通过 @JsonIgnoreProfile 隐藏）",
  "timestamp": 1700000000000,
  "data": { ... },
  "extra": { ... }
}
```

消费端快捷方法：

```java
// 强制断言：如果非成功，把 ResponseData 里的异常重新抛为 BusinessException
UserInfo user = remoteUserService.get(userId).requiredSuccess();

// 失败降级
UserInfo user = remoteUserService.get(userId).failureDefault(UserInfo.guest());
```

### 3.2 异常体系

#### 3.2.1 错误码接口：`BaseCallbackCode`

```java
public enum OrderErrorCode implements BaseCallbackCode {
    ORDER_NOT_FOUND("ORDER_001", "订单不存在"),
    ORDER_OVER_LIMIT("ORDER_002", "当日下单已达{0}单上限", "下单次数超限");

    private final String code;
    private final String description;   // 内部/联调使用
    private final String bizMessage;    // 用户可见（toast），可覆盖
}
```

每个错误码必须提供 `code`、`description`，并可选择性提供 `bizMessage`（用于生产环境隐藏系统细节）。

#### 3.2.2 预定义错误码：`BaseErrorCallbackCode`

| 枚举 | code | 说明 |
| ----- | ----- | ----- |
| `COMPLETE` | `200` | 请求成功 |
| `BAD_REQUEST` | `BAD_REQUEST` | 错误请求（默认模糊化） |
| `UNAUTHORIZED` | `401` | 未认证 |
| `ACCESS_FORBIDDEN` | `403` | 权限不足 |
| `SERVER_ERROR` | `SERVER_ERROR` | 服务端异常（默认模糊化） |
| `BIZ_EXCEPTION` | `BIZ_EXCEPTION` | 通用业务异常 |
| `ENTRY_NOT_EXISTS` | `ENTRY_NOT_EXISTS` | 数据不存在（用户提示：操作资源不存在） |
| `DUPLICATE_KEY` | `DUPLICATE_KEY` | 唯一约束冲突（用户提示：已存在相同记录） |
| `SIGN_ERROR` | `SIGN_ERROR` | 签名校验失败 |

#### 3.2.3 抛出异常

```java
// 最常用：直接抛业务异常
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);

// 占位符填充
throw new BusinessException(OrderErrorCode.ORDER_OVER_LIMIT, 10);

// 携带扩展数据（例如余额不足时返回缺少金额）
throw new BusinessException(new ShortageInfo(500L), OrderErrorCode.BALANCE_INSUFFICIENT);

// 网关层使用：故意模糊化系统异常，不让堆栈泄露到前端
throw new ServerErrorException(BaseErrorCallbackCode.SERVER_ERROR);
```

异常语义：

| 异常类 | 默认错误码 | `isMaskErrorDetails()` | 使用场景 |
| ----- | ----- | ----- | ----- |
| `BusinessException` | `BIZ_EXCEPTION` | false | 业务规则校验失败，消息直接展示给用户 |
| `BadRequestException` | `BAD_REQUEST` | true | 参数非法，生产环境只返回模糊提示 |
| `UnauthorizedException` | `UNAUTHORIZED` | false | Token 失效 / 登录过期 |
| `AccessDeniedException` | `ACCESS_FORBIDDEN` | false | 已登录但无权访问 |
| `ServerErrorException` | `SERVER_ERROR` | true | 系统内部错误，必须隐藏详情 |

### 3.3 分页模型

```java
// 请求端 DTO 实现 PageRequest
@Data
public class UserListQuery implements PageRequest {
    private String keyword;
    private Integer pageNum;     // 可选，默认 1
    private Integer pageSize;    // 可选，默认 10
}

// 服务端
int start = query.getStartIndex();   // (pageNum - 1) * pageSize
int end   = query.getEndIndex();

// 返回
return new PageResult<>(pageNum, pageSize, total, userList);
```

`PageResult` 字段：`pageNum`、`pageSize`、`totalPage`、`total`、`content`。

### 3.4 签名接口：`BaseSign`

```java
@Data
public class PaymentNotifyRequest implements BaseSign {
    private Long orderId;
    private BigDecimal amount;

    // BaseSign 强制要求
    private String sign;
    private Long nonceTimestamp;
}
```

`SignatureUtil`（位于 `ddf-common-core`）会基于 `BaseSign` 对 DTO 做 ASCII 排序 + HMAC-SHA256 签名，
服务端通过 `SignatureUtil.verify(dto, sign)` 验签，失败时抛 `BusinessException(SIGN_ERROR)`。

### 3.5 Redis Key 约束

```java
public enum UserRedisKeyEnum implements RedisKeyConstraint {

    USER_INFO("user:info:%s", Duration.ofHours(1), RedisKeyTypeEnum.STRING, UserInfo.class, null),
    USER_SESSION("user:session:%s", Duration.ofMinutes(30), RedisKeyTypeEnum.STRING, null, null);

    // getKey("1001") → "user:info:1001"
    // getShardingKey("1001") → 若配置了分片规则，追加 "_shard_N"
}
```

`RedisKeyConstraint` 强制约定：
- `template` 使用 `%s` 占位符
- `getKey(Object...)` 自动校验参数个数与占位符匹配，不匹配时抛 `ServerErrorException`
- `getShardingKey` 支持按分片规则追加后缀（如 `"_shard_0"`），用于多 Redis 实例路由

### 3.6 树形结构约束：`ITreeTagCollection`

```java
@Data
public class DeptDTO implements ITreeTagCollection<Long, DeptDTO> {
    private Long treeId;        // 部门 ID
    private Long treeParentId;  // 父部门 ID
    private Integer sort;       // 排序权重
    private List<DeptDTO> children = new ArrayList<>();
}
```

实现该接口后，在 `ddf-common-core` 中直接调用 `TreeConvertUtil.convert(flatList)` 即可得到完整树。

### 3.7 Jackson 定制

- **JSR-310**：`jackson-datatype-jsr310` 已引入，确保 `LocalDateTime` / `Duration` 序列化稳定
- **MsgPack**：`jackson-dataformat-msgpack` 支持二进制紧凑序列化，适合内部 RPC
- **`@JsonIgnoreProfile`**：注解在字段上，指定哪些 profile（如 `prod`）下不输出该字段，
  用于生产环境隐藏 `subMessage` 等调试信息

### 3.8 敏感数据脱敏

```java
@Data
public class UserDTO {
    @SensitiveField(SensitiveStrategy.PHONE)
    private String mobile;

    @SensitiveField(SensitiveStrategy.ID_CARD)
    private String idCard;
}
```

在序列化阶段自动将 `13800138000` → `138****8000`、`11010119900101****`。

---

## 4. 进阶用法 / 扩展点

### 4.1 自定义全局错误码

新建一个枚举实现 `BaseCallbackCode`，放在业务工程的 `enums` 包下：

```java
public enum AppErrorCode implements BaseCallbackCode {
    INVENTORY_SHORTAGE("INV_001", "库存不足");

    private final String code;
    private final String description;

    @Override public String getCode() { return code; }
    @Override public String getDescription() { return description; }
    @Override public String getBizMessage() { return description; }
}
```

> 错误码的 `code` 字符串应保持全局唯一。推荐格式：`{业务域}_{序号}`。

### 4.2 扩展 `BaseException`

如果默认异常体系不够用（例如需要 HTTP status 映射、国际化），可以继承 `BaseException`：

```java
public class PaymentException extends BaseException {
    public PaymentException(BaseCallbackCode code) { super(code); }
    @Override public BaseCallbackCode defaultCallback() { return PaymentErrorCode.DEFAULT; }
    @Override public boolean isMaskErrorDetails() { return true; }
}
```

### 4.3 自定义 Redis 分片规则

实现 `RedisShardingRule<S, M>` 接口，在 `RedisKeyConstraint` 中注册：

```java
public class UserIdModSharding implements RedisShardingRule<Long, String> {
    @Override public String getSharding(Long userId) { return "shard_" + (userId % 4); }
}
```

---

## 5. 与其他模块协作

| 模块 | 协作方式 |
| ----- | ----- |
| `ddf-common-core` | 上游依赖；`ResponseData`、`BaseException`、`BaseSign`、`RedisKeyConstraint`、`ITreeTagCollection` 的默认实现与工具类 |
| `ddf-common-mvc` | 全局异常处理器把 `BaseException` 映射为 `ResponseData`；`@JsonIgnoreProfile`  Jackson 模块在此注册 |
| `ddf-common-redis` | 所有 Redis Key 枚举必须实现 `RedisKeyConstraint`；`ApplicationNamedKeyGenerator` 读取 `spring.application.name` |
| `ddf-common-authentication` | 鉴权异常使用 `UnauthorizedException` / `AccessDeniedException` |
| `ddf-common-limit` | 限流拦截器返回 `ResponseData.failure(...)` |
| `ddf-common-data-mysql-starter` | `BaseDomain` 实体基类（在 core 中）配合 `PageRequest` / `PageResult` 完成分页链路 |

---

## 6. FAQ

**Q1：为什么 `ddf-common-api` 不直接包含 `BaseDomain`？**  
`BaseDomain` 引入了 `spring-data-commons` 的 `@CreatedDate` / `@LastModifiedDate`，对纯协议模块来说过重。
因此它放在 `ddf-common-core` 中，而 `api` 只保留不依赖 Spring Data 的 `PageRequest` / `PageResult`。

**Q2：`BaseCallbackCode` 的 `bizMessage` 与 `description` 有什么区别？**  
- `description`：开发/联调时使用，可包含技术细节、占位符原始模板
- `bizMessage`：最终给用户看的（toast / alert）。生产环境当 `isMaskErrorDetails() = true` 时，
  系统用 `bizMessage` 替换 `description`，防止堆栈和敏感信息泄露

**Q3：`ResponseData` 的 `subMessage` 为什么在生产环境消失了？**  
`subMessage` 字段标注了 `@JsonIgnoreProfile(profile = {"pro", "prod"})`，在 prod profile 下 Jackson
序列化时会自动忽略该字段。

**Q4：可以在非 Spring 项目使用 `ddf-common-api` 吗？**  
可以。唯一强依赖 Spring 的部分是 `spring-boot-starter-validation`（用于 DTO 上的 `@NotNull` 等注解），
如果你不需要校验，可以排除该依赖，仅使用 `ResponseData`、`BaseCallbackCode`、`PageResult` 等纯 POJO。

**Q5：错误码的 code 字符串有什么命名规范？**  
- 使用大写下划线格式：`ORDER_NOT_FOUND`、`INVENTORY_SHORTAGE`
- 全局唯一（跨服务也不应重复），方便日志检索和网关统一映射
- 不要在运行时拼接 code 字符串，所有 code 必须是枚举常量

---

## 7. 参考

- 源码：`model/common/response/ResponseData.java`、`model/common/request/PageRequest.java`
- 源码：`exception/BaseCallbackCode.java`、`exception/BaseErrorCallbackCode.java`、`exception/BaseException.java`
- 源码：`constraint/redis/RedisKeyConstraint.java`、`constraint/collect/ITreeTagCollection.java`
- 源码：`model/common/request/BaseSign.java`
- Jackson 文档：<https://github.com/FasterXML/jackson-docs/>
