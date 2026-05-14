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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传辅助类。
 * <p>在 S3 上传流程上补充文件校验、对象 key 生成和缩略图生成能力。</p>
 *
 * @author snowball
 */
@RequiredArgsConstructor
public class FileUploadHelper {

    /**
     * 默认允许的图片扩展名。
     */
    private static final Set<String> DEFAULT_ALLOWED_IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    /**
     * 默认允许的视频扩展名。
     */
    private static final Set<String> DEFAULT_ALLOWED_VIDEO_TYPES = Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv");

    /**
     * 默认最大文件大小，10MB。
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 默认缩略图宽度。
     */
    private static final int DEFAULT_THUMBNAIL_WIDTH = 200;

    /**
     * 默认缩略图高度。
     */
    private static final int DEFAULT_THUMBNAIL_HEIGHT = 200;

    private final S3Api s3Api;
    private final S3Properties s3Properties;

    /**
     * 上传文件，仅做基础校验。
     *
     * @param platform 平台标识
     * @param identity 业务标识
     * @param multipartFile 上传文件
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile) {
        return upload(platform, identity, multipartFile, false, false);
    }

    /**
     * 上传文件，并按需生成缩略图。
     *
     * @param platform 平台标识
     * @param identity 业务标识
     * @param multipartFile 上传文件
     * @param generateThumb 是否生成缩略图
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile, boolean generateThumb) {
        return upload(platform, identity, multipartFile, generateThumb, false);
    }

    /**
     * 上传文件完整入口。
     *
     * @param platform 平台标识
     * @param identity 业务标识
     * @param multipartFile 上传文件
     * @param generateThumb 是否生成缩略图
     * @param allowVideo 是否允许视频类型
     * @return 上传结果
     */
    public UploadResult upload(String platform, String identity, MultipartFile multipartFile, boolean generateThumb,
            boolean allowVideo) {
        validateFile(multipartFile, allowVideo);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            String filename = multipartFile.getOriginalFilename();
            String contentType = resolveContentType(multipartFile);
            long size = multipartFile.getSize();
            String objectKey = generateObjectKey(platform, identity, filename);

            UploadResult result = s3Api.upload(objectKey, inputStream, contentType, size);
            if (shouldGenerateThumbnail(generateThumb, filename)) {
                UploadResult thumbResult = generateAndUploadThumbnail(multipartFile, objectKey);
                result.setThumbPath(thumbResult.getUrl());
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量上传文件。
     *
     * @param platform 平台标识
     * @param identity 业务标识
     * @param multipartFiles 上传文件数组
     * @return 上传结果列表
     */
    public List<UploadResult> batchUpload(String platform, String identity, MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles).map(file -> upload(platform, identity, file)).toList();
    }

    /**
     * 生成并上传缩略图。
     *
     * @param multipartFile 原始文件
     * @param originalObjectKey 原始对象 key
     * @return 缩略图上传结果
     * @throws IOException 处理图片失败时抛出
     */
    private UploadResult generateAndUploadThumbnail(MultipartFile multipartFile, String originalObjectKey)
            throws IOException {
        String thumbObjectKey = generateThumbObjectKey(originalObjectKey);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(multipartFile.getInputStream())
                    .size(DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(outputStream);

            byte[] thumbData = outputStream.toByteArray();
            return s3Api.upload(thumbObjectKey, new ByteArrayInputStream(thumbData), "image/jpeg", thumbData.length);
        }
    }

    /**
     * 生成缩略图对象 key。
     *
     * @param originalObjectKey 原始对象 key
     * @return 缩略图对象 key
     */
    private String generateThumbObjectKey(String originalObjectKey) {
        int extensionIndex = originalObjectKey.lastIndexOf(".");
        if (extensionIndex < 0) {
            return originalObjectKey + "_thumb.jpg";
        }
        return originalObjectKey.substring(0, extensionIndex) + "_thumb.jpg";
    }

    /**
     * 校验上传文件。
     *
     * @param file 上传文件
     * @param allowVideo 是否允许视频类型
     */
    private void validateFile(MultipartFile file, boolean allowVideo) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        if (file.getSize() > resolveMaxFileSize()) {
            throw new IllegalArgumentException("文件大小不能超过 " + (resolveMaxFileSize() / 1024 / 1024) + "MB");
        }

        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (!resolveAllowedTypes(allowVideo).contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 解析允许上传的扩展名集合。
     *
     * @param allowVideo 是否允许视频类型
     * @return 允许的扩展名集合
     */
    private Set<String> resolveAllowedTypes(boolean allowVideo) {
        Set<String> configuredAllowedTypes = s3Properties.getAllowedFileTypes();
        if (configuredAllowedTypes != null && !configuredAllowedTypes.isEmpty()) {
            return configuredAllowedTypes;
        }

        Set<String> defaultAllowedTypes = new HashSet<>(DEFAULT_ALLOWED_IMAGE_TYPES);
        if (allowVideo) {
            defaultAllowedTypes.addAll(DEFAULT_ALLOWED_VIDEO_TYPES);
        }
        return defaultAllowedTypes;
    }

    /**
     * 解析最大文件大小。
     *
     * @return 最大文件大小
     */
    private long resolveMaxFileSize() {
        return s3Properties.getMaxFileSize() > 0 ? s3Properties.getMaxFileSize() : DEFAULT_MAX_FILE_SIZE;
    }

    /**
     * 判断是否需要生成缩略图。
     *
     * @param generateThumb 是否开启缩略图
     * @param filename 文件名
     * @return 是否生成缩略图
     */
    private boolean shouldGenerateThumbnail(boolean generateThumb, String filename) {
        return generateThumb && isImage(filename);
    }

    /**
     * 判断文件是否为图片类型。
     *
     * @param filename 文件名
     * @return 是否为图片
     */
    private boolean isImage(String filename) {
        if (filename == null) {
            return false;
        }
        return DEFAULT_ALLOWED_IMAGE_TYPES.contains(getFileExtension(filename).toLowerCase());
    }

    /**
     * 解析上传 Content-Type。
     *
     * @param multipartFile 上传文件
     * @return Content-Type
     */
    private String resolveContentType(MultipartFile multipartFile) {
        return StringUtils.defaultIfBlank(multipartFile.getContentType(), "application/octet-stream");
    }

    /**
     * 获取文件扩展名，不包含点号。
     *
     * @param filename 文件名
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 生成对象 key。
     *
     * @param platform 平台标识
     * @param identity 业务标识
     * @param filename 文件名
     * @return 对象 key
     */
    public String generateObjectKey(String platform, String identity, String filename) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String datePath = LocalDateTime.now().format(formatter);
        String uuid = IdUtil.simpleUUID();
        String extension = getFileExtension(filename);
        String suffix = extension.isEmpty() ? "" : "." + extension;
        return String.format("%s/%s/%s/%s%s", platform, datePath, identity, uuid, suffix);
    }
}
