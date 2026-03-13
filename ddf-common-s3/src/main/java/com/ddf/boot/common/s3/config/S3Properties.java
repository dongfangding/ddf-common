package com.ddf.boot.common.s3.config;

import cn.hutool.core.collection.CollUtil;
import com.google.common.base.Preconditions;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 兼容存储配置属性类.
 *
 * <p>支持 MinIO、AWS S3、阿里云 OSS、腾讯云 COS 等 S3 兼容存储服务.</p>
 *
 * <h2>配置示例</h2>
 * <h3>MinIO 本地环境</h3>
 * <pre>
 * customizer.infra:
 *   s3:
 *     enable: true
 *     endpoint: http://localhost:9000
 *     access-key: minioadmin
 *     secret-key: minioadmin
 *     bucket-name: my-bucket
 *     region: us-east-1
 *     secure: false
 *     path-style-access: true
 * </pre>
 *
 * <h3>AWS S3 生产环境</h3>
 * <pre>
 * customizer.infra:
 *   s3:
 *     enable: true
 *     endpoint: https://s3.amazonaws.com
 *     access-key: AWS_ACCESS_KEY
 *     secret-key: AWS_SECRET_KEY
 *     session-token: AWS_SESSION_TOKEN  # 可选，临时凭证
 *     bucket-name: prod-bucket
 *     region: us-east-1
 *     secure: true
 *     path-style-access: false  # AWS S3 推荐使用虚拟主机风格
 * </pre>
 *
 * <h3>阿里云 OSS</h3>
 * <pre>
 * customizer.infra:
 *   s3:
 *     enable: true
 *     endpoint: https://oss-cn-hangzhou.aliyuncs.com
 *     access-key: OSS_ACCESS_KEY
 *     secret-key: OSS_SECRET_KEY
 *     bucket-name: oss-bucket
 *     region: cn-shanghai
 *     secure: true
 * </pre>
 *
 * @author snowball
 */
@Data
@ConfigurationProperties(prefix = "customizer.infra.s3")
public class S3Properties {

    /**
     * 是否启用 S3 客户端，默认为 true.
     */
    private boolean enable = true;

    /**
     * S3 兼容存储服务端点.
     *
     * <p>示例:</p>
     * <ul>
     *   <li>MinIO: http://localhost:9000</li>
     *   <li>AWS S3: https://s3.amazonaws.com</li>
     *   <li>阿里云 OSS: https://oss-cn-hangzhou.aliyuncs.com</li>
     * </ul>
     */
    private String endpoint;

    /**
     * Access Key / Access Key ID.
     */
    private String accessKey;

    /**
     * Secret Key / Secret Access Key.
     */
    private String secretKey;

    /**
     * AWS Session Token（可选），用于临时凭证.
     *
     * <p>使用 AWS STS 临时凭证时需要设置.</p>
     */
    private String sessionToken;

    /**
     * 默认 Bucket 名称.
     */
    private String bucketName;

    /**
     * 区域（Region）.
     *
     * <p>AWS S3 必须设置正确的 region，部分操作需要 region 信息.</p>
     */
    private String region = "us-east-1";

    /**
     * 是否使用 HTTPS/SSL.
     */
    private boolean secure;

    /**
     * 是否使用路径风格访问（Path Style）.
     *
     * <p>默认为 true.</p>
     * <ul>
     *   <li>true: http://endpoint/bucket/key (MinIO 默认)</li>
     *   <li>false: http://bucket.endpoint/key (AWS S3 推荐)</li>
     * </ul>
     */
    private boolean pathStyleAccess = true;

    /**
     * 自定义域名（可选）.
     *
     * <p>用于 CNAME 绑定自定义域名场景.</p>
     */
    private String customDomain;

    /**
     * 连接超时时间（毫秒）.
     */
    private int connectionTimeout = 10000;

    /**
     * 读取超时时间（毫秒）.
     */
    private int readTimeout = 10000;

    /**
     * 多 Bucket 配置.
     */
    private List<S3BucketProperty> buckets;

    /**
     * 允许上传的文件类型（扩展名小写）.
     *
     * <p>为空时使用默认值（图片类型）.</p>
     */
    private Set<String> allowedFileTypes = new HashSet<>();

    /**
     * 最大文件大小（字节）.
     *
     * <p>默认为 10MB.</p>
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * 主 Bucket 配置，初始化时自动设置.
     */
    private S3BucketProperty primaryBucketProperty;

    /**
     * 验证配置并初始化主 Bucket.
     */
    @PostConstruct
    public void init() {
        if (!enable) {
            return;
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(endpoint), "S3 endpoint 不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(accessKey), "S3 accessKey 不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(secretKey), "S3 secretKey 不能为空");

        if (CollUtil.isNotEmpty(buckets)) {
            // 找到主 Bucket
            primaryBucketProperty = buckets.stream()
                    .filter(S3BucketProperty::isPrimary)
                    .findFirst()
                    .orElse(buckets.get(0));
        } else if (StringUtils.isNotBlank(bucketName)) {
            primaryBucketProperty = new S3BucketProperty();
            primaryBucketProperty.setBucketName(bucketName);
            primaryBucketProperty.setPrimary(true);
        } else {
            Preconditions.checkArgument(false, "请配置 S3 bucketName 或 buckets");
        }
    }

    /**
     * 获取主 Bucket 名称.
     */
    public String getPrimaryBucketName() {
        return primaryBucketProperty != null ? primaryBucketProperty.getBucketName() : bucketName;
    }

    /**
     * 检查是否使用路径风格访问.
     */
    public boolean isPathStyleAccess() {
        // 如果设置了自定义域名，使用虚拟主机风格
        if (StringUtils.isNotBlank(customDomain)) {
            return false;
        }
        return pathStyleAccess;
    }

}
