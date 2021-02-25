package com.ddf.boot.common.limit.ratelimit.extra;


import com.ddf.boot.common.limit.ratelimit.config.RateLimitProperties;
import com.ddf.boot.common.limit.ratelimit.handler.RateLimitAspect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.RateLimitKeyGenerator;

/**
 * <p>预留的允许可以使用外部方式填充限流属性
 * 这个是全局属性，做默认
 * </p >
 *
 * @see RateLimitAspect
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/25 10:26
 */
public interface RateLimitPropertiesCollect {

    /**
     * 全局key生成规则,使用实现类bean的名称
     *
     * @see RateLimitKeyGenerator
     * @return
     */
    String getKeyGenerators();

    /**
     * 令牌桶最大数量
     *
     * @return
     */
    Integer getMax();

    /**
     * 令牌恢复速率，单位秒
     *
     * @return
     */
    Integer getRate();

    /**
     * 将扩展接口的值复制给属性类
     *
     * @param properties
     */
    default void copyToProperties(RateLimitProperties properties) {
        properties.setKeyGenerators(getKeyGenerators());
        properties.setMax(getMax());
        properties.setRate(getRate());
    }
}
