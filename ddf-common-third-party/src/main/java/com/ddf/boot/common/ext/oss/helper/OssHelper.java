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
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.core.util.ResourceUrlUtil;
import com.ddf.boot.common.ext.oss.config.AliOssPolicyDTO;
import com.ddf.boot.common.ext.oss.config.BucketProperty;
import com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration;
import com.ddf.boot.common.ext.oss.config.OssProperties;
import com.ddf.boot.common.ext.oss.config.StsTokenRequest;
import com.ddf.boot.common.ext.oss.config.StsTokenResponse;
import com.ddf.boot.common.ext.oss.dto.StsOssTransfer;
import com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @since 2020/10/12 13:33
 */
@Slf4j
public class OssHelper {

    /**
     * @see OssBeanAutoConfiguration
     */
    private IAcsClient defaultAcsClient;

    /**
     * @see OssBeanAutoConfiguration
     */
    private OSS defaultOssClient;

    private OssProperties ossProperties;

    public OssHelper(IAcsClient defaultAcsClient, OSS defaultOssClient, OssProperties ossProperties) {
        this.defaultAcsClient = defaultAcsClient;
        this.defaultOssClient = defaultOssClient;
        this.ossProperties = ossProperties;
    }

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
     */
    public OSS getDefaultOssClient() {
        return defaultOssClient;
    }

    /**
     * 返回主存储桶属性， 一般都会只用到一个存储桶，不会用到多个的
     */
    public BucketProperty getPrimaryBucketProperty() {
        return OssHelper.primaryBucketProperty;
    }


    /**
     * 返回STS核心授权信息
     *
     * @param stsTokenRequest 参数
     */
    public StsTokenResponse getOssToken(StsTokenRequest stsTokenRequest) {
        String path = getPath(stsTokenRequest.getPlatform(), stsTokenRequest.getIdentity());
        AssumeRoleResponse acsResponse = getAcsResponse(path);
        final AssumeRoleResponse.Credentials credentials = acsResponse.getCredentials();
        return StsTokenResponse.builder().securityToken(credentials.getSecurityToken()).accessKeySecret(
                credentials.getAccessKeySecret()).accessKeyId(credentials.getAccessKeyId()).expiration(
                credentials.getExpiration()).bucketName(primaryBucketProperty.getBucketName()).endPoint(
                primaryBucketProperty.getBucketEndpoint()).objectPrefix(path).build();
    }



    /**
     * 获取阿里云oss路径前缀, 优先使用cdn，没有再使用bucket域名
     */
    public String getOssPrefix() {
        return getOssPrefix(true);
    }

    /**
     * 获取阿里云oss路径前缀
     *
     * @param useCdn 如果存在cdn地址， 是否使用cdn路径
     */
    public String getOssPrefix(boolean useCdn) {
        if (useCdn) {
            return StringUtils.isNotBlank(ossProperties.getCdnAddr()) ? ossProperties.getCdnAddr() :
                    primaryBucketProperty.getBucketEndpoint();
        }
        return primaryBucketProperty.getBucketEndpoint();
    }


    /**
     * 获取oss存储对象真实访问地址, 存储时相对路径，取出时拼凑完成的访问前缀，优先使用cdn， 没有再使用Bucket域名
     *
     * @param objectKey 对象key
     */
    public String getOssObjectRealUrl(String objectKey) {
        return getOssObjectRealUrl(getOssPrefix(), objectKey);
    }

    /**
     * 获取oss存储对象真实访问地址, 存储时相对路径，取出时拼凑完成的访问前缀，优先使用cdn， 没有再使用Bucket域名
     *
     * @param prefix 主要是有可能会在循环中使用，所以获取前缀会在循环外获取一次， 然后在循环内部直接饮用，避免循环跨服务调用， 还有不需要使用cdn的
     * @param objectKey 对象key
     */
    public String getOssObjectRealUrl(String prefix, String objectKey) {
        return ResourceUrlUtil.wrapAbsolutePath(prefix, objectKey);
    }



    /**
     * 获取OSS token, 使用完成后关闭对象
     *
     * @param stsTokenRequest STStoken请求参数
     * @param consumer 参数
     */
    public void getStsOss(StsTokenRequest stsTokenRequest, Consumer<StsOssTransfer> consumer) {
        final StsTokenResponse acsResponse = getOssToken(stsTokenRequest);
        final OSS stsOss = new OSSClientBuilder().build(ossProperties.getEndpoint(), acsResponse.getAccessKeyId(),
                acsResponse.getAccessKeySecret(), acsResponse.getSecurityToken());
        try {
            final StsOssTransfer stsOssTransfer = StsOssTransfer.builder()
                    .oss(stsOss)
                    .stsTokenResponse(acsResponse)
                    .build();
            consumer.accept(stsOssTransfer);
        } finally {
            stsOss.shutdown();
        }
    }


    /**
     * 获取Acs 响应属性
     *
     * @param path 路径
     */
    private AssumeRoleResponse getAcsResponse(String path) {
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
     *
     * @param platform platform参数
     * @param identity identity参数
     */
    private static String getPath(String platform, String identity) {
        // 校验用户可控的路径段，防止 /、* 等字符拼入 STS 策略资源导致越权
        validatePathSegment(platform, "platform");
        validatePathSegment(identity, "identity");
        String formatTime = "yyyy/MM/dd";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatTime);
        String format = LocalDateTime.now().format(dtf);
        return MessageFormat.format("{0}/{1}/{2}/{3}", platform, format, identity, IdUtil.simpleUUID());
    }

    /**
     * 校验路径段：不允许为空，且不允许包含可导致 STS 策略资源越权或路径穿越的字符
     *
     * @param value 待校验值
     * @param name 字段名
     */
    private static void validatePathSegment(String value, String name) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        if (value.contains("/") || value.contains("*") || value.contains("?") || value.contains("\\") || value.contains(
                "..")) {
            throw new IllegalArgumentException(name + " 包含非法字符");
        }
    }

    /**
     * 对资源进行动态授权
     *
     * @param path 路径
     * @param bucketName 存储桶名称参数
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
