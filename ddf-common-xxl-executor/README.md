# ddf-common-xxl-executor

> XXL-JOB executor auto-configuration module. Simplifies distributed scheduled task integration with automatic
> executor registration, task definition annotations, and sharded task support for distributed systems needing
> centralized task management and monitoring.

English · [简体中文](./README.zh-CN.md)

---

## 1. When to Use This Module

`ddf-common-xxl-executor` solves the **"in distributed environments, scheduled tasks need centralized scheduling to avoid single-point execution"** problem.

| Scenario                | Typical Problem                                               | What the Module Provides                                                                          |
|-------------------------|---------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| Timed data cleanup      | Single-node Quartz tasks duplicate execution in clusters      | XXL-JOB central scheduling assigns tasks to only one executor                                     |
| Batch order processing  | Task volume is large; single-machine execution takes too long | Sharding broadcast strategy; multiple machines process different data shards in parallel          |
| Timed report generation | Task failures are invisible and cannot be retried             | Admin console visual monitoring, failure alerts, manual retry                                     |
| Elastic scaling         | Need temporary execution nodes during business peaks          | New nodes auto-register to the scheduling center and immediately participate in task distribution |

---

## 2. Maven Dependency

```xml
<dependency>
    <groupId>io.github.dongfangding</groupId>
    <artifactId>ddf-common-xxl-executor</artifactId>
    <version>${ddf-common.version}</version>
</dependency>
```

> Before using this module, deploy the XXL-JOB Admin scheduling center independently.

---

## 3. Minimum Configuration

```yaml
xxl:
  job:
    admin-addresses: http://localhost:8080/xxl-job-admin
    executor:
      app-name: ddf-executor
      ip:                       # Auto-detect local IP
      port: 9999                # Executor communication port
      log-path: /data/logs/xxl-job
      log-retention-days: 30
```

---

## 4. Core API

### 4.1 Define Simple Task

```java
@Component
public class DemoTask {

    @XxlJob("demoTask")
    public ReturnT<String> demoTask(String param) {
        // Task logic
        XxlJobHelper.log("Task execution param: {}", param);
        return ReturnT.SUCCESS;
    }
}
```

### 4.2 Define Cron Task

```java
@Component
public class ScheduledTask {

    /**
     * Execute daily at midnight
     */
    @XxlJob(value = "dailyReportTask", cron = "0 0 0 * * ?")
    public ReturnT<String> dailyReport(String param) {
        reportService.generateDailyReport();
        return ReturnT.SUCCESS;
    }
}
```

### 4.3 Sharded Task

```java
@Component
public class ShardingTask {

    @XxlJob("dataSyncTask")
    public ReturnT<String> dataSync(String param) {
        int shardIndex = XxlJobHelper.getShardIndex();  // Current shard index
        int shardTotal = XxlJobHelper.getShardTotal();  // Total shard count

        // Query data by shard: ID % shardTotal == shardIndex
        List<Order> orders = orderService.listByShard(shardIndex, shardTotal);
        for (Order order : orders) {
            syncService.sync(order);
        }

        return ReturnT.SUCCESS;
    }
}
```

### 4.4 Task Parameters and Status Control

```java
@XxlJob("paramTask")
public ReturnT<String> paramTask(String param) {
    String jobParam = XxlJobHelper.getJobParam();  // Get scheduling parameter

    try {
        process(jobParam);
        XxlJobHelper.handleSuccess("Processing completed");
        return ReturnT.SUCCESS;
    } catch (Exception e) {
        XxlJobHelper.handleFail("Processing failed: " + e.getMessage());
        return ReturnT.FAIL;
    }
}
```

---

## 5. Advanced Usage / Extension Points

### 5.1 Routing Strategies

Select execution strategy for tasks in the Admin console:

| Strategy             | Description          | Suitable For                              |
|----------------------|----------------------|-------------------------------------------|
| `FIRST`              | Fixed first executor | Test environments                         |
| `ROUND`              | Round-robin          | Load balancing                            |
| `RANDOM`             | Random               | Simple distribution                       |
| `CONSISTENT_HASH`    | Consistent hash      | Same parameter always routes to same node |
| `FAILOVER`           | Failover             | High availability requirements            |
| `BUSYOVER`           | Busy transfer        | Avoid busy nodes                          |
| `SHARDING_BROADCAST` | Sharding broadcast   | Large data parallel processing            |

### 5.2 Executor Port Planning

When deploying multiple executors on the same machine, ports must not conflict:

```yaml
# Service A
xxl:
  job:
    executor:
      app-name: service-a-executor
      port: 9999

# Service B
xxl:
  job:
    executor:
      app-name: service-b-executor
      port: 9998
```

### 5.3 Task Idempotency Design

```java
@XxlJob("idempotentTask")
public ReturnT<String> idempotentTask(String param) {
    String uniqueKey = "xxl:job:" + XxlJobHelper.getJobId() + ":" + param;

    // Distributed lock guarantees idempotency
    if (!redisTemplate.opsForValue().setIfAbsent(uniqueKey, "1", Duration.ofHours(1))) {
        XxlJobHelper.log("Task already executed, skipping");
        return ReturnT.SUCCESS;
    }

    // Execute business logic
    doBusiness(param);
    return ReturnT.SUCCESS;
}
```

---

## 6. Interplay with Other Modules

| Module                          | How They Cooperate                                       |
|---------------------------------|----------------------------------------------------------|
| `ddf-common-redis`              | Combine with Redis distributed lock for task idempotency |
| `ddf-common-core`               | Utility, logging, and exception handling support         |
| `ddf-common-data-mysql-starter` | Task data persistence to MySQL                           |

---

## 7. FAQ

**Q1: What's the difference from Spring `@Scheduled`?**
Spring `@Scheduled` is a single-machine scheduled task that duplicates execution in clusters. XXL-JOB is a distributed scheduling center where tasks are centrally assigned by Admin, naturally avoiding duplicate execution, and supporting sharding, failure retry, and monitoring alerts.

**Q2: What if the executor port conflicts?**
Each service's executor must be configured with an independent port. If the port is already in use, startup will fail; change to an available port.

**Q3: Where to view task execution logs?**
Execution logs are written to the directory configured by `log-path`, and can also be viewed and downloaded in real time from the XXL-JOB Admin console.

**Q4: How to handle data skew in sharded tasks?**
Sharding keys should be as evenly distributed as possible (e.g., user ID modulo). For hot data, secondary sharding or separate compensation handling can be done in the business layer.

---

## 8. References

- Source: `XxlJobExecutorConfiguration`
- XXL-JOB official docs: https://www.xuxueli.com/xxl-job/
