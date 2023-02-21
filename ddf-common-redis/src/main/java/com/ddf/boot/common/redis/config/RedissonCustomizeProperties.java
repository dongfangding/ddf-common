package com.ddf.boot.common.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p>redisson扩展配置属性
 * 主要原因是，redisson原生start配置文件支持json和file两种方式，对无法支持对系统环境变量的解析。
 * 所以这里，对一些可能用到的额外属性做剥离，核心基础属性还是使用RedisProperties的
 * 参考org.redisson.spring.starter.RedissonAutoConfiguration
 * </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/04/25 10:54
 */
@ConfigurationProperties(prefix = "spring.redis.redisson")
@Data
public class RedissonCustomizeProperties {

    private String codec;
}
