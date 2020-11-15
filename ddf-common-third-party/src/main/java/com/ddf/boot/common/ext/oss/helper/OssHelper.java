package com.ddf.boot.common.ext.oss.helper;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.IAcsClient;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:33
 */
@Slf4j
@Component
@AllArgsConstructor(onConstructor_={@Autowired})
public class OssHelper {

    /**
     * @see OssBeanAutoConfiguration
     */
    private final IAcsClient defaultAcsClient;

    /**
     * @see OssBeanAutoConfiguration
     */
    private final OSS defaultOssClient;

    private final OssProperties ossProperties;

    /**
     * 主存储桶配置
     */
    public static BucketProperty primaryBucketProperty;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        // 这里会保证一定能够拿到主存储桶信息， 在OssProperties初始化的时候已经校验过
        primaryBucketProperty = ossProperties.getBuckets().size() == 1 ? ossProperties.getBuckets().get(0) :
                ossProperties.getBuckets().stream().filter(BucketProperty::isPrimary).findFirst().get();
    }

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
        return OssHelper.primaryBucketProperty;
    }


    /**
     * 返回STS核心授权信息
     * @return
     * @throws ClientException
     */
    public StsTokenResponse getOssToken(StsTokenRequest stsTokenRequest) {
        String path = getPath(stsTokenRequest.getPlatform(), stsTokenRequest.getIdentity());
        AssumeRoleResponse acsResponse = getAcsResponse(path);
        final AssumeRoleResponse.Credentials credentials = acsResponse.getCredentials();
        return StsTokenResponse.builder()
                .securityToken(credentials.getSecurityToken())
                .accessKeySecret(credentials.getAccessKeySecret())
                .accessKeyId(credentials.getAccessKeyId())
                .expiration(credentials.getExpiration())
                .bucketName(primaryBucketProperty.getBucketName())
                .endPoint(primaryBucketProperty.getBucketEndpoint())
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
        final OSS stsOss = new OSSClientBuilder().build(ossProperties.getEndpoint(), acsResponse.getAccessKeyId(),
                acsResponse.getAccessKeySecret(), acsResponse.getSecurityToken());
        try {
            final StsOssTransfer stsOssTransfer = StsOssTransfer.builder()
                    .oss(stsOss)
                    .stsTokenResponse(acsResponse).build();
            consumer.accept(stsOssTransfer);
        } finally {
            stsOss.shutdown();
        }
    }


    /**
     * 获取Acs 响应属性
     * @param path
     * @return
     */
    public AssumeRoleResponse getAcsResponse(String path) {
        final AssumeRoleRequest request = new AssumeRoleRequest();
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(ossProperties.getRoleArn());
        request.setRoleSessionName(ossProperties.getRoleSessionName());
        // 若policy为空，则用户将获得该角色下所有权限
        request.setPolicy(getPolicy(primaryBucketProperty.getBucketName(), path));
        // 设置凭证有效时间
        request.setDurationSeconds(ossProperties.getDurationSeconds());
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
        return MessageFormat.format("{0}/{1}/{2}/{3}", platform, format, identity, IdUtil.simpleUUID());
    }

    /**
     * 对资源进行动态授权
     * @param path
     * @param bucketName
     * @return
     */
    private static String getPolicy(String bucketName, String path) {
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
}
