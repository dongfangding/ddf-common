package com.ddf.boot.common.s3.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.UploadResult;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

/**
 * FileUploadHelper 行为测试
 *
 * @author Codex
 * @since 2026/03/23
 */
class FileUploadHelperTest {

    @Test
    @DisplayName("空文件应直接拒绝上传")
    void shouldRejectEmptyFile() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        FileUploadHelper helper = new FileUploadHelper(s3Api, new S3Properties());
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> helper.upload("platform", "identity", file)
        );

        assertEquals("上传文件不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("默认配置下应拒绝上传非图片类型文件")
    void shouldRejectNonImageFileByDefault() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        FileUploadHelper helper = new FileUploadHelper(s3Api, new S3Properties());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "demo".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> helper.upload("platform", "identity", file)
        );

        assertEquals("不支持的文件类型: pdf", exception.getMessage());
    }

    @Test
    @DisplayName("显式配置 allowedFileTypes 后应允许对应类型上传")
    void shouldAllowConfiguredFileTypes() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        S3Properties properties = new S3Properties();
        properties.setAllowedFileTypes(Set.of("pdf"));
        FileUploadHelper helper = new FileUploadHelper(s3Api, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "report.pdf",
                "application/pdf",
                "demo".getBytes()
        );
        UploadResult uploadResult = UploadResult.builder().objectKey("uploaded/report.pdf").url("https://cdn/report.pdf").build();
        when(s3Api.upload(any(String.class), any(), eq("application/pdf"), eq(4L))).thenReturn(uploadResult);

        UploadResult result = helper.upload("platform", "identity", file);

        assertEquals("uploaded/report.pdf", result.getObjectKey());
    }

    @Test
    @DisplayName("生成缩略图时应追加 _thumb.jpg 并回填 thumbPath")
    void shouldUploadThumbnailAndSetThumbPath() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        FileUploadHelper helper = new FileUploadHelper(s3Api, new S3Properties());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO7Z0a4AAAAASUVORK5CYII=")
        );
        when(s3Api.upload(any(String.class), any(), any(String.class), any(Long.class)))
                .thenReturn(UploadResult.builder().objectKey("origin/path/avatar.png").url("https://cdn/origin.png").build())
                .thenReturn(UploadResult.builder().objectKey("origin/path/avatar_thumb.jpg").url("https://cdn/avatar_thumb.jpg").build());

        UploadResult result = helper.upload("platform", "identity", file, true);

        assertEquals("https://cdn/avatar_thumb.jpg", result.getThumbPath());
        verify(s3Api, times(2)).upload(any(String.class), any(), any(String.class), any(Long.class));
    }

    @Test
    @DisplayName("超过最大文件大小时应拒绝上传")
    void shouldRejectFileWhenExceedingMaxSize() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        S3Properties properties = new S3Properties();
        properties.setMaxFileSize(3);
        FileUploadHelper helper = new FileUploadHelper(s3Api, properties);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "demo".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> helper.upload("platform", "identity", file)
        );

        assertEquals("文件大小不能超过 0MB", exception.getMessage());
    }

    @Test
    @DisplayName("缺少 content-type 时应回退为 application/octet-stream")
    void shouldFallbackToOctetStreamWhenContentTypeMissing() {
        S3Api s3Api = Mockito.mock(S3Api.class);
        FileUploadHelper helper = new FileUploadHelper(s3Api, new S3Properties());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                null,
                "demo".getBytes()
        );
        UploadResult uploadResult = UploadResult.builder().objectKey("uploaded/avatar.png").build();
        when(s3Api.upload(any(String.class), any(), eq("application/octet-stream"), eq(4L))).thenReturn(uploadResult);

        UploadResult result = helper.upload("platform", "identity", file);

        assertEquals("uploaded/avatar.png", result.getObjectKey());
    }

    @Test
    @DisplayName("生成对象 key 时无扩展名文件不应追加后缀")
    void shouldGenerateObjectKeyWithoutSuffixWhenFilenameHasNoExtension() {
        FileUploadHelper helper = new FileUploadHelper(Mockito.mock(S3Api.class), new S3Properties());

        String objectKey = helper.generateObjectKey("platform", "identity", "README");

        assertTrue(objectKey.startsWith("platform/"));
        assertTrue(objectKey.contains("/identity/"));
        assertTrue(!objectKey.endsWith("."));
    }
}
