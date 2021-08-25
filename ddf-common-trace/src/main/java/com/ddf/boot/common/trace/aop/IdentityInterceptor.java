package com.ddf.boot.common.trace.aop;

import com.ddf.boot.common.trace.context.ContextDomain;
import com.ddf.boot.common.trace.context.QlTraceContext;
import com.ddf.boot.common.trace.context.TraceProcess;
import com.ddf.boot.common.trace.extra.IdentityCollectService;
import com.ddf.boot.common.trace.util.TraceContextUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 处理trace上下文
 *
 * @author snowball
 * @date 2021/8/20 18:51
 **/
@Slf4j
public class IdentityInterceptor extends HandlerInterceptorAdapter {

    private final IdentityCollectService identityCollectService;

    public IdentityInterceptor(IdentityCollectService identityCollectService) {
        this.identityCollectService = identityCollectService;
    }


    /**
     * 解析用户相关信息放入当前线程上下文
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = StringUtils.defaultIfBlank(TraceContextUtil.getSkyWalkingTraceId(), TraceContextUtil.defaultTraceId());
        final ContextDomain contextDomain = ContextDomain.builder()
                .traceId(traceId)
                .identity(identityCollectService.get(request))
                .traceProcess(TraceProcess.defaultInstance())
                .build();
        QlTraceContext.setContextDomain(contextDomain);
        MDC.put(QlTraceContext.USER_ID, contextDomain.getIdentity().getUid() + "");
        MDC.put(QlTraceContext.TRACE_ID, traceId);
        return Boolean.TRUE;
    }
 
    /**
     * 释放
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        QlTraceContext.remove();
        MDC.remove(QlTraceContext.USER_ID);
        MDC.remove(QlTraceContext.TRACE_ID);
        super.afterCompletion(request, response, handler, ex);
    }
}
