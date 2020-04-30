package com.ddf.boot.common.websocket.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 设置支付密码指令数据格式
 *
 * @author dongfang.ding
 * @date 2019/8/24 13:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SetPayPasswordPayload implements Serializable {
    private static final long serialVersionUID = -3021400145435475474L;

    /**
     * 支付密码
     */
    private String payPassword;

}
