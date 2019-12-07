package com.ddf.common.websocket.model.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("金额校准数据")
public class AmountCheck implements Serializable {

    private static final long serialVersionUID = 42L;

    @JsonProperty("UNIONPAY")
    private UnionPay unionPay;

    private List<BankCard> bankCardList;


    /**
     * 云闪付相关金额数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("云闪付相关金额数据")
    public static class UnionPay {

        @ApiModelProperty("云闪付账号")
        private String accountName;

        @ApiModelProperty("单笔限额")
        private BigDecimal singleQuota;

        @ApiModelProperty("单日限额")
        private BigDecimal dailyQuota;
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
