package com.ddf.boot.common.ext.oss.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/11/14 16:22
 */
@Slf4j
@Configurable
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(OssProperties.class)
public class OssBeanAutoConfiguration {

    /**
     * 默认注入的Oss Bean的name
     */
    public static final String DEFAULT_OSS_CLIENT_NAME = "defaultOssClient";

    /**
     * 默认注入的IAcsClient Bean的name
     */
    public static final String DEFAULT_ACS_CLIENT_NAME = "defaultAcsClient";

    @Bean(name = DEFAULT_OSS_CLIENT_NAME, destroyMethod = "shutdown")
    @Primary
    public OSS defaultOssClient(OssProperties ossProperties) {
        return new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    @Bean(name = DEFAULT_ACS_CLIENT_NAME, destroyMethod = "shutdown")
    @Primary
    public IAcsClient defaultAcsClient(OssProperties ossProperties) {
        DefaultProfile.addEndpoint("", "Sts", ossProperties.getStsEndpoint());
        IClientProfile profile = DefaultProfile.getProfile("", ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        // 用profile构造client
        return new DefaultAcsClient(profile);
    }
}
