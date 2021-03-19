package com.ddf.boot.common.limit.ratelimit.config;

import com.ddf.boot.common.limit.ratelimit.extra.RateLimitPropertiesCollect;
import com.ddf.boot.common.limit.ratelimit.keygenerator.GlobalRateLimitKeyGenerator;
import java.util.Objects;
import jodd.util.StringUtil;
import lombok.Data;

/**
 * <p>限流全局参数</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2021/02/24 13:42
 */
@Data
public class RateLimitProperties {

    public static final String BEAN_NAME = "rateLimitProperties";

    /**
     * 限流key生成规则bean_name
     */
    private String keyGenerators = GlobalRateLimitKeyGenerator.BEAN_NAME;

    /**
     * 作为不控制限流的特殊值
     */
    public static final Integer NOT_CONTROL = 0;

    /**
     * 是否是spring-cloud环境并使用@RequestScope刷新特性。
     * 由于当前模块的依赖问题， 在这个模块中不准备依赖cloud的依赖。如果想要使用动态刷新特性，可以实现接口{@link RateLimitPropertiesCollect}来返回实时刷新值
     *
     * @see RateLimitPropertiesCollect
     * @see RateLimitRegistrar
     * @return
     */
    private boolean cloudRefresh;

    /**
     * 令牌桶最大数量
     */
    private Integer max = NOT_CONTROL;

    /**
     * 令牌恢复速率，单位秒
     */
    private Integer rate = NOT_CONTROL;

    /**
     * 执行参数检查
     *
     */
    public void check() {
        if (StringUtil.isBlank(keyGenerators)) {
            throw new IllegalArgumentException("限流key组件生成器参数异常");
        }
        if (Objects.isNull(max) || max <= 0) {
            throw new IllegalArgumentException("令牌桶最大数量参数异常");
        }
        if (Objects.isNull(rate) || rate <= 0) {
            throw new IllegalArgumentException("令牌恢复速率参数异常");
        }
    }
}
