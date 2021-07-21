package com.ddf.common.ids.service.config;

import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.api.impl.IdsApiImpl;
import com.ddf.common.ids.service.config.properties.IdsProperties;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.SnowflakeService;
import com.ddf.common.ids.service.service.impl.segment.SegmentIDGenImpl;
import com.ddf.common.ids.service.service.impl.segment.dao.IDAllocDao;
import com.ddf.common.ids.service.service.impl.segment.dao.impl.IDAllocDaoImpl;
import com.ddf.common.ids.service.service.impl.snowflake.SnowflakeIDGenImpl;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@EnableConfigurationProperties(value = {IdsProperties.class})
public class IdsServiceAutoConfiguration {

    /**
     * 雪花id配置属性
     */
    private final IdsProperties idsProperties;

    public IdsServiceAutoConfiguration(IdsProperties idsProperties) {
        this.idsProperties = idsProperties;
    }

    /**
     * 将数据源注入到查询Dao中，支持外部重新注册Bean
     *
     * @param dataSource
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "segmentEnable", havingValue = "true")
    public IDAllocDao idAllocDao(@Autowired DataSource dataSource) {
        return new IDAllocDaoImpl(dataSource);
    }

    /**
     * 号段模式id实现类
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "segmentEnable", havingValue = "true")
    public IDGen segmentIDGen(@Autowired IDAllocDao idAllocDao) {
        return  new SegmentIDGenImpl(idAllocDao, idsProperties);
    }

    /**
     * 对外统一暴露的ids服务接口
     *
     * @return
     */
    @Bean
    public IdsApi idsApi(@Autowired(required = false) IDGen segmentIDGen) {
        if (!idsProperties.isSegmentEnable()) {
            return new IdsApiImpl(idsProperties, snowflakeService(), null);
        } else {
            return new IdsApiImpl(idsProperties, snowflakeService(), segmentIDGen);
        }
    }

    /**
     * 雪花id实现类
     *
     * @return
     */
    @Bean
    public IDGen snowflakeIDGen() {
        return new SnowflakeIDGenImpl(idsProperties);
    }

    /**
     * 包装的雪花id组件类
     *
     * @return
     */
    @Bean
    public SnowflakeService snowflakeService() {
        return new SnowflakeService(snowflakeIDGen());
    }
}
