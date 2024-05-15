package com.banma.sunshine.authentication.annotation.filter;


import com.banma.sunshine.authentication.annotation.config.AuthenticateConstant;
import com.banma.sunshine.authentication.annotation.util.UserContextUtil;
import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

/**
 * 拦截请求处理用户认证信息
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Slf4j
@Component
public class ServerAuthenticateTokenFilter implements HandlerInterceptor {

    /**
     * 前置校验
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 解析请求头
        resolveRequestContext(request);
        MDC.put(AuthenticateConstant.MDC_USER_ID, UserContextUtil.getUserId());
        MDC.put(
                AuthenticateConstant.MDC_TRACE_ID,
                request.getHeader(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName())
        );
        MDC.put(AuthenticateConstant.MDC_CLIENT_IP, UserContextUtil.getClientIpFromGateway());
        MDC.put(AuthenticateConstant.MDC_IMEI, UserContextUtil.getImei());
        return true;
    }

    /**
     * 执行器结束
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 移除用户信息
        UserContextUtil.removeRequestContext();
        MDC.remove(AuthenticateConstant.MDC_USER_ID);
        MDC.remove(AuthenticateConstant.MDC_TRACE_ID);
        MDC.remove(AuthenticateConstant.MDC_CLIENT_IP);
        MDC.remove(AuthenticateConstant.MDC_IMEI);
    }

    /**
     * 解析请求上下文
     *
     * @param request
     */
    public void resolveRequestContext(HttpServletRequest request) {
        UserContextUtil.setRequestContext(RequestContext.builder()
                .sign(request.getHeader(RequestHeaderEnum.SIGN.getName()))
                .os(OsEnum.resolve(request.getHeader(RequestHeaderEnum.OS.getName())))
                .channel(request.getHeader(RequestHeaderEnum.CHANNEL.getName()))
                .imei(request.getHeader(RequestHeaderEnum.IMEI.getName()))
                .nonce(Long.parseLong(
                        ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.NONCE.getName()), "0")))
                .version(ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.VERSION.getName()), "1.0.0"))
                .versionCode(Long.parseLong(
                        ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.VERSION_CODE.getName()), "0")))
                .longitude(new BigDecimal(
                        ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.LONGITUDE.getName()), "0")))
                .latitude(new BigDecimal(
                        ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.LATITUDE.getName()), "0")))
                .requestUri(request.getRequestURI())
                .clientIp(request.getHeader(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName()))
                .clientIpFromGateway(request.getHeader(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName()))
                .userIdFromGateway(request.getHeader(RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName()))
                .build());
    }
}
