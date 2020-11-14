package com.ddf.boot.common.ext.oss.dto;

import com.aliyun.oss.OSS;
import com.ddf.boot.common.ext.oss.config.StsTokenResponse;
import lombok.Builder;
import lombok.Data;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2020/11/14 18:52
 */
@Data
@Builder
public class StsOssTransfer {

    private OSS oss;

    private StsTokenResponse stsTokenResponse;

}
