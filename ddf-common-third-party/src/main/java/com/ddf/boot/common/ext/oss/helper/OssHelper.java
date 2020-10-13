package com.ddf.boot.common.ext.oss.helper;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.ddf.boot.common.core.util.BeanUtil;
import com.ddf.boot.common.ext.oss.config.OssProperties;
import com.ddf.boot.common.ext.oss.config.StsCredentials;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/10/12 13:33
 */
public class OssHelper {

    public static DefaultAcsClient defaultAcsClient;

    public static AssumeRoleRequest assumeRoleRequest;

    public static OssProperties ossProperties;

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
    public static StsCredentials getStsCredentials() throws ClientException {
        return BeanUtil.copy(getRoleResponse().getCredentials(), StsCredentials.class);
    }

    /**
     * 获取临时授权的oss实例，其实这个一般用不到，服务端自己使用肯定是直接使用的，一般是将授权信息返回给web客户端，然后web客户端这么去玩
     * @return
     * @throws ClientException
     */
    public static OSS getStsOss() throws ClientException {
        AssumeRoleResponse acsResponse = defaultAcsClient.getAcsResponse(assumeRoleRequest);
        AssumeRoleResponse.Credentials credentials = acsResponse.getCredentials();
        return new OSSClientBuilder().build(ossProperties.getEndpoint(), credentials.getAccessKeyId(),
                credentials.getAccessKeySecret(), credentials.getSecurityToken());
    }
}
