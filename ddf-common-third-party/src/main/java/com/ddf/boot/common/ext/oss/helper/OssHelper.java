package com.ddf.boot.common.ext.oss.helper;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.ddf.boot.common.core.exception200.ServerErrorException;
import com.ddf.boot.common.core.util.BeanUtil;
import com.ddf.boot.common.core.util.PreconditionUtil;
import com.ddf.boot.common.ext.oss.config.*;
import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>description</p >
 *
 * @author dongfang.ding
 * @version 1.0
 * @date 2020/10/12 13:33
 */
public class OssHelper {

    public static DefaultAcsClient defaultAcsClient;

    public static AssumeRoleRequest assumeRoleRequest;

    public static OssProperties ossProperties;

    /**
     * 主存储桶配置
     */
    public static BucketProperty primaryBucketProperty;

    /**
     * 存储OSS实例的map, key为bucket name
     */
    private final static Map<String, OSS> ossMap = new ConcurrentHashMap<>();

    /**
     * 放入实例
     * @param bucketName
     * @param oss
     */
    public static void putOss(String bucketName, OSS oss) {
        ossMap.put(bucketName, oss);
    }


    /**
     * 根据bucket name 获取存储的实例
     * @param bucketName
     * @return
     */
    public static OSS getOss(String bucketName) {
        return ossMap.get(bucketName);
    }

    /**
     * 获取STS授权信息
     * @return
     * @throws ClientException
     */
    public static AssumeRoleResponse getRoleResponse() throws ClientException {
        return defaultAcsClient.getAcsResponse(assumeRoleRequest);
    }

    /**
     * 返回STS核心授权信息
     * @return
     * @throws ClientException
     */
    public static StsTokenResponse getStsCredentials(StsTokenRequest request) throws ClientException {
        if (primaryBucketProperty == null) {
            final Optional<BucketProperty> first = ossProperties.getBuckets().stream().filter(BucketProperty::isPrimary).findFirst();
            if (!first.isPresent()) {
                throw new ServerErrorException("没有配置主存储桶！");
            }
            primaryBucketProperty = first.get();
        }
        final StsTokenResponse response = BeanUtil.copy(getRoleResponse().getCredentials(), StsTokenResponse.class);
        PreconditionUtil.checkArgument(response != null, "获取oss授权信息失败");
        response.setObjectPrefix(getPath(request.getPlatform(), request.getIdentity()));
        response.setOssPrefix(getOssPrefix(primaryBucketProperty.getBucketName(), primaryBucketProperty.getBucketEndpoint()));
        response.setBucketName(primaryBucketProperty.getBucketName());
        response.setEndPoint(primaryBucketProperty.getBucketEndpoint());
        return response;
    }

    /**
     * 获取临时授权的oss实例，其实这个一般用不到，服务端自己使用肯定是直接使用的，一般是将授权信息返回给web客户端，然后web客户端这么去玩
     * @return
     * @throws ClientException
     */
    public static OSS getStsOss() throws ClientException {
        AssumeRoleResponse acsResponse = defaultAcsClient.getAcsResponse(assumeRoleRequest);
        AssumeRoleResponse.Credentials credentials = acsResponse.getCredentials();
        final OSS build = new OSSClientBuilder().build(ossProperties.getEndpoint(), credentials.getAccessKeyId(),
                credentials.getAccessKeySecret(), credentials.getSecurityToken());
        PreconditionUtil.checkArgument(build != null, "获取oss授权信息失败");
        return build;
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
     * todo 完善
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
        String result = JSONUtil.toJsonStr(aliOssPolicy);
        return result;
    }
}
