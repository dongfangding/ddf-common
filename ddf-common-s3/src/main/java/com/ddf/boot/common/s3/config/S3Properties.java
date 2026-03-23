package com.ddf.boot.common.s3.config;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 兼容存储配置属性。
 *
 * <p>当前模块基于 S3 协议抽象，支持 MinIO、AWS S3、阿里云 OSS、腾讯云 COS 等兼容实现。</p>
 *
 * @author snowball
 */
@Data
@ConfigurationProperties(prefix = "customizer.infra.s3")
public class S3Properties {

    /**
     * 是否启用 S3 模块。
     */
    private boolean enable = true;

    /**
     * S3 服务端点。
     */
    private String endpoint;

    /**
     * 访问凭证 accessKey。
     */
    private String accessKey;

    /**
     * 访问凭证 secretKey。
     */
    private String secretKey;

    /**
     * 临时凭证 sessionToken，可选。
     */
    private String sessionToken;

    /**
     * 默认 Bucket 名称。
     */
    private String bucketName;

    /**
     * 区域配置。
     */
    private String region = "us-east-1";

    /**
     * 是否启用 HTTPS。
     */
    private boolean secure;

    /**
     * 是否使用 path-style 风格访问。
     */
    private boolean pathStyleAccess = true;

    /**
     * 自定义访问域名。
     */
    private String customDomain;

    /**
     * 连接超时时间，单位毫秒。
     */
    private int connectionTimeout = 10000;

    /**
     * 读取超时时间，单位毫秒。
     */
    private int readTimeout = 10000;

    /**
     * 多 Bucket 配置。
     */
    private List<S3BucketProperty> buckets;

    /**
     * 允许上传的文件扩展名集合。
     */
    private Set<String> allowedFileTypes = new HashSet<>();

    /**
     * 最大文件大小，单位字节。
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * 解析后的主 Bucket 配置。
     */
    private S3BucketProperty primaryBucketProperty;

    /**
     * 初始化并校验关键配置。
     */
    @PostConstruct
    public void init() {
        if (!enable) {
            return;
        }

        requireNotBlank(endpoint, "S3 endpoint 不能为空");
        requireNotBlank(accessKey, "S3 accessKey 不能为空");
        requireNotBlank(secretKey, "S3 secretKey 不能为空");

        if (buckets != null && !buckets.isEmpty()) {
            validateBuckets();
            primaryBucketProperty = resolvePrimaryBucket(buckets);
            return;
        }

        if (StringUtils.isNotBlank(bucketName)) {
            primaryBucketProperty = new S3BucketProperty();
            primaryBucketProperty.setBucketName(bucketName);
            primaryBucketProperty.setPrimary(true);
            return;
        }

        throw new IllegalStateException("请配置 S3 bucketName 或 buckets");
    }

    /**
     * 获取主 Bucket 名称。
     *
     * @return 主 Bucket 名称
     */
    public String getPrimaryBucketName() {
        return primaryBucketProperty != null ? primaryBucketProperty.getBucketName() : bucketName;
    }

    /**
     * 判断是否使用 path-style 访问。
     *
     * @return 是否使用 path-style
     */
    public boolean isPathStyleAccess() {
        if (StringUtils.isNotBlank(customDomain)) {
            return false;
        }
        return pathStyleAccess;
    }

    /**
     * 校验 Bucket 列表配置。
     */
    private void validateBuckets() {
        long primaryBucketCount = buckets.stream()
                .peek(bucket -> requireNotBlank(bucket.getBucketName(), "S3 buckets 中的 bucketName 不能为空"))
                .filter(S3BucketProperty::isPrimary)
                .count();
        if (primaryBucketCount > 1) {
            throw new IllegalStateException("S3 buckets 配置中只允许存在一个 primary=true 的 Bucket");
        }
    }

    /**
     * 解析主 Bucket，若没有显式 primary，则取第一个。
     *
     * @param bucketProperties Bucket 列表
     * @return 主 Bucket 配置
     */
    private S3BucketProperty resolvePrimaryBucket(List<S3BucketProperty> bucketProperties) {
        return bucketProperties.stream()
                .filter(S3BucketProperty::isPrimary)
                .findFirst()
                .orElse(bucketProperties.get(0));
    }

    /**
     * 校验字符串非空。
     *
     * @param value 待校验值
     * @param message 异常消息
     */
    private void requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalStateException(message);
        }
    }
}
