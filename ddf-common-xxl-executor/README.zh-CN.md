# ddf-common-xxl-executor

> XXL-JOB 执行器自动配置模块。简化分布式定时任务集成，提供执行器自动注册、任务定义注解和分片任务支持，
> 适用于需要集中管理和监控定时任务的分布式系统。

[English](./README.md) · 简体中文

---

## 1. 适用场景

`ddf-common-xxl-executor` 解决的是 **"分布式环境下定时任务需要集中调度、避免单点执行"** 问题。

| 场景 | 典型问题 | 模块提供的能力 |
| --- | --- | --- |
| 定时数据清理 | 单节点 Quartz 任务在集群中重复执行 | XXL-JOB 中心调度，任务只分配到一台执行器 |
| 批量订单处理 | 任务量大，单机执行耗时过长 | 分片广播策略，多台机器并行处理不同数据分片 |
| 定时报表生成 | 任务执行失败无感知，无法重试 | Admin 控制台可视化监控、失败告警、手动重试 |
| 弹性扩容 | 业务高峰需临时增加执行节点 | 新节点自动注册到调度中心，立即参与任务分配 |

---

## 2. 依赖引入

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-xxl-executor</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> 使用本模块前需独立部署 XXL-JOB Admin 调度中心。

---

## 3. 最小化配置

```yaml
xxl:
  job:
    admin-addresses: http://localhost:8080/xxl-job-admin
    executor:
      app-name: ddf-executor
      ip:                       # 自动获取本机 IP
      port: 9999                # 执行器通信端口
      log-path: /data/logs/xxl-job
      log-retention-days: 30
```

---

## 4. 核心 API

### 4.1 定义简单任务

```java
@Component
public class DemoTask {

    @XxlJob("demoTask")
    public ReturnT<String> demoTask(String param) {
        // 任务逻辑
        XxlJobHelper.log("任务执行参数: {}", param);
        return ReturnT.SUCCESS;
    }
}
```

### 4.2 定义 Cron 任务

```java
@Component
public class ScheduledTask {

    /**
     * 每天凌晨执行
     */
    @XxlJob(value = "dailyReportTask", cron = "0 0 0 * * ?")
    public ReturnT<String> dailyReport(String param) {
        reportService.generateDailyReport();
        return ReturnT.SUCCESS;
    }
}
```

### 4.3 分片任务

```java
@Component
public class ShardingTask {

    @XxlJob("dataSyncTask")
    public ReturnT<String> dataSync(String param) {
        int shardIndex = XxlJobHelper.getShardIndex();  // 当前分片序号
        int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数

        // 按分片查询数据：ID % shardTotal == shardIndex
        List<Order> orders = orderService.listByShard(shardIndex, shardTotal);
        for (Order order : orders) {
            syncService.sync(order);
        }

        return ReturnT.SUCCESS;
    }
}
```

### 4.4 任务参数与状态控制

```java
@XxlJob("paramTask")
public ReturnT<String> paramTask(String param) {
    String jobParam = XxlJobHelper.getJobParam();  // 获取调度参数

    try {
        process(jobParam);
        XxlJobHelper.handleSuccess("处理完成");
        return ReturnT.SUCCESS;
    } catch (Exception e) {
        XxlJobHelper.handleFail("处理失败: " + e.getMessage());
        return ReturnT.FAIL;
    }
}
```

---

## 5. 进阶用法 / 扩展点

### 5.1 路由策略

在 Admin 控制台为任务选择执行策略：

| 策略 | 说明 | 适用场景 |
| --- | --- | --- |
| `FIRST` | 固定第一台执行器 | 测试环境 |
| `ROUND` | 轮询 | 负载均衡 |
| `RANDOM` | 随机 | 简单分散 |
| `CONSISTENT_HASH` | 一致性 Hash | 相同参数固定到同一节点 |
| `FAILOVER` | 故障转移 | 高可用要求 |
| `BUSYOVER` | 忙碌转移 | 避开繁忙节点 |
| `SHARDING_BROADCAST` | 分片广播 | 大数据量并行处理 |

### 5.2 执行器端口规划

同一台机器部署多个执行器时，端口需错开：

```yaml
# 服务 A
xxl:
  job:
    executor:
      app-name: service-a-executor
      port: 9999

# 服务 B
xxl:
  job:
    executor:
      app-name: service-b-executor
      port: 9998
```

### 5.3 任务幂等设计

```java
@XxlJob("idempotentTask")
public ReturnT<String> idempotentTask(String param) {
    String uniqueKey = "xxl:job:" + XxlJobHelper.getJobId() + ":" + param;

    // 分布式锁保证幂等
    if (!redisTemplate.opsForValue().setIfAbsent(uniqueKey, "1", Duration.ofHours(1))) {
        XxlJobHelper.log("任务已执行，跳过");
        return ReturnT.SUCCESS;
    }

    // 执行业务逻辑
    doBusiness(param);
    return ReturnT.SUCCESS;
}
```

---

## 6. 与其他模块协作

| 模块 | 协作方式 |
| --- | --- |
| `ddf-common-redis` | 结合 Redis 分布式锁实现任务幂等 |
| `ddf-common-core` | 工具类、日志、异常处理支撑 |
| `ddf-common-data-mysql-starter` | 任务数据持久化到 MySQL |

---

## 7. FAQ

**Q1：与 Spring `@Scheduled` 有什么区别？**
Spring `@Scheduled` 是单机定时任务，集群中会重复执行。XXL-JOB 是分布式调度中心，任务由 Admin 统一分配，天然避免重复执行，且支持分片、失败重试、监控告警。

**Q2：执行器端口冲突怎么办？**
每个服务的执行器需配置独立端口。如端口已被占用，启动会报错，更换为空闲端口即可。

**Q3：任务执行日志在哪里查看？**
执行日志写入 `log-path` 配置的目录，同时可在 XXL-JOB Admin 控制台实时查看和下载。

**Q4：分片任务的数据倾斜怎么处理？**
分片键选择应尽量均匀（如用户 ID 取模）。对于热点数据，可在业务层做二次分片或单独补偿处理。

---

## 8. 参考

- 源码：`XxlJobExecutorConfiguration`
- XXL-JOB 官方文档：https://www.xuxueli.com/xxl-job/
