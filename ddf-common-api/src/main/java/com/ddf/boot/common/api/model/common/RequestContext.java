package com.ddf.boot.common.api.model.common;

import com.ddf.boot.common.api.enums.OsEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext implements Serializable {

    private static final long serialVersionUID = -7528108356364083934L;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 请求路径
     */
    private String requestUri;

    /**
     * 签名字段
     */
    private String sign;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 设备号
     */
    private String imei;

    /**
     * 防重放，时间毫秒值
     */
    private Long nonce;

    /**
     * 客户端类型
     */
    private OsEnum os;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;
}
