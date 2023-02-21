package com.ddf.boot.common.trace.context;

import lombok.Data;

/**
 * <p>存储请求身份上下文</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/20 17:48
 */
@Data
public class QlTraceContext {

    public static final String USER_ID = "uid";
    public static final String TRACE_ID = "traceId";

    private static ThreadLocal<ContextDomain> IDENTITY_THREAD_LOCAL = ThreadLocal.withInitial(() -> ContextDomain.builder().build());

    public static ContextDomain getContextDomain() {
        return IDENTITY_THREAD_LOCAL.get();
    }

    public static void setContextDomain(ContextDomain contextDomain) {
        IDENTITY_THREAD_LOCAL.set(contextDomain);
    }

    public static void remove() {
        IDENTITY_THREAD_LOCAL.remove();
    }

    public static Integer getUid() {
        return IDENTITY_THREAD_LOCAL.get().getIdentity().getUid();
    }

    public static String getTraceId() {
        return IDENTITY_THREAD_LOCAL.get().getTraceId();
    }
}
