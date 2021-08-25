package com.ddf.boot.common.trace.extra;

import com.ddf.boot.common.trace.context.Identity;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>获取用户信息接口层， 因为多个应用获取方式不一样，定义接口，由应用方提供信息
 * 应用放保证提供信息准确、可用，当前模块不负责校验，也无法校验
 * </p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/24 10:36
 */
public interface IdentityCollectService {

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    Identity get(HttpServletRequest request);
}
