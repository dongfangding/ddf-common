package com.ddf.boot.common.core.util;

import com.ddf.boot.common.api.model.common.RequestContext;

/**
 * <p>request context 上下文 工具类</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/01/14 17:16
 */
public class RequestContextUtil {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = ThreadLocal.withInitial(RequestContext::new);

    /**
     * 获取当前header上下文信息
     *
     * @return
     */
    public static RequestContext getRequestContext() {
        return REQUEST_CONTEXT.get();
    }

    /**
     * 设置当前header上下文信息
     *
     * @param headerContext
     */
    public static void setRequestContext(RequestContext headerContext) {
        REQUEST_CONTEXT.set(headerContext);
    }
}
