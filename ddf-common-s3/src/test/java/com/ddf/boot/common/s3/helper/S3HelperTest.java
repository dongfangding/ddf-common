package com.ddf.boot.common.s3.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3BucketProperty;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.UploadResult;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * S3Helper 行为测试
 *
 * @author Codex
 * @since 2026/03/23
 */
class S3HelperTest {

    @Test
    @DisplayName("仅配置 bucketName 时应能获取主 Bucket 名称")
    void shouldReturnPrimaryBucketNameFromBucketNameProperty() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        S3Properties properties = new S3Properties();
        properties.setBucketName("default-bucket");
        S3Helper helper = new S3Helper(s3Api, properties);

        assertEquals("default-bucket", helper.getPrimaryBucketName());
    }

    @Test
    @DisplayName("uploadAndOperate 应删除实际上传的对象而不是重新生成新 key")
    void shouldDeleteUploadedObjectKeyAfterOperate() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        S3Properties properties = createProperties();
        S3Helper helper = new S3Helper(s3Api, properties);
        UploadResult uploadResult = UploadResult.builder().objectKey("actual/object-key.png").build();
        when(s3Api.upload(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyLong())).thenReturn(
                uploadResult);

        helper.uploadAndOperate("platform", "identity", "avatar.png",
                new ByteArrayInputStream("demo".getBytes(StandardCharsets.UTF_8)), "image/png", 4L,
                objectKey -> assertEquals("actual/object-key.png", objectKey));

        verify(s3Api).delete("actual/object-key.png");
    }

    private static S3Properties createProperties() {
        S3Properties properties = new S3Properties();
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
