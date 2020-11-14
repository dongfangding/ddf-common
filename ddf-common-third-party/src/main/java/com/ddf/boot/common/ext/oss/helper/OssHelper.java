package com.ddf.boot.common.ext.oss.helper;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.ddf.boot.common.core.exception200.ServerErrorException;
import com.ddf.boot.common.ext.oss.config.*;
import com.ddf.boot.common.ext.oss.dto.StsOssTransfer;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:33
 */
@Component
@AllArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class OssHelper implements SmartInitializingSingleton {

    /**
     * @see OssBeanDefinitionRegistrar
     */
    private final DefaultAcsClient defaultAcsClient;

    private OssProperties primaryOssProperties;

    /**
     * @see OssBeanDefinitionRegistrar
     */
    private final OSS defaultOssClient;


    /**
     * 主存储桶配置
     */
    public BucketProperty primaryBucketProperty;


    /**
     * 返回默认OSS bean
     * @return
     */
    public OSS getDefaultOssClient() {
        return defaultOssClient;
    }

    /**
     * 返回主存储桶属性， 一般都会只用到一个存储桶，不会用到多个的
     * @return
     */
    public BucketProperty getPrimaryBucketProperty() {
        return this.primaryBucketProperty;
    }


    /**
     * 返回STS核心授权信息
     * @return
     * @throws ClientException
     */
    public StsTokenResponse getOssToken(StsTokenRequest stsTokenRequest) {
        String path = getPath(stsTokenRequest.getPlatform(), stsTokenRequest.getIdentity());
        AssumeRoleResponse acsResponse = getAcsResponse(stsTokenRequest, path);
        final AssumeRoleResponse.Credentials credentials = acsResponse.getCredentials();
        return StsTokenResponse.builder()
                .securityToken(credentials.getSecurityToken())
                .accessKeySecret(credentials.getAccessKeySecret())
                .accessKeyId(credentials.getAccessKeyId())
                .expiration(credentials.getExpiration())
                .bucketName(primaryBucketProperty.getBucketName())
                .endPoint(primaryBucketProperty.getBucketEndpoint())
                .ossPrefix(getOssPrefix(primaryBucketProperty.getBucketName(), primaryBucketProperty.getBucketEndpoint()))
                .objectPrefix(path)
                .build();
    }


    /**
     * 获取OSS token, 使用完成后关闭对象
     * @param stsTokenRequest
     * @return
     */
    public void getStsOss(StsTokenRequest stsTokenRequest, Consumer<StsOssTransfer> consumer) {
        final StsTokenResponse acsResponse = getOssToken(stsTokenRequest);
        final OSS stsOss = new OSSClientBuilder().build(primaryBucketProperty.getBucketEndpoint(), acsResponse.getAccessKeyId(),
                acsResponse.getAccessKeySecret(), acsResponse.getSecurityToken());
        try {
            final StsOssTransfer stsOssTransfer = StsOssTransfer.builder()
                    .oss(new OSSClientBuilder().build(acsResponse.getEndPoint(), acsResponse.getAccessKeyId(),
                            acsResponse.getAccessKeySecret(), acsResponse.getSecurityToken()))
                    .stsTokenResponse(acsResponse).build();
            consumer.accept(stsOssTransfer);
        } finally {
            stsOss.shutdown();
        }
    }


    /**
     * 获取Acs 响应属性
     * @param stsTokenRequest
     * @return
     */
    public AssumeRoleResponse getAcsResponse(StsTokenRequest stsTokenRequest, String path) {
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(primaryOssProperties.getRoleArn());
        request.setRoleSessionName(primaryOssProperties.getRoleSessionName());
        // 若policy为空，则用户将获得该角色下所有权限
        request.setPolicy(getPolicy(primaryBucketProperty.getBucketName(), path));
        // 设置凭证有效时间
        request.setDurationSeconds(primaryOssProperties.getDurationSeconds());
        try {
            return defaultAcsClient.getAcsResponse(request);
        } catch (ClientException e) {
            log.error("处理阿里云OSS异常!", e);
            throw new ServerErrorException("处理阿里云OSS异常");
        }
    }


    /**
     * 获取ObjectKey前缀路径
     * @param platform
     * @param identity
     * @return
     */
    private static String getPath(String platform, String identity) {
        String formatTime = "yyyy/MM/dd";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatTime);
        String format = LocalDateTime.now().format(dtf);
        return MessageFormat.format("{0}/{1}/{2}/{3}", format, platform, identity, IdUtil.simpleUUID());
    }

    /**
     * 获取oss访问域前缀
     * @param bucketName
     * @param endPoint
     * @return
     */
    public static String getOssPrefix(String bucketName, String endPoint) {
        return StrUtil.format("{}{}.{}", "https://", bucketName, endPoint);
    }

    /**
     * 对资源进行动态授权
     * @param path
     * @param bucketName
     * @return
     */
    private static String getPolicy(String path, String bucketName) {
        AliOssPolicyDTO.StatementBean statementBean = new AliOssPolicyDTO.StatementBean();
        statementBean.setEffect("Allow");
        statementBean.setAction(Lists.newArrayList("oss:GetObject", "oss:PutObject", "oss:HeadObject"));
        statementBean.setResource(Lists.newArrayList("acs:oss:*:*:" + bucketName + "/" + path + "*"));

        AliOssPolicyDTO aliOssPolicy = new AliOssPolicyDTO();
        aliOssPolicy.setBucket(bucketName);
        aliOssPolicy.setPath(path);
        aliOssPolicy.setVersion("1");
        aliOssPolicy.setStatement(Lists.newArrayList(statementBean));
        return JSONUtil.toJsonStr(aliOssPolicy);
    }

    /**
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (primaryBucketProperty == null) {
            final Optional<BucketProperty> first = primaryOssProperties.getBuckets().stream().filter(BucketProperty::isPrimary).findFirst();
            if (!first.isPresent()) {
                throw new ServerErrorException("没有配置主存储桶！");
            }
            primaryBucketProperty = first.get();
        }
    }
}
