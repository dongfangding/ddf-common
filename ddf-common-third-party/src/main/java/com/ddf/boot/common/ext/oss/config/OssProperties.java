package com.ddf.boot.common.ext.oss.config;

import cn.hutool.core.collection.CollUtil;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.ddf.boot.common.core.util.SecureUtil;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

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
     * 是否加密， 如果加密，则使用系统自带的AES算法进行解密
     */
    private boolean secret;

    /**
     * 构建OSS对象的属性
     * @see com.aliyun.oss.OSSClientBuilder#build(java.lang.String, java.lang.String, java.lang.String)
     */
    private String accessKeyId;

    /**
     * 构建OSS对象的属性
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


    @Override
    public void afterPropertiesSet() throws Exception {
        Preconditions.checkArgument(!StringUtils.isAnyBlank(this.getAccessKeyId(), this.getAccessKeySecret()), "请检查oss配置属性");
        Preconditions.checkArgument(CollUtil.isNotEmpty(this.getBuckets()), "请检查bucket列表配置");
        Preconditions.checkArgument(StringUtils.isNotBlank(stsEndpoint), "sts的接入地址不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(roleArn), "roleArn不能为空");
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

        // 配置STS访问
        if (!StringUtils.isAnyBlank(this.getStsEndpoint(), this.getRoleArn())) {
            DefaultProfile.addEndpoint("", "Sts", this.getStsEndpoint());
            IClientProfile profile = DefaultProfile.getProfile("", this.getAccessKeyId(), this.getAccessKeySecret());
            // 用profile构造client
            DefaultAcsClient client = new DefaultAcsClient(profile);
            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(this.getRoleArn());
            request.setRoleSessionName(this.getRoleSessionName());
            // 若policy为空，则用户将获得该角色下所有权限
            request.setPolicy(this.getPolicy());
            // 设置凭证有效时间
            request.setDurationSeconds(this.getDurationSeconds());

            final AssumeRoleResponse response = client.getAcsResponse(request);
            System.out.println("Expiration: " + response.getCredentials().getExpiration());
            System.out.println("Access Key Id: " + response.getCredentials().getAccessKeyId());
            System.out.println("Access Key Secret: " + response.getCredentials().getAccessKeySecret());
            System.out.println("Security Token: " + response.getCredentials().getSecurityToken());
            System.out.println("RequestId: " + response.getRequestId());
        }
    }

}
