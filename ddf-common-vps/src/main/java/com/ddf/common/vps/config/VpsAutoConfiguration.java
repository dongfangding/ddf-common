package com.ddf.common.vps.config;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.common.vps.helper.VpsClient;
import com.github.tobato.fastdfs.FdfsClientConfig;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.RegistrationPolicy;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2021/11/29 17:10
 */
@AutoConfiguration
@EnableConfigurationProperties(VpsProperties.class)
@Import(FdfsClientConfig.class)
// 解决jmx重复注册bean的问题
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class VpsAutoConfiguration {

    @Bean
    public VpsClient vpsClient(FastFileStorageClient fastFileStorageClient, ThumbImageConfig thumbImageConfig,
            VpsProperties vpsProperties, FdfsWebServer fdfsWebServer, EnvironmentHelper environmentHelper) {
        return new VpsClient(fastFileStorageClient, thumbImageConfig, vpsProperties, fdfsWebServer, environmentHelper);
    }
}
