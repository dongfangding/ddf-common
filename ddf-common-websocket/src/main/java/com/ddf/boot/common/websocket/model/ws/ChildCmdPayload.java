package com.ddf.boot.common.websocket.model.ws;

import com.ddf.boot.common.websocket.enumerate.CmdEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 子命令码业务数据类
 *
 * @author dongfang.ding
 * @date 2019/9/25 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("子命令码业务数据类")
@Accessors(chain = true)
public class ChildCmdPayload implements Serializable {
    private static final long serialVersionUID = 2977718922768592210L;

    @ApiModelProperty(value = "子命令码", required = true)
    private CmdEnum.ChildEnum childCmd;

    @ApiModelProperty("信息，可为空")
    private String message;
}
