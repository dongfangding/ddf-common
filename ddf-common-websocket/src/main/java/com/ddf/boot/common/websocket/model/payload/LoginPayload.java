package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 对设备下发登录指令业务数据类
 *
 * @author dongfang.ding
 * @date 2019/9/25 9:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("支付方式登录指令业务数据类")
@Accessors(chain = true)
public class LoginPayload implements Serializable {
    private static final long serialVersionUID = 2977718922768592210L;

    @ApiModelProperty("账户名")
    private String accountName;

    @ApiModelProperty("登录密码")
    private String loginPassword;
}
