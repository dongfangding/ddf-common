package com.ddf.boot.common.s3.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.s3.config.S3BucketProperty;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.PresignedUrlResult;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.http.Method;
import java.lang.reflect.Field;
import java.time.Duration;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * S3Service 行为测试
 *
 * @author Codex
 * @since 2026/03/23
 */
class S3ServiceTest {

    @Test
    @DisplayName("path-style-access=false 时应生成虚拟主机风格访问地址")
    void shouldBuildVirtualHostStyleUrl() {
        S3Properties properties = createProperties();
        properties.setPathStyleAccess(false);

        S3Service service = new S3Service(properties);

        String url = service.getUrl("archive", "path/demo.txt");

        assertEquals("https://archive.s3.example.com/path/demo.txt", url);
    }

    @Test
    @DisplayName("预签名上传 URL 应携带 content-type 约束头")
    void shouldAddContentTypeHeaderForPresignedUploadUrl() throws Exception {
        S3Properties properties = createProperties();
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("https://signed.example.com/upload");

        S3Service service = new S3Service(minioClient, properties);

        PresignedUrlResult result = service.getPresignedUploadUrl(
                "archive", "path/demo.txt", "image/png", Duration.ofMinutes(15));

        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());
        GetPresignedObjectUrlArgs args = captor.getValue();
        assertEquals(Method.PUT, args.method());
        assertEquals("image/png", args.extraHeaders().get("Content-Type").iterator().next());
        assertEquals("https://signed.example.com/upload", result.getUrl());
    }

    @Test
    @DisplayName("配置了 sessionToken 时应使用 StaticProvider 注入临时凭证")
    void shouldUseStaticProviderWhenSessionTokenConfigured() throws Exception {
        S3Properties properties = createProperties();
        properties.setSessionToken("session-token");

        S3Service service = new S3Service(properties);
        Object asyncClient = readField(service.getMinioClient(), "asyncClient");
        Provider provider = (Provider) readField(asyncClient, "provider");

        assertInstanceOf(StaticProvider.class, provider);
        assertEquals("session-token", provider.fetch().sessionToken());
    }

    @Test
    @DisplayName("path-style-access=false 时应启用虚拟主机风格并应用自定义超时")
    void shouldConfigureVirtualStyleAndTimeouts() throws Exception {
        S3Properties properties = createProperties();
        properties.setPathStyleAccess(false);
        properties.setConnectionTimeout(3210);
        properties.setReadTimeout(6540);

        S3Service service = new S3Service(properties);
        Object asyncClient = readField(service.getMinioClient(), "asyncClient");
        boolean useVirtualStyle = (boolean) readField(asyncClient, "useVirtualStyle");
        OkHttpClient httpClient = (OkHttpClient) readField(asyncClient, "httpClient");

        assertTrue(useVirtualStyle);
        assertEquals(3210L, httpClient.connectTimeoutMillis());
        assertEquals(6540L, httpClient.readTimeoutMillis());
        assertEquals(6540L, httpClient.writeTimeoutMillis());
    }

    @Test
    @DisplayName("path-style-access=true 时不启用虚拟主机风格")
    void shouldKeepPathStyleWhenConfigured() throws Exception {
        S3Properties properties = createProperties();
        properties.setPathStyleAccess(true);

        S3Service service = new S3Service(properties);
        Object asyncClient = readField(service.getMinioClient(), "asyncClient");
        boolean useVirtualStyle = (boolean) readField(asyncClient, "useVirtualStyle");

        assertFalse(useVirtualStyle);
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Class<?> currentType = target.getClass();
        while (currentType != null) {
            try {
                Field field = currentType.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ex) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static S3Properties createProperties() {
        S3Properties properties = new S3Properties();
        properties.setEndpoint("https://s3.example.com");
        properties.setAccessKey("ak");
        properties.setSecretKey("sk");
        properties.setRegion("us-east-1");
        properties.setBucketName("default-bucket");
        properties.setPrimaryBucketProperty(primaryBucket("default-bucket"));
        return properties;
    }

    private static S3BucketProperty primaryBucket(String bucketName) {
        S3BucketProperty bucketProperty = new S3BucketProperty();
        bucketProperty.setBucketName(bucketName);
        bucketProperty.setPrimary(true);
        return bucketProperty;
    }
}
