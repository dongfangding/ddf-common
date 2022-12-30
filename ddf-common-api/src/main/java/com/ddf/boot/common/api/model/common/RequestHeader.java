package com.ddf.boot.common.api.model.common;

import com.ddf.boot.common.api.enums.OsEnum;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>请求头对象</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/12/30 13:58
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestHeader implements Serializable {

    private static final long serialVersionUID = 1L;

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
