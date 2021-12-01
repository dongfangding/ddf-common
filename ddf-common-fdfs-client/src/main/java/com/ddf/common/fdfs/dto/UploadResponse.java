package com.ddf.common.fdfs.dto;

import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.github.tobato.fastdfs.domain.fdfs.DefaultThumbImageConfig;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.upload.ThumbImage;
import lombok.Data;

/**
 * <p>上传后响应</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/11/30 20:04
 */
@Data
public class UploadResponse {

    public static final DefaultThumbImageConfig DEFAULT_THUMB_IMAGE_CONFIG =
            (DefaultThumbImageConfig) SpringContextHolder.getBean("defaultThumbImageConfig");

    /**
     * 组name
     */
    private String group;

    /**
     * 上传后路径，取出group name
     */
    private String path;

    /**
     * 上传后路径，带group name
     */
    private String fullPath;

    /**
     * 缩略图路径， 带group name
     * @see com.github.tobato.fastdfs.domain.upload.ThumbImage#getThumbImagePath(java.lang.String)
     * @see DefaultThumbImageConfig#getThumbImagePath(java.lang.String)
     */
    private String thumbPath;


    /**
     * 只适用于使用默认的缩略图配置
     *
     * @param storePath
     * @return
     */
    public static UploadResponse fromStorePath(StorePath storePath) {
        final UploadResponse response = new UploadResponse();
        response.setGroup(storePath.getGroup());
        response.setPath(storePath.getPath());
        response.setFullPath(storePath.getFullPath());
        response.setThumbPath(DEFAULT_THUMB_IMAGE_CONFIG.getThumbImagePath(storePath.getFullPath()));
        return response;
    }

    /**
     * 构造图片上传后返回对象
     *
     * @param storePath
     * @return
     */
    public static UploadResponse fromStorePath(StorePath storePath, ThumbImage thumbImage) {
        final UploadResponse response = new UploadResponse();
        response.setGroup(storePath.getGroup());
        response.setPath(storePath.getPath());
        response.setFullPath(storePath.getFullPath());
        response.setThumbPath(thumbImage.getThumbImagePath(storePath.getFullPath()));
        return response;
    }
}
