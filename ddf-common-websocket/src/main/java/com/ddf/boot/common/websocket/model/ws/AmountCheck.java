package com.ddf.boot.common.websocket.model.ws;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 金额校准数据
 *

 * @date 2019/10/17 13:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("金额校准数据")
public class AmountCheck implements Serializable {

    private static final long serialVersionUID = 42L;

    private App app;

    private List<BankCard> bankCardList;


    /**
     * 支付方式相关限额信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("支付方式相关限额信息")
    public static class App {

        @ApiModelProperty("支付方式账号")
        private String accountName;

        @ApiModelProperty("单笔限额")
        private BigDecimal singleQuota;

        @ApiModelProperty("单日限额")
        private BigDecimal dailyQuota;

        @ApiModelProperty("支付方式钱包余额")
        private BigDecimal balance;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("银行卡相关金额数据")
    public static class BankCard {

        @ApiModelProperty("银行卡号")
        private String cardNo;

        @ApiModelProperty("单笔限额")
        private BigDecimal singleQuota;

        @ApiModelProperty("单日限额")
        private BigDecimal dailyQuota;

        @ApiModelProperty("银行卡余额")
        private BigDecimal balance;
    }
}
