package com.ddf.boot.common.s3.model;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

/**
 * 预签名 URL 结果.
 *
 * @author snowball
 */
@Data
@Builder
public class PresignedUrlResult {

    /**
     * 预签名 URL.
     */
    private String url;

    /**
     * 对象 Key.
     */
    private String objectKey;

    /**
     * 过期时间.
     */
    private Instant expiresAt;

}
