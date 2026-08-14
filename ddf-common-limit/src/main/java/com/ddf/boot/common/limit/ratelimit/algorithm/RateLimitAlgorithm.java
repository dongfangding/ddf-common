package com.ddf.boot.common.limit.ratelimit.algorithm;

/**
 * 限流算法策略接口，接入方注册自定义算法 Bean 即可扩展。
 */
public interface RateLimitAlgorithm {

    /** 算法标识，与 @RateLimit 注解的 algorithm 字段对应（也是 bean name） */
    String getAlgorithm();

    /** 尝试获取许可，返回 false 表示被限流 */
    boolean tryAcquire(String key, int max, int rate);
}
