package com.ddf.boot.common.s3;

import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.helper.S3Helper;
import com.ddf.boot.common.s3.service.S3Service;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * S3 兼容存储自动配置类.
 *
 * <p>支持 MinIO、AWS S3、阿里云 OSS 等 S3 兼容存储服务.</p>
 * <p>当配置文件中 <code>customizer.infra.s3.enable=true</code> 时，此自动配置生效.</p>
 *
 * @author snowball
 */
@AutoConfiguration
@ComponentScan("com.ddf.boot.common.s3")
@Import(S3Properties.class)
@EnableConfigurationProperties(S3Properties.class)
@ConditionalOnProperty(value = "customizer.infra.s3.enable", havingValue = "true", matchIfMissing = true)
public class S3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(S3Api.class)
    public S3Api s3Api(S3Properties s3Properties) {
        return new S3Service(s3Properties);
    }

    @Bean
    @ConditionalOnMissingBean(S3Helper.class)
    public S3Helper s3Helper(S3Api s3Api, S3Properties s3Properties) {
        return new S3Helper(s3Api, s3Properties);
    }

}
