package com.ddf.boot.common.ext.oss.config;

import cn.hutool.core.collection.CollUtil;
import com.aliyun.oss.OSSClientBuilder;
import com.ddf.boot.common.core.util.SecureUtil;
import com.ddf.boot.common.ext.oss.helper.OssHelper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/12 13:25
 */
@Data
@Component
@ConfigurationProperties(prefix = "customs.ext.oss")
public class OssProperties implements InitializingBean {

    @Autowired
    private OssHelper ossHelper;

    /**
     * bucket配置
     */
    private List<OssProperty> properties;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollUtil.isNotEmpty(properties)) {
            for (OssProperty property : properties) {
                if (StringUtils.isAnyBlank(property.getBucketName(), property.getBucketEndpoint(), property.getAccessKeyId(), property.getAccessKeySecret())) {
                    throw new IllegalArgumentException("bucket属性不允许为空");
                }
                if (property.isSecret()) {
                    ossHelper.putOss(SecureUtil.decryptFromHexByAES(property.getBucketName()), new OSSClientBuilder().build(SecureUtil.decryptFromHexByAES(property.getBucketEndpoint()),
                            SecureUtil.decryptFromHexByAES(property.getAccessKeyId()), SecureUtil.decryptFromHexByAES(property.getAccessKeySecret())));
                } else {
                    ossHelper.putOss(property.getBucketName(), new OSSClientBuilder().build(property.getBucketEndpoint(),
                            property.getAccessKeyId(), property.getAccessKeySecret()));
                }
            }
        }
    }
}
