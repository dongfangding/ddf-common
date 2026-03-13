package com.ddf.boot.common.s3.helper;

import cn.hutool.core.util.IdUtil;
import com.ddf.boot.common.s3.api.S3Api;
import com.ddf.boot.common.s3.config.S3Properties;
import com.ddf.boot.common.s3.model.UploadResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传辅助类.
 *
 * <p>在 S3Helper 基础上扩展，提供文件校验、缩略图生成等功能.</p>
 *
 * @author snowball
 */
@Slf4j
@RequiredArgsConstructor
public class FileUploadHelper {

    private final S3Api s3Api;
    private final S3Properties s3Properties;

    /**
     * 默认允许的图片类型.
     */
    private static final Set<String> DEFAULT_ALLOWED_IMAGE_TYPES = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    /**
     * 默认允许的文档类型.
     */
    private static final Set<String> DEFAULT_ALLOWED_DOC_TYPES = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    /**
     * 默认允许的视频类型.
     */
    private static final Set<String> DEFAULT_ALLOWED_VIDEO_TYPES = Set.of(
            "mp4", "avi", "mov", "wmv", "flv", "mkv"
    );

    /**
     * 默认最大文件大小 (10MB).
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 默认缩略图宽度.
     */
    private static final int DEFAULT_THUMBNAIL_WIDTH = 200;

    /**
     * 默认缩略图高度.
     */
    private static final int DEFAULT_THUMBNAIL_HEIGHT = 200;

    /**
     * 上传文件（带校验）.
     *
     * @param platform      平台标识
     * @param identity      业务标识
     * @param multipartFile 上传的文件
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile) {
        return upload(platform, identity, multipartFile, false, false);
    }

    /**
     * 上传文件（带校验和缩略图）.
     *
     * @param platform       平台标识
     * @param identity       业务标识
     * @param multipartFile  上传的文件
     * @param generateThumb  是否生成缩略图
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile,
                               boolean generateThumb) {
        return upload(platform, identity, multipartFile, generateThumb, false);
    }

    /**
     * 上传文件（完整参数）.
     *
     * @param platform       平台标识
     * @param identity       业务标识
     * @param multipartFile  上传的文件
     * @param generateThumb  是否生成缩略图
     * @param allowVideo     是否允许视频上传
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile,
                               boolean generateThumb, boolean allowVideo) {
        // 文件校验
        validateFile(multipartFile, allowVideo);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            String filename = multipartFile.getOriginalFilename();
            String contentType = multipartFile.getContentType();
            long size = multipartFile.getSize();

            // 生成 objectKey
            String objectKey = generateObjectKey(platform, identity, filename);

            // 上传原图
            UploadResult result = s3Api.upload(objectKey, inputStream, contentType, size);

            // 生成缩略图
            if (generateThumb && isImage(filename)) {
                UploadResult thumbResult = generateAndUploadThumbnail(platform, identity,
                        multipartFile, objectKey);
                result.setThumbPath(thumbResult.getUrl());
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量上传文件（带校验）.
     *
     * @param platform      平台标识
     * @param identity      业务标识
     * @param multipartFiles 上传的文件数组
     * @return 上传结果列表
     */
    public List<UploadResult> batchUpload(String platform, String identity,
                                          MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .map(file -> upload(platform, identity, file))
                .toList();
    }

    /**
     * 生成并上传缩略图.
     * @param platform 参数
     * @param identity 参数
     * @param multipartFile 参数
     * @param originalObjectKey 参数
     */
    private UploadResult generateAndUploadThumbnail(String platform, String identity,
                                                    MultipartFile multipartFile,
                                                    String originalObjectKey) throws IOException {
        // 生成缩略图 objectKey
        String thumbObjectKey = generateThumbObjectKey(originalObjectKey);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 生成缩略图
            Thumbnails.of(multipartFile.getInputStream())
                    .size(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);

            byte[] thumbData = outputStream.toByteArray();
            String contentType = "image/jpeg";

            // 上传缩略图
            return s3Api.upload(thumbObjectKey, new ByteArrayInputStream(thumbData),
                    contentType, thumbData.length);
        }
    }

    /**
     * 生成缩略图的 objectKey.
     * @param originalObjectKey 参数
     */
    private String generateThumbObjectKey(String originalObjectKey) {
        String basePath = originalObjectKey.substring(0, originalObjectKey.lastIndexOf("."));
        return basePath + "_thumb.jpg";
    }

    /**
     * 校验文件.
     * @param file 参数
     * @param allowVideo 参数
     */
    private void validateFile(MultipartFile file, boolean allowVideo) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 文件大小校验
        long maxSize = s3Properties.getMaxFileSize() > 0
                ? s3Properties.getMaxFileSize()
                : DEFAULT_MAX_FILE_SIZE;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 文件类型校验
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(filename).toLowerCase();

        // 获取允许的类型
        Set<String> allowedTypes = s3Properties.getAllowedFileTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            allowedTypes = DEFAULT_ALLOWED_IMAGE_TYPES;
            if (allowVideo) {
                allowedTypes = new java.util.HashSet<>(allowedTypes);
                ((java.util.HashSet<String>) allowedTypes).addAll(DEFAULT_ALLOWED_VIDEO_TYPES);
            }
        }

        if (!allowedTypes.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 判断是否为图片.
     * @param filename 参数
     */
    private boolean isImage(String filename) {
        if (filename == null) {
            return false;
        }
        String extension = getFileExtension(filename).toLowerCase();
        return DEFAULT_ALLOWED_IMAGE_TYPES.contains(extension);
    }

    /**
     * 获取文件扩展名.
     * @param filename 参数
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 生成对象 Key（路径）.
     * @param platform 参数
     * @param identity 参数
     * @param filename 参数
     */
    public String generateObjectKey(String platform, String identity, String filename) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String format = LocalDateTime.now().format(dtf);
        String uuid = IdUtil.simpleUUID();
        String extension = getFileExtension(filename);
        if (!extension.isEmpty()) {
            extension = "." + extension;
        }
        return String.format("%s/%s/%s/%s%s", platform, format, identity, uuid, extension);
    }

}
