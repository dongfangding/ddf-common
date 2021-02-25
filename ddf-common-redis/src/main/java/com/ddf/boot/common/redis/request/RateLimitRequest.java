package com.ddf.boot.common.redis.request;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>针对限流请求的参数类</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/11 11:12
 */
@Data
@AllArgsConstructor
public class RateLimitRequest {

    /**
     * 限流key
     */
    private String key;

    /**
     * 令牌桶的最大大小
     */
    private Integer max;

    /**
     * 每秒钟恢复的令牌数量
     */
    private Integer rate;

    /**
     * 获取Builder， 主要是因为可能会对属性做一些判断修改，所以自己写了构建器，方便改写
     *
     * @return
     */
    public static RateLimitRequestBuilder builder() {
        return new RateLimitRequestBuilder();
    }


    public static class RateLimitRequestBuilder {

        /**
         * 前缀
         */
        public static final String KEY_PREFIX = "rate_limit:";

        /**
         * 限流key
         */
        private String key;

        /**
         * 令牌桶的最大大小
         */
        private Integer max;

        /**
         * 每秒钟恢复的令牌数量
         */
        private Integer rate;

        /**
         * 是否忽略使用前缀
         */
        private boolean ignorePrefix;


        public RateLimitRequestBuilder key(String key) {
            this.key = key;
            return this;
        }

        public RateLimitRequestBuilder max(Integer max) {
            this.max = max;
            return this;
        }

        public RateLimitRequestBuilder rate(Integer rate) {
            this.rate = rate;
            return this;
        }

        public RateLimitRequestBuilder ignorePrefix(boolean ignorePrefix) {
            this.ignorePrefix = ignorePrefix;
            return this;
        }

        public RateLimitRequest build() {
            String key = ignorePrefix ? this.key : KEY_PREFIX + this.key;
            return new RateLimitRequest(key, max, rate);
        }
    }

}
