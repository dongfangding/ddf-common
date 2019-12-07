package com.ddf.common.websocket.model.ws;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 验证码推送主体数据
 *
 * @author dongfang.ding
 * @date 2019/12/7 0007 21:09
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("验证码推送主体数据")
@Accessors(chain = true)
public class VerifyCodePayload {

    private static final long serialVersionUID = -8069195136127549902L;

    @ApiModelProperty("验证码类型")
    private Type type;

    @ApiModelProperty("验证码")
    private String verifyCode;

    public enum Type {
        /** 云闪付登录验证码 */
        union_pay_login,

        /** 云闪付转账验证码 */
        uinion_pay
    }
}
