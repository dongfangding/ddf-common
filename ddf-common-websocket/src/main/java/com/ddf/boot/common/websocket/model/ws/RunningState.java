package com.ddf.boot.common.websocket.model.ws;

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

    @ApiModelProperty("支付方式应用相关状态")
    private App app;


    @ApiModelProperty("指令码运行状态")
    private CmdState cmdState;

    /**
     * 云闪付运行状态
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("支付方式应用相关状态")
    public static class App implements Serializable {
        private static final long serialVersionUID = 7875158127229269039L;

        @ApiModelProperty("运行状态")
        private Integer runningState;
        @ApiModelProperty("登录状态")
        private Integer loginState;
    }

    /**
     * 指令码运行状态
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel("指令码运行状态")
    public static class CmdState implements Serializable  {

        private static final long serialVersionUID = 668373605225693998L;

        @ApiModelProperty("转账脚本运行状态")
        private Integer cmdPayState;
    }
}
