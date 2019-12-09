package com.ddf.boot.common.websocket.model.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 设备运行状态主体报文数据
 *


 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("设备运行状态")
public class RunningState implements Serializable {

    private static final long serialVersionUID = 1226849750814326104L;

    @JsonProperty("UNIONPAY")
    @ApiModelProperty("云闪付应用相关状态")
    private UnionPay unionPay;


    /**
     * 云闪付运行状态
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("云闪付应用相关状态")
    public static class UnionPay {
        @ApiModelProperty("运行状态")
        private Integer runningState;
        @ApiModelProperty("登录状态")
        private Integer loginState;
    }
}
