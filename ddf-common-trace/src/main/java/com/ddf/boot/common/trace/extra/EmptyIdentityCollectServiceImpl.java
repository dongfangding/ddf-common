package com.ddf.boot.common.trace.extra;

import com.ddf.boot.common.trace.context.Identity;
import javax.servlet.http.HttpServletRequest;

/**
 * 提供一个获取用户信息的默认实现
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/24 10:36
 */
public class EmptyIdentityCollectServiceImpl implements IdentityCollectService {

    /**
     * 获取用户信息
     *
     * @param request
     * @return
     */
    @Override
    public Identity get(HttpServletRequest request) {
        return Identity.empty();
    }
}
