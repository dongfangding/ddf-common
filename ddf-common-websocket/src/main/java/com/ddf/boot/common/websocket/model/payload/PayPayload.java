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
 * 转账提现主体业务数据
 *
 * @author dongfang.ding
 * @date 2019/10/17 17:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("转账提现主体业务数据")
@Accessors(chain = true)
public class PayPayload implements Serializable {

    private static final long serialVersionUID = -5516231773916010002L;

    @ApiModelProperty("收款人")
    private String receiver;

    @ApiModelProperty("收款人银行卡号")
    private String receiverBankCardNo;

    @ApiModelProperty("转账金额")
    private BigDecimal amount;

    @ApiModelProperty("付款卡号")
    private String payBankCardNo;

    @ApiModelProperty("支付密码")
    private String payPassword;

    /**
     * 服务端会去处理尽量不会将同一个订单的提现报文多次发送，但客户端也需要根据订单id来避免针对同一个订单的多次转账
     */
    @ApiModelProperty("订单号，")
    private String orderId;
}
