package com.ddf.boot.common.websocket.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 设置商户码
 *
 * @author dongfang.ding
 * @date 2019/8/24 13:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SetMerchantQrcodeBankPayload implements Serializable {

    private static final long serialVersionUID = 9008298183214470633L;

    /**
     * 商户码名称
     */
    private String merchantQrcodeName;

    /**
     * 要开通商户码的银行卡
     */
    private String bankCardNumber;

}
