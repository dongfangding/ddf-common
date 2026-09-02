package com.ddf.boot.common.ext.oss.helper;

import com.aliyun.oss.OSS;
import com.aliyuncs.IAcsClient;
import com.ddf.boot.common.ext.oss.config.BucketProperty;
import com.ddf.boot.common.ext.oss.config.OssProperties;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * OssHelper 测试
 *
 * @author Codex
 * @since 2026/04/20
 */
class OssHelperTest {

    private final IAcsClient acsClient = mock(IAcsClient.class);
    private final OSS ossClient = mock(OSS.class);

    @Test
    @DisplayName("应初始化并返回主存储桶配置")
    void shouldInitPrimaryBucketProperty() {
        OssProperties properties = buildProperties("https://cdn.example.com",
                List.of(bucket("bucket-a", "https://bucket-a.oss-cn-hangzhou.aliyuncs.com", false),
                        bucket("bucket-b", "https://bucket-b.oss-cn-hangzhou.aliyuncs.com", true)));
        OssHelper helper = new OssHelper(acsClient, ossClient, properties);

        helper.init();

        assertSame(ossClient, helper.getDefaultOssClient());
        assertEquals("bucket-b", helper.getPrimaryBucketProperty().getBucketName());
    }

    @Test
    @DisplayName("应按是否使用 CDN 返回 OSS 前缀")
    void shouldReturnOssPrefixByCdnFlag() {
        OssProperties properties = buildProperties("https://cdn.example.com",
                List.of(bucket("bucket-a", "https://bucket-a.oss-cn-hangzhou.aliyuncs.com", true)));
        OssHelper helper = new OssHelper(acsClient, ossClient, properties);
        helper.init();

        assertEquals("https://cdn.example.com", helper.getOssPrefix());
        assertEquals("https://cdn.example.com", helper.getOssPrefix(true));
        assertEquals("https://bucket-a.oss-cn-hangzhou.aliyuncs.com", helper.getOssPrefix(false));
    }

    @Test
    @DisplayName("应拼接对象真实访问地址")
    void shouldBuildRealObjectUrl() {
        OssProperties properties = buildProperties("",
                List.of(bucket("bucket-a", "https://bucket-a.oss-cn-hangzhou.aliyuncs.com", true)));
        OssHelper helper = new OssHelper(acsClient, ossClient, properties);
        helper.init();

        assertEquals("https://bucket-a.oss-cn-hangzhou.aliyuncs.com/path/to/demo.jpg",
                helper.getOssObjectRealUrl("path/to/demo.jpg"));
        assertEquals("https://static.example.com/path/to/demo.jpg",
                helper.getOssObjectRealUrl("https://static.example.com", "path/to/demo.jpg"));
    }

    private static OssProperties buildProperties(String cdnAddr, List<BucketProperty> buckets) {
        OssProperties properties = new OssProperties();
        properties.setCdnAddr(cdnAddr);
        properties.setBuckets(buckets);
        return properties;
    }

    private static BucketProperty bucket(String name, String endpoint, boolean primary) {
        BucketProperty property = new BucketProperty();
        property.setBucketName(name);
        property.setBucketEndpoint(endpoint);
        property.setPrimary(primary);
        return property;
    }
}
