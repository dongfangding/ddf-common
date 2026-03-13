package com.ddf.common.vps.config;

import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.common.vps.helper.VpsClient;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class VpsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(VpsAutoConfiguration.class))
            .withUserConfiguration(VpsSupportConfiguration.class);

    @Test
    void shouldRegisterVpsClient() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(VpsClient.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class VpsSupportConfiguration {

        @Bean
        FastFileStorageClient fastFileStorageClient() {
            return Mockito.mock(FastFileStorageClient.class);
        }

        @Bean
        ThumbImageConfig thumbImageConfig() {
            return Mockito.mock(ThumbImageConfig.class);
        }

        @Bean
        FdfsWebServer fdfsWebServer() {
            return Mockito.mock(FdfsWebServer.class);
        }

        @Bean
        EnvironmentHelper environmentHelper() {
            return Mockito.mock(EnvironmentHelper.class);
        }
    }
}
