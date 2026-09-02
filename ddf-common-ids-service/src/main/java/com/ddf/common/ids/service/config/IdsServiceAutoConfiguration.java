package com.ddf.common.ids.service.config;

import com.ddf.common.ids.service.api.IdsApi;
import com.ddf.common.ids.service.api.impl.IdsApiImpl;
import com.ddf.common.ids.service.config.properties.IdsProperties;
import com.ddf.common.ids.service.service.IDGen;
import com.ddf.common.ids.service.service.IdGenRegistry;
import com.ddf.common.ids.service.service.SnowflakeService;
import com.ddf.common.ids.service.service.impl.segment.SegmentIDGenImpl;
import com.ddf.common.ids.service.service.impl.segment.dao.IDAllocDao;
import com.ddf.common.ids.service.service.impl.segment.dao.impl.IDAllocDaoImpl;
import com.ddf.common.ids.service.service.impl.snowflake.SnowflakeIDGenImpl;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
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
 * @since 2021/07/19 20:20
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
     * @param dataSource 数据source参数
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "segmentEnable", havingValue = "true")
    public IDAllocDao idAllocDao(DataSource dataSource) {
        return new IDAllocDaoImpl(dataSource);
    }

    /**
     * 号段模式id实现类
     *
     * @param idAllocDao 参数
     */
    @Bean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "segmentEnable", havingValue = "true")
    public IDGen segmentIDGen(IDAllocDao idAllocDao) {
        return new SegmentIDGenImpl(idAllocDao, idsProperties);
    }

    /**
     * ID 生成策略注册表，收集所有 IDGen 实现 Bean
     *
     * @param idGens 参数
     */
    @Bean
    public IdGenRegistry idGenRegistry(List<IDGen> idGens) {
        return new IdGenRegistry(idGens);
    }

    /**
     * 对外统一暴露的ids服务接口
     *
     * @param snowflakeService 参数
     * @param idGenRegistry 参数
     */
    @Bean
    public IdsApi idsApi(Optional<SnowflakeService> snowflakeService, IdGenRegistry idGenRegistry) {
        return new IdsApiImpl(idsProperties, snowflakeService.orElse(null), idGenRegistry);
    }

    /**
     * 雪花id实现类
     */
    @Bean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "snowflakeEnable",
            havingValue = "true")
    public IDGen snowflakeIDGen() {
        return new SnowflakeIDGenImpl(idsProperties);
    }

    /**
     * 包装的雪花id组件类
     */
    @Bean
    @ConditionalOnProperty(prefix = IdsProperties.IDS_PROPERTIES_PREFIX, value = "snowflakeEnable",
            havingValue = "true")
    public SnowflakeService snowflakeService() {
        return new SnowflakeService(snowflakeIDGen());
    }
}
