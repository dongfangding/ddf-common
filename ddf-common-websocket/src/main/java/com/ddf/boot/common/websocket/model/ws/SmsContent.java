package com.ddf.boot.common.websocket.model.ws;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 短信内容类
 *


 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("短信内容类")
public class SmsContent implements Serializable {

    private static final long serialVersionUID = -4100508553123827398L;

    @ApiModelProperty("短信发送方标识")
    private String credit;

    @ApiModelProperty("短信id")
    private String primaryKey;

    @ApiModelProperty("短信内容")
    private String content;

    @ApiModelProperty("收件时间，毫秒值")
    private Long receiveTime;
}
