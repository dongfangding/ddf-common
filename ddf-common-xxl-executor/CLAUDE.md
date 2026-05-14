# CLAUDE.md

## 模块简介

提供 XXL-JOB 执行器自动配置，简化定时任务集成。

## 核心类

| 类路径                                                      | 功能    |
|----------------------------------------------------------|-------|
| `com.ddf.boot.xxljob.config.XxlJobExecutorConfiguration` | 执行器配置 |

## 使用说明

### 1. 配置

```yaml
xxl:
  job:
    admin-addresses: http://localhost:8080/xxl-job-admin  # Admin 地址
    executor:
      app-name: ddf-executor       # 执行器名称
      ip:                           # 执行器 IP（自动获取）
      port: 9999                    # 执行器端口
      log-path: /data/logs/xxl-job  # 日志路径
      log-retention-days: 30        # 日志保留天数
```

### 2. 定义任务

```java
@Component
public class DemoTask {

    /**
     * 简单任务
     */
    @XxlJob("demoTask")
    public ReturnT<String> demoTask(String param) {
        // 任务逻辑
        return ReturnT.SUCCESS;
    }

    /**
     * 带 Cron 表达式的任务
     */
    @XxlJob(value = "cronTask", cron = "0 0 0 * * ?")
    public ReturnT<String> cronTask(String param) {
        // 每天零点执行
        return ReturnT.SUCCESS;
    }

    /**
     * 分片任务
     */
    @XxlJob("shardingTask")
    public ReturnT<String> shardingTask(String param) {
        // 获取分片参数
        int index = XxlJobHelper.getShardIndex();
        int total = XxlJobHelper.getShardTotal();

        // 分片处理逻辑
        return ReturnT.SUCCESS;
    }
}
```

### 3. 任务参数

| 方法                                                               | 说明     |
|------------------------------------------------------------------|--------|
| `XxlJobHelper.getJobParam()`                                     | 获取任务参数 |
| `XxlJobHelper.handleFail(String msg)`                            | 标记任务失败 |
| `XxlJobHelper.handleSuccess(String msg)`                         | 标记任务成功 |
| `XxlJobHelper.log(String msg)`                                   | 记录日志   |
| `XxlJobHelper.setRouteStrategy(ExecutorRouteStrategyEnum.ROUND)` | 设置路由策略 |

## 路由策略

| 策略                      | 说明       |
|-------------------------|----------|
| `FIRST`                 | 第一个      |
| `LAST`                  | 最后一个     |
| `ROUND`                 | 轮询       |
| `RANDOM`                | 随机       |
| `CONSISTENT_HASH`       | 一致性 Hash |
| `LEAST_FREQUENTLY_USED` | 最不经常使用   |
| `LEAST_RECENTLY_USED`   | 最近最久未使用  |
| `FAILOVER`              | 故障转移     |
| `BUSYOVER`              | 忙碌转移     |
| `SHARDING_BROADCAST`    | 分片广播     |

## 注意事项

1. **端口冲突**：确保执行器端口不与其他服务冲突
2. **日志路径**：配置日志路径便于问题排查
3. **任务幂等**：建议任务实现幂等，防止重复执行
4. **执行时长**：长时间任务建议使用异步处理
