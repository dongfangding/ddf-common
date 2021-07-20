package com.ddf.common.ids.service.config;

import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.api.impl.IdsApiImpl;
import com.ddf.common.ids.service.config.properties.SnowflakeProperties;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.SnowflakeService;
import com.ddf.common.ids.service.service.impl.segment.SegmentIDGenImpl;
import com.ddf.common.ids.service.service.impl.snowflake.SnowflakeIDGenImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>ids自动配置类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/07/19 20:20
 */
@Configuration
@EnableConfigurationProperties(value = {SnowflakeProperties.class})
public class IdsServiceAutoConfiguration {

    /**
     * 雪花id配置属性
     */
    private final SnowflakeProperties snowflakeProperties;

    public IdsServiceAutoConfiguration(SnowflakeProperties snowflakeProperties) {
        this.snowflakeProperties = snowflakeProperties;
    }

    /**
     * 对外统一暴露的ids服务接口
     *
     * @return
     */
    @Bean
    public IdsApi idsApi() {
        return new IdsApiImpl(snowflakeService(), snowflakeIDGen());
    }

    /**
     * 雪花id实现类
     *
     * @return
     */
    @Bean
    public IDGen snowflakeIDGen() {
        return new SnowflakeIDGenImpl(snowflakeProperties.getZkAddress(), snowflakeProperties.getZkPort());
    }

    /**
     * 包装的雪花id实现
     *
     * @return
     */
    @Bean
    public SnowflakeService snowflakeService() {
        return new SnowflakeService(snowflakeIDGen());
    }

    /**
     * 号段模式id实现类
     *
     * @return
     */
    @Bean
    public IDGen segmentIDGen() {
        return new SegmentIDGenImpl();
    }
}
