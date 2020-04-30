package com.ddf.boot.common.websocket.model.ws;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 支付方式应用到账消息报文主体内容
 *

 * @date 2019/9/21 15:28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("应用到账消息报文主体内容")
public class TopicMessage implements Serializable {

    private static final long serialVersionUID = 6290652775704591461L;

    @ApiModelProperty("到账消息内容")
    private String content;

    @ApiModelProperty("订单Id,唯一主键，用来去重")
    private String orderId;

    @ApiModelProperty("订单时间，时间戳")
    private Long orderTime;
}
