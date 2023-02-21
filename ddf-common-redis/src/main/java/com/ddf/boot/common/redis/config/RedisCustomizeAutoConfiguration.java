package com.ddf.boot.common.redis.config;

import cn.hutool.core.util.StrUtil;
import com.ddf.boot.common.redis.helper.GeoHelper;
import com.ddf.boot.common.redis.helper.RedisTemplateHelper;
import com.ddf.boot.common.redis.serializer.ObjectStringRedisSerializer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.ReflectionUtils;

/**
 * <p>redis自动配置类</p >
 *
 * redisson配置文档参考https://github.com/redisson/redisson/wiki/2.-%E9%85%8D%E7%BD%AE%E6%96%B9%E6%B3%95
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/12/11 11:06
 */
@Configuration
@EnableConfigurationProperties({RedissonCustomizeProperties.class, RedisProperties.class})
public class RedisCustomizeAutoConfiguration implements RedissonAutoConfigurationCustomizer {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    @Autowired
    private RedissonCustomizeProperties redissonCustomizeProperties;
    @Autowired
    private RedisProperties redisProperties;

    /**
     * 注册redis扩展方法类
     *
     * @param stringRedisTemplate
     * @return
     */
    @Bean
    public RedisTemplateHelper redisTemplateHelper(StringRedisTemplate stringRedisTemplate,
            RedissonClient redissonClient) {
        return new RedisTemplateHelper(stringRedisTemplate, redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.setStringSerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(name = "stringRedisTemplate")
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new ObjectStringRedisSerializer());
        template.setKeySerializer(new ObjectStringRedisSerializer());
        template.setHashKeySerializer(new ObjectStringRedisSerializer());
        template.setValueSerializer(new ObjectStringRedisSerializer());
        template.setHashValueSerializer(new ObjectStringRedisSerializer());
        return template;
    }

    /**
     * 注册geo帮助类
     *
     * @param redissonClient
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public GeoHelper geoHelper(RedissonClient redissonClient) {
        return new GeoHelper(redissonClient);
    }

    /**
     * Customize the RedissonClient configuration.
     *
     * @param configuration the {@link Config} to customize
     */
    @SneakyThrows
    @Override
    public void customize(Config configuration) {
        if (StrUtil.isNotBlank(redissonCustomizeProperties.getCodec())) {
            configuration.setCodec((Codec) Class.forName(redissonCustomizeProperties.getCodec()).newInstance());
        } else {
            configuration.setCodec(new JsonJacksonCodec());
        }
        // 因为原生redisson外部文件配置方式不支持环境变量注入，这里提供一种解决方案，如果不在外部配置文件中配置基础连接信息的话，就到
        // 原生的org.springframework.boot.autoconfigure.data.redis.RedisProperties对象里去拿连接信息
        if (redisProperties.getSentinel() != null) {
            String[] nodes = convert(redisProperties.getSentinel().getNodes());
            configuration.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword());
        } else if (redisProperties.getCluster() != null) {
            String[] nodes = convert(redisProperties.getCluster().getNodes());
            configuration.useClusterServers()
                    .addNodeAddress(nodes)
                    .setPassword(redisProperties.getPassword());
        } else {
            configuration.useSingleServer().setAddress(getPrefix() + redisProperties.getHost() + ":" + redisProperties.getPort())
                    .setDatabase(redisProperties.getDatabase())
                    .setPassword(redisProperties.getPassword());
        }
    }

    /**
     * 获取redis连接协议前缀
     *
     * @return
     */
    private String getPrefix() {
        String prefix = REDIS_PROTOCOL_PREFIX;
        Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
        if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
            prefix = REDISS_PROTOCOL_PREFIX;
        }
        return prefix;
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<String>(nodesObject.size());
        for (String node : nodesObject) {
            if (!node.startsWith(REDIS_PROTOCOL_PREFIX) && !node.startsWith(REDISS_PROTOCOL_PREFIX)) {
                nodes.add(REDIS_PROTOCOL_PREFIX + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }
}
