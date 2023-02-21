package com.ddf.boot.common.trace.filter;

import com.ddf.boot.common.trace.context.ContextDomain;
import com.ddf.boot.common.trace.context.Identity;
import com.ddf.boot.common.trace.context.QlTraceContext;
import com.ddf.boot.common.trace.context.TraceProcess;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

/**
 * 为dubbo隐式传参设置traceId, 这里永远都是设置值， 因为生成方式会从别的地方设置
 *
 * @author dongfang.ding
 * @date 2021/8/23 16:47
 **/
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
@Slf4j
public class QlTraceIdFilter implements Filter {

    /**
     * 将应用身份上下文信息通过隐式传参传递或者解析出来
     *
     * @param invoker
     * @param invocation
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        final RpcContext rpcContext = RpcContext.getContext();
        if (rpcContext.isConsumerSide()) {
            final ContextDomain contextDomain = QlTraceContext.getContextDomain();
            final Identity identity = ObjectUtils.defaultIfNull(contextDomain.getIdentity(), Identity.empty());
            rpcContext.setAttachment(QlTraceContext.TRACE_ID, contextDomain.getTraceId());
            // 应用没有设置身份上下文，可以供统一获取用户信息，暂时不处理
            rpcContext.setAttachment(QlTraceContext.USER_ID, Objects.isNull(identity.getUid()) ? "" : identity.getUid() + "");
            rpcContext.setAttachment("os", identity.getOs());
            rpcContext.setAttachment("imei", identity.getImei());
            // 传递处理过程中的参数
            final TraceProcess process = ObjectUtils.defaultIfNull(contextDomain.getTraceProcess(), TraceProcess.defaultInstance());
            rpcContext.setAttachment("traceDepthNum", String.valueOf(process.getTraceDepthNum()));
            rpcContext.setAttachment("traceFlag", String.valueOf(process.isTraceFlag()));
        } else {
            final String traceId = rpcContext.getAttachment(QlTraceContext.TRACE_ID);
            final String uid = rpcContext.getAttachment(QlTraceContext.USER_ID);
            final String os = rpcContext.getAttachment("os");
            final String imei = rpcContext.getAttachment("imei");
            final String num = StringUtils.defaultIfBlank(rpcContext.getAttachment("traceDepthNum"), "0");
            final String traceFlag = StringUtils.defaultIfBlank(rpcContext.getAttachment("traceFlag"), "false");
            final ContextDomain contextDomain = ContextDomain.builder()
                    .traceId(traceId)
                    .identity(Identity.builder()
                            .uid(StringUtils.isNotBlank(uid) ? Integer.parseInt(uid) : null)
                            .os(os)
                            .imei(imei)
                            .build())
                    .traceProcess(TraceProcess.builder()
                            .traceDepthNum(Integer.parseInt(num))
                            .traceFlag(Boolean.parseBoolean(traceFlag))
                            .build())
                    .build();
            // TODO 什么时机清除MDC和上下文呢？？
            QlTraceContext.setContextDomain(contextDomain);
            MDC.put(QlTraceContext.USER_ID, uid);
            MDC.put(QlTraceContext.TRACE_ID, traceId);
        }
        return invoker.invoke(invocation);
    }
}
