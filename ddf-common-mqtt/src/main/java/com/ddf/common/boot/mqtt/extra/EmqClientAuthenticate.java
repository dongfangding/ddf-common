package com.ddf.common.boot.mqtt.extra;

import com.ddf.common.boot.mqtt.model.request.emq.EmqAuthenticateRequest;
import com.ddf.common.boot.mqtt.model.response.emq.EmqClientAuthenticateResponse;

/**
 * <p客户端自己根据用户信息判断是否是授权登录用户</p >
 *
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/03/22 15:22
 */
public interface EmqClientAuthenticate {

    /**
     * 客户端自己的校验规则
     * 比如需要校验客户端的权限，那么会传入token, 要校验token信息是否是登录用户，匹配的话就算校验通过
     *
     * @param request
     * @return
     */
    EmqClientAuthenticateResponse authenticate(EmqAuthenticateRequest request);
}
