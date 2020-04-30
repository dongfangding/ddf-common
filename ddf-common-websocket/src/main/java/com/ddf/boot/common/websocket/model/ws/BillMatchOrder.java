package com.ddf.boot.common.websocket.model.ws;

import com.ddf.boot.common.websocket.enumerate.BillTypeEnum;
import com.ddf.boot.common.websocket.enumerate.OutQRCodeTypeEnum;
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
 * 支付方式账单数据
 *
 * @author dongfang.ding
 * @date 2019/9/23 11:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("支付方式账单数据")
public class BillMatchOrder implements Serializable {

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
    private String tradeNo;

    @ApiModelProperty("订单金额")
    @JsonProperty("money")
    private BigDecimal amount;

    @ApiModelProperty("订单备注")
    @JsonProperty("mark")
    private String mark;

    @ApiModelProperty("付款卡号")
    private String targetAccountNo;

    @ApiModelProperty("付款人")
    private String targetAccountName;

    @ApiModelProperty("订单时间")
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
        if (StringUtils.isNotBlank(targetAccountName)) {
            sbl.append("对方姓名[").append(targetAccountName).append("],");
        }
        if (StringUtils.isNotBlank(targetAccountNo)) {
            sbl.append("对方账号[").append(targetAccountNo).append("]。");
        }
        if (StringUtils.isNotBlank(mark)) {
            sbl.append("备注: ").append(mark).append("。");
        }
        return sbl.toString();
    }
}
