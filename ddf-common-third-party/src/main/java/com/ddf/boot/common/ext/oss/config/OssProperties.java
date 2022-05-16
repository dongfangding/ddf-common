package com.ddf.boot.common.ext.oss.config;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.boot.common.core.util.SecureUtil;
import com.google.common.base.Preconditions;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:25
 */
@Data
@Component
@ConfigurationProperties(prefix = "customs.ext.oss")
public class OssProperties implements InitializingBean {

    /**
     * 是否使用oss， 使用的话必须为true, 如果不使用则会跳过参数处理过程
     */
    private boolean enable;

    /**
     * 是否加密， 如果加密，则使用系统自带的AES算法进行解密
     */
    private boolean secret;

    /**
     * 构建OSS对象的属性
     *
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String accessKeyId;

    /**
     * 构建OSS对象的属性
     *
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String accessKeySecret;

    /**
     * Endpoint（地域节点） SDK中需要的，和bucket无关，和OSS所在地域有关
     */
    private String endpoint;

    /**
     * bucket配置
     */
    private List<BucketProperty> buckets;

    /**
     * https://help.aliyun.com/document_detail/66053.html?spm=a2c4g.11186623.2.27.22303b4957W2X1#reference-sdg-3pv-xdb
     * sts的接入地址
     */
    private String stsEndpoint;

    /**
     * 使用sts临时授权访问oss
     * https://help.aliyun.com/document_detail/100624.html?spm=a2c4g.11186623.2.10.89fb1b92RKtxmw#concept-xzh-nzk-2gb
     */
    private String roleArn;

    /**
     * https://help.aliyun.com/document_detail/100624.html?spm=a2c4g.11186623.2.10.89fb1b92RKtxmw#concept-xzh-nzk-2gb
     */
    private String roleSessionName;

    /**
     * https://help.aliyun.com/document_detail/100624.html?spm=a2c4g.11186623.2.10.89fb1b92RKtxmw#concept-xzh-nzk-2gb
     */
    private String policy;

    /**
     * 授权临时凭证有效期
     * https://help.aliyun.com/document_detail/100624.html?spm=a2c4g.11186623.2.10.89fb1b92RKtxmw#concept-xzh-nzk-2gb
     */
    private long durationSeconds = 1800L;

    /**
     * oss前缀地址的cdn加速地址, 可选，没有的话还是使用bucket自己的域名
     */
    private String cdnAddr;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (!enable) {
            return;
        }
        Preconditions.checkArgument(!StringUtils.isAnyBlank(this.getAccessKeyId(), this.getAccessKeySecret()),
                "请检查oss配置属性"
        );
        Preconditions.checkArgument(CollUtil.isNotEmpty(this.getBuckets()), "请检查bucket列表配置");
        Preconditions.checkArgument(StringUtils.isNotBlank(stsEndpoint), "sts的接入地址不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(roleArn), "roleArn不能为空");
        final List<BucketProperty> buckets = this.getBuckets();
        boolean includePrimary = buckets.size() == 1 || buckets.stream().filter(BucketProperty::isPrimary).count() == 1;
        PreconditionUtil.checkArgument(includePrimary, "请且只能配置一个主存储桶， 参考属性primary");
        boolean isDecrypt = false;
        for (BucketProperty property : this.getBuckets()) {
            Preconditions.checkArgument(StringUtils.isNotBlank(property.getBucketName()), "请检查bucket配置");
            if (this.isSecret()) {
                // 在运行时解密存储
                if (!isDecrypt) {
                    this.setAccessKeyId(SecureUtil.decryptFromHexByAES(this.getAccessKeyId()));
                    this.setAccessKeySecret(SecureUtil.decryptFromHexByAES(this.getAccessKeySecret()));
                    isDecrypt = true;
                }
            }
        }
    }

}
