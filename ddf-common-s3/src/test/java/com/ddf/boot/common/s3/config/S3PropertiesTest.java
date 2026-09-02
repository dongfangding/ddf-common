package com.ddf.boot.common.s3.config;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * S3Properties 行为测试
 *
 * @author Codex
 * @since 2026/03/23
 */
class S3PropertiesTest {

    @Test
    @DisplayName("存在多个主 Bucket 时应拒绝启动")
    void shouldRejectMultiplePrimaryBuckets() {
        S3Properties properties = createBaseProperties();
        properties.setBuckets(List.of(bucket("a", true), bucket("b", true)));

        IllegalStateException exception = assertThrows(IllegalStateException.class, properties::init);

        assertEquals("S3 buckets 配置中只允许存在一个 primary=true 的 Bucket", exception.getMessage());
    }

    @Test
    @DisplayName("仅配置 bucketName 时应回填主 Bucket")
    void shouldCreatePrimaryBucketFromBucketName() {
        S3Properties properties = createBaseProperties();
        properties.setBucketName("default-bucket");

        properties.init();

        assertEquals("default-bucket", properties.getPrimaryBucketName());
    }

    @Test
    @DisplayName("自定义域名存在时应强制走虚拟主机风格")
    void shouldDisablePathStyleWhenCustomDomainConfigured() {
        S3Properties properties = createBaseProperties();
        properties.setBucketName("default-bucket");
        properties.setCustomDomain("https://cdn.example.com");
        properties.setPathStyleAccess(true);

        properties.init();

        assertEquals(false, properties.isPathStyleAccess());
    }

    private static S3Properties createBaseProperties() {
        S3Properties properties = new S3Properties();
        properties.setEndpoint("https://s3.example.com");
        properties.setAccessKey("ak");
        properties.setSecretKey("sk");
        return properties;
    }

    private static S3BucketProperty bucket(String bucketName, boolean primary) {
        S3BucketProperty bucketProperty = new S3BucketProperty();
        bucketProperty.setBucketName(bucketName);
        bucketProperty.setPrimary(primary);
        return bucketProperty;
    }
}
