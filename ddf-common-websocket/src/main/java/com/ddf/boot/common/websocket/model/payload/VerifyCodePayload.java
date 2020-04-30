package com.ddf.boot.common.websocket.model.payload;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 验证码推送主体数据
 *
 * @author dongfang.ding
 * @date 2019/9/27 20:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("验证码推送主体数据")
@Accessors(chain = true)
public class VerifyCodePayload implements Serializable {

    private static final long serialVersionUID = -8069195136127549902L;

    @ApiModelProperty("验证码类型")
    private Type type;

    @ApiModelProperty("验证码")
    private String verifyCode;

    public enum Type {
        /** 登录验证码 */
        LOGIN,

        /** 转账验证码 */
        PAY,

        /**
         * 注册验证码
         */
        REGISTRY
    }

}
