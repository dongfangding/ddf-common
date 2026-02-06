/**
 * Copyright (c) 2013-2021 Nikita Koksharov
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ddf.boot.common.redis.config;

import com.ddf.boot.common.redis.helper.RedisCommandHelper;
import com.ddf.boot.common.redis.serializer.ObjectStringRedisSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

/**
 * redis多数据源简单配置， 依赖于redisson的实现，且只特定于当前模块，因为依赖于一些配置
 *
 * @author snowball
 * @since 2023/10/6 20:21
 **/
@Configuration
@EnableConfigurationProperties(ExtraRedisProperties.class)
@ConditionalOnProperty(prefix = "customizer.infra.redis.extra-multi", value = "enable", havingValue = "true")
public class ExtraRedissonAutoConfiguration implements SmartInitializingSingleton, ApplicationContextAware {
    private static final String REDIS_PROTOCOL_PREFIX = "redis://";
    private static final String REDISS_PROTOCOL_PREFIX = "rediss://";

    @Autowired
    private ExtraRedisProperties extraRedisProperties;
    @Autowired
    private RedissonProperties redissonProperties;
    private ApplicationContext applicationContext;

    private void createRedissonClient() {
        final Map<String, ExtraRedisProperties.RedisProperties> map = extraRedisProperties.getMap();
        if (!extraRedisProperties.isEnable() || CollectionUtils.isEmpty(map)) {
            return;
        }
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        map.forEach((clientName, properties) -> {
            String beanName = clientName + "RedissonClient";
            genericApplicationContext.registerBean(beanName, RedissonClient.class, () -> {
                final Config config = createCommonConfig();
                // 只支持单机连接方式
                final SingleServerConfig singleServerConfig = config.useSingleServer();
                singleServerConfig.setAddress(
                        getPrefix(properties.isSsl()) + properties.getHost() + ":" + properties.getPort()).setDatabase(
                        properties.getDatabase()).setPassword(properties.getPassword()).setTimeout(
                        properties.getTimeout());
                if (StringUtils.isBlank(properties.getClientName())) {
                    singleServerConfig.setClientName(
                            applicationContext.getEnvironment().getProperty("spring.application.name"));
                }
                return Redisson.create(config);
            }, (bd) -> {
                bd.setDestroyMethodName("shutdown");
            });
        });

        map.forEach((clientName, properties) -> {
            String beanName = clientName + "RedissonConnectionFactory";
            String redissonClientBeanName = clientName + "RedissonClient";
            final RedissonClient redissonClient = applicationContext.getBean(
                    redissonClientBeanName, RedissonClient.class);
            genericApplicationContext.registerBean(
                    beanName, RedissonConnectionFactory.class, () -> new RedissonConnectionFactory(redissonClient));
        });

        map.forEach((clientName, properties) -> {
            String beanName = clientName + "StringRedisTemplate";
            String redissonConnectionFactoryBeanName = clientName + "RedissonConnectionFactory";
            final RedissonConnectionFactory redissonConnectionFactory = applicationContext.getBean(
                    redissonConnectionFactoryBeanName, RedissonConnectionFactory.class);
            genericApplicationContext.registerBean(beanName, StringRedisTemplate.class, () -> {
                StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
                stringRedisTemplate.setConnectionFactory(redissonConnectionFactory);
                stringRedisTemplate.setDefaultSerializer(new ObjectStringRedisSerializer());
                stringRedisTemplate.setKeySerializer(new ObjectStringRedisSerializer());
                stringRedisTemplate.setHashKeySerializer(new ObjectStringRedisSerializer());
                stringRedisTemplate.setValueSerializer(new ObjectStringRedisSerializer());
                stringRedisTemplate.setHashValueSerializer(new ObjectStringRedisSerializer());
                return stringRedisTemplate;
            });

            String beanName1 = clientName + "RedisTemplate";
            genericApplicationContext.registerBean(beanName1, RedisTemplate.class, () -> {
                RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
                redisTemplate.setConnectionFactory(redissonConnectionFactory);
                redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
                redisTemplate.setStringSerializer(new StringRedisSerializer());
                redisTemplate.setKeySerializer(new StringRedisSerializer());
                redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
                redisTemplate.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
                redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
                return redisTemplate;
            });


        });

        map.forEach((clientName, properties) -> {
            String redisCommandHelperBeanName = clientName + "RedisCommandHelper";
            String redisTemplateBeanName = clientName + "StringRedisTemplate";
            final StringRedisTemplate redisTemplateBean = applicationContext.getBean(
                    redisTemplateBeanName, StringRedisTemplate.class);
            genericApplicationContext.registerBean(redisCommandHelperBeanName, RedisCommandHelper.class,
                    () -> new RedisCommandHelper(redisTemplateBean)
            );
        });
    }

    /**
     * 获取redis连接协议前缀
     *
     * @return
     */
    private String getPrefix(boolean isSsl) {
        return isSsl ? REDISS_PROTOCOL_PREFIX : REDIS_PROTOCOL_PREFIX;
    }


    /**
     * 创建config
     *
     * @return
     */
    private Config createCommonConfig() {
        Config config = null;
        if (redissonProperties.getConfig() != null) {
            try {
                config = Config.fromYAML(redissonProperties.getConfig());
            } catch (IOException e) {
                try {
                    config = Config.fromJSON(redissonProperties.getConfig());
                } catch (IOException e1) {
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        } else if (redissonProperties.getFile() != null) {
            try {
                InputStream is = getConfigStream();
                config = Config.fromYAML(is);
            } catch (IOException e) {
                // trying next format
                try {
                    InputStream is = getConfigStream();
                    config = Config.fromJSON(is);
                } catch (IOException e1) {
                    throw new IllegalArgumentException("Can't parse config", e1);
                }
            }
        }
        return config;
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

    private InputStream getConfigStream() throws IOException {
        Resource resource = applicationContext.getResource(redissonProperties.getFile());
        InputStream is = resource.getInputStream();
        return is;
    }


    @Override
    public void afterSingletonsInstantiated() {
        createRedissonClient();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
