package com.ddf.boot.common.core.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>存储请求相关的参数的上下文对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/01/14 17:17
 */
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class RequestContext implements Serializable {

    private static final long serialVersionUID = -7528108356364083934L;

    /**
     * token
     */
    private String token;

    /**
     * 签名参数
     */
    private String sign;

    /**
     * 客户端操作系统， 如 pc/ios/android
     */
    private String os;

    /**
     * 客户端设备唯一标识，
     * 主要是移动端设备，需要识别到具体设备号的时候
     */
    private String imei;

    /**
     * 客户端ip
     */
    private String clientIp;
}
