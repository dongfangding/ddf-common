package com.ddf.boot.common.redis.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 基于漏桶算法的分布式限流参数$
 *
 * @author dongfang.ding
 * @date 2020/12/18 0018 23:03
 */
@Data
@AllArgsConstructor
public class LeakyBucketRateLimitRequest {

    /**
     * 锁的key
     */
    private String key;

    /**
     * 速率
     */
    private long rate;

    /**
     * 恢复速率的单位时间，单位为秒
     */
    private long rateIntervalSeconds;

    /**
     * 本次请求要获取的令牌数量
     */
    private long permits;


    /**
     * 获取Builder， 主要是因为可能会对属性做一些判断修改，所以自己写了构建器，方便改写
     *
     * @return
     */
    public static LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder builder() {
        return new LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder();
    }

    public static class LeakyBucketRateLimitRequestBuilder {

        /**
         * 前缀
         */
        public static final String KEY_PREFIX = "rate_limit:";

        /**
         * 锁的key
         */
        private String key;

        /**
         * 速率
         */
        private long rate;

        /**
         * 恢复速率的单位时间，单位为秒, 默认为1秒
         */
        private long rateIntervalSeconds = 1;

        /**
         * 本次请求要获取的令牌数量, 默认为1
         */
        private long permits = 1;

        /**
         * 是否忽略使用前缀
         */
        private boolean ignorePrefix;


        public LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder key(String key) {
            this.key = key;
            return this;
        }


        public LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder rate(long rate) {
            this.rate = rate;
            return this;
        }

        public LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder rateIntervalSeconds(
                long rateIntervalSeconds) {
            this.rateIntervalSeconds = rateIntervalSeconds;
            return this;
        }

        public LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder permits(long permits) {
            this.permits = permits;
            return this;
        }

        public LeakyBucketRateLimitRequest.LeakyBucketRateLimitRequestBuilder ignorePrefix(boolean ignorePrefix) {
            this.ignorePrefix = ignorePrefix;
            return this;
        }

        public LeakyBucketRateLimitRequest build() {
            String key = ignorePrefix ? this.key : KEY_PREFIX + this.key;
            return new LeakyBucketRateLimitRequest(key, rate, rateIntervalSeconds, permits);
        }
    }
}
