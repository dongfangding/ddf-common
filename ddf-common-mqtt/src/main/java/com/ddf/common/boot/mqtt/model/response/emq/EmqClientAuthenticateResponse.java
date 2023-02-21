package com.ddf.common.boot.mqtt.model.response.emq;

import java.io.Serializable;
import lombok.Data;

/**
 * <p>emq http连接认证和ACL响应</p >
 *
 * 这个响应是给应用方自己实现返回的，因为该模块无法得知每个应用自己的认证方式以及错误消息
 *
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 13:54
 */
@Data
public class EmqClientAuthenticateResponse implements Serializable {

    private static final long serialVersionUID = 6593260244631866442L;

    /**
     * 是否校验通过
     */
    private boolean result;

    /**
     * 可以放错误原因
     */
    private String msg;
}
