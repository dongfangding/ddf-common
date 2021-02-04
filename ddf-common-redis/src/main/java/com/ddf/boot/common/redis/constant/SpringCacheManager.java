package com.ddf.boot.common.redis.constant;

/**
 * <p>spring redis cache已默认加载前缀 ${applicationName}</p >
 * 若此处使用redis cache manager 此处无需声明的key的前缀
 *
 * @author Mitchell
 * @version 1.0
 * @date 2021/1/14 16:36
 */
public interface SpringCacheManager {

    /**
     * manager bean name
     */
    interface CacheManagerBeanName {

        /**
         * redis缓存，默认使用
         */
        String PRIMARY = "redisCacheManager";

        /**
         * 本地缓存，需声明指定
         */
        String LOCAL = "localCacheManager";

    }

    /**
     * cache name
     */
    interface CacheName {
        String CACHE_NAME_DEMO = "cache_name_demo";
    }

    /**
     * 生成业务信息的key规则
     *
     * @param property1 属性1
     * @param property2 属性2
     * @return
     */
    static String genCacheKeyDemo(String property1, String property2) {
        return ApplicationNamedKeyGenerator.genNormalKey(CacheName.CACHE_NAME_DEMO, property1, property2);
    }
}
