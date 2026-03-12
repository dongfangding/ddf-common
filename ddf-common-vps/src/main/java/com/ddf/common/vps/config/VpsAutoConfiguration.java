package com.ddf.common.vps.config;

import com.github.tobato.fastdfs.FdfsClientConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan("com.ddf.common.vps")
public class VpsAutoConfiguration {
}
