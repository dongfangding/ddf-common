package com.ddf.boot.common.trace.util;

import java.util.UUID;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2021/08/20 17:48
 */
public class TraceContextUtil {

    private TraceContextUtil() {}

    /**
     * 获取skyWalking生成的traceId，这样日志可以和skyWalking的追踪对应起来
     *
     * @return
     */
    public static String getSkyWalkingTraceId() {
        return TraceContext.traceId();
    }

    /**
     * 默认traceId
     *
     * @return
     */
    public static String defaultTraceId() {
        return System.currentTimeMillis() + "." + UUID.randomUUID();
    }
}
