package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 切换IP/GPS指定数据格式
 *
 * @author dongfang.ding
 * @date 2019/8/24 13:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("切换IP/GPS指定数据格式")
@Accessors(chain = true)
public class SwitchIpGpsPayload implements Serializable {

    private static final long serialVersionUID = -6120106240537894085L;

    @ApiModelProperty("IP")
    private String ip;

    @ApiModelProperty("端口")
    private String port;

    @ApiModelProperty("经度")
    private String longitude;

    @ApiModelProperty("纬度")
    private String latitude;

    @ApiModelProperty("IP/经纬度对应区域")
    private String address;

}