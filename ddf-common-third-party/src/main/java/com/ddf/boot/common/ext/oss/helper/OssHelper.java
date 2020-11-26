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
import com.ddf.boot.common.core.util.ResourceUrlUtils;
import com.ddf.boot.common.ext.oss.config.AliOssPolicyDTO;
import com.ddf.boot.common.ext.oss.config.BucketProperty;
import com.ddf.boot.common.ext.oss.config.OssBeanAutoConfiguration;
import com.ddf.boot.common.ext.oss.config.OssProperties;
import com.ddf.boot.common.ext.oss.config.StsTokenRequest;
import com.ddf.boot.common.ext.oss.config.StsTokenResponse;
import com.ddf.boot.common.ext.oss.dto.StsOssTransfer;
import com.google.common.collect.Lists;
import java.io.File;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:33
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_={@Autowired})
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
     * 获取阿里云oss路径前缀, 优先使用cdn，没有再使用bucket域名
     * @return
     */
    public String getOssPrefix() {
        return getOssPrefix(true);
    }

    /**
     * 获取阿里云oss路径前缀
     * @param useCdn 如果存在cdn地址， 是否使用cdn路径
     * @return
     */
    public String getOssPrefix(boolean useCdn) {
        if (useCdn) {
            return StringUtils.isNotBlank(ossProperties.getCdnAddr()) ? ossProperties.getCdnAddr() : primaryBucketProperty.getBucketEndpoint();
        }
        return primaryBucketProperty.getBucketEndpoint();
    }


    /**
     * 获取oss存储对象真实访问地址, 存储时相对路径，取出时拼凑完成的访问前缀，优先使用cdn， 没有再使用Bucket域名
     *
     * @param objectKey 对象key
     * @return
     */
    public String getOssObjectRealUrl(String objectKey) {
        return getOssObjectRealUrl(getOssPrefix(), objectKey);
    }

    /**
     * 获取oss存储对象真实访问地址, 存储时相对路径，取出时拼凑完成的访问前缀，优先使用cdn， 没有再使用Bucket域名
     *
     * @param prefix    主要是有可能会在循环中使用，所以获取前缀会在循环外获取一次， 然后在循环内部直接饮用，避免循环跨服务调用， 还有不需要使用cdn的
     * @param objectKey 对象key
     * @return
     */
    public String getOssObjectRealUrl(String prefix, String objectKey) {
        return ResourceUrlUtils.wrapAbsolutePath(prefix, objectKey);
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


    public static void main(String[] args) {
        final OSS stsOss = new OSSClientBuilder().build("oss-cn-hangzhou.aliyuncs.com", "STS.NTXKVzNyBFa1HFFYC21t9awAb",
                "965xDoLYyGZeY5WdRUfRzFDk8w37JuA9i4HJuUL8b1QJ", "CAIS5AJ1q6Ft5B2yfSjIr5ftAOzOo6Zj8aPaSmD3vUNnPfsVjrLqgDz2IH1NfXNgAe0ev/Q2mWlZ6Psdlq1oSpZDHaZ87G7HqMY5yxioRqackWPcj9Vd+jTMewW6Dxr8w7X8AYHQR8/cffGAck3NkjQJr5LxaTSlWS7jU/iOkoU1QdkLeQO6YDFaZrJRPRAwkNIGEnHTOP2xUHjtmXGCLEdhti12i2509d6noKum5wHZkUfxx8IMuo31OeLEVcR3O4plWNrH4I5Mf6HagilL8EoIpuUkgKVc8DaCutCDDhxN7g6adOHT9MZoKAI+P+9gQ/Qc66Gl0qck/eaIztuslR8WY70KDHiAG4vwn8fNFb34botkebr1N3jHkPL3b8Ov6l16OS1Hb1MUJIN6cEUdU0J8FmvoTYa8403PbwuZTKyI7bo7y5IdzS+zoIPTfQjXHu3IgX9FY85iMhwyXBkNxnx1r3Wbm4exGRqAAa069nX+8Odb6DsF3dyfeylI8yklBMFqaOzE/BqjTJ0ziOOP6uD078pcFLeS5bazr3cwrIGK7DNIrH1Vf+wnxMXeXTyxm1I+T17pyAsEwSIuNu2MSXocV8twtV7umeqws9dJnAMCe1d7/ztJERbDMGsUHrW6WCrNsUYqqeeGpv5d");

        stsOss.putObject("dapai-live-test", "console/1/2020/11/24/b31736a36477477c87dae69055ddfddb.svga",
                new File("C:\\Users\\Administrator\\Pictures\\sharding\\rocket.svga"));

    }
}
