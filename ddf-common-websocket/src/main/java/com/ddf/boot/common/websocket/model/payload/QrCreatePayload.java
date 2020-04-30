package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 二维码生成业务数据类
 *
 * @author dongfang.ding
 * @date 2019/8/21 17:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("二维码生成指令业务数据类")
@Accessors(chain = true)
public class QrCreatePayload implements Serializable {

    private static final long serialVersionUID = -1932300189664379647L;

    /** 个人码 */
    public static final Integer CODE_TYPE_PERSONAL = 0;
    /** 商户码 */
    public static final Integer CODE_TYPE_BUSINESS = 1;


    @ApiModelProperty("金额")
    private BigDecimal amount;

    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    @ApiModelProperty(value = "二维码类型,0-个人码 1-商户码", allowableValues = "0,1")
    private Integer qrCodeType;

}
