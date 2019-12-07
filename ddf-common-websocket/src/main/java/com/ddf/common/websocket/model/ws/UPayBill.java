package com.ddf.common.websocket.model.ws;

import com.ddf.common.websocket.enumerate.BillTypeEnum;
import com.ddf.common.websocket.enumerate.OutQRCodeTypeEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 云闪付账单数据
 *


 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("云闪付账单数据")
public class UPayBill implements Serializable {

    private static final long serialVersionUID = 6290652775704591461L;

    /**
     * 状态 失败
     */
    public static final Integer STATUS_FAILURE = 0;

    /**
     * 状态 成功
     */
    public static final Integer STATUS_SUCCESS = 1;

    @ApiModelProperty("订单号")
    @JsonProperty("tradeno")
    private String tradeNo;

    @ApiModelProperty("订单金额")
    @JsonProperty("money")
    private BigDecimal amount;

    @ApiModelProperty("订单备注")
    @JsonProperty("mark")
    private String mark;

    @ApiModelProperty("付款卡号")
    @JsonProperty("payaccountno")
    private String payNo;

    @ApiModelProperty("付款人")
    @JsonProperty("payaccountname")
    private String payName;

    @ApiModelProperty("订单时间")
    @JsonProperty("ordertime")
    private String orderTime;

    @ApiModelProperty("状态， 0 失败 1 成功， 只有成功的数据才有效")
    private Integer status;

    @ApiModelProperty("0-普通码 1-商户码")
    private OutQRCodeTypeEnum qrCodeType;

    @ApiModelProperty("是收入还是支出 income 收入 pay 支出")
    private BillTypeEnum billType;


    @Override
    public String toString() {
        StringBuilder sbl = new StringBuilder(100);
        sbl.append("订单");
        if (StringUtils.isNotBlank(orderTime)) {
            sbl.append("于").append(orderTime);
        }
        if (amount != null) {
            sbl.append("收入金额").append(amount).append("元。");
        }
        if (StringUtils.isNotBlank(payName)) {
            sbl.append("付款人[").append(payName).append("],");
        }
        if (StringUtils.isNotBlank(payNo)) {
            sbl.append("付款卡号[").append(payNo).append("]。");
        }
        if (StringUtils.isNotBlank(mark)) {
            sbl.append("备注: ").append(mark).append("。");
        }
        return sbl.toString();
    }
}
