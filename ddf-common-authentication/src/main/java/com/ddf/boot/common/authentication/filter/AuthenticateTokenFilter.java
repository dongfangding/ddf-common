package com.ddf.boot.common.authentication.filter;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.common.RequestHeader;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.consts.AuthenticateConstant;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.authentication.model.AuthenticateCheckResult;
import com.ddf.boot.common.authentication.model.UserClaim;
import com.ddf.boot.common.authentication.util.TokenUtil;
import com.ddf.boot.common.authentication.util.UserContextUtil;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.WebUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * 拦截请求处理用户认证信息
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Slf4j
public class AuthenticateTokenFilter extends HandlerInterceptorAdapter {

    public static final String BEAN_NAME = "authenticateTokenFilter";

    /**
     * 系统级别忽略的路径
     */
    private static final List<String> SYSTEM_IGNORE_PATH = Collections.unmodifiableList(Lists.newArrayList("/error"));

    @Autowired(required = false)
    private UserClaimService userClaimService;
    @Autowired
    private TokenCustomizeCheckService tokenCustomizeCheckService;
    @Autowired
    private AuthenticationProperties authenticateProperties;
    @Autowired
    private EnvironmentHelper environmentHelper;

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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath();
        // 解析请求头
        resolveHeader(request);
        if (SYSTEM_IGNORE_PATH.contains(path)) {
            return true;
        }
        String host = WebUtil.getHost();
        final String token = request.getHeader(authenticateProperties.getTokenHeaderName());
        request.setAttribute(AuthenticateConstant.CLIENT_IP, host);
        // 跳过忽略路径
        if (authenticateProperties.isIgnore(path)) {
            return true;
        }

        if (userClaimService == null) {
            throw new NoSuchBeanDefinitionException(UserClaimService.class);
        }
        // 填充认证接口前置属性
        userClaimService.storeRequest(request, host);

        // 校验并转换jws
        AuthInfo authInfo = checkAndParseAuthInfo(request, token);
        final UserClaim userClaim = authInfo.getUserClaim();
        UserClaim storeUserClaim = authInfo.getStoreUserClaim();

        // 预留认证通过后置接口
        userClaimService.afterVerifySuccess(userClaim);

        String userInfo = JsonUtil.asString(storeUserClaim);
        // 塞入最新用户数据
        UserContextUtil.setUserClaim(storeUserClaim);
        MDC.put(AuthenticateConstant.MDC_USER_ID, storeUserClaim.getUserId());
        MDC.put(AuthenticateConstant.MDC_TRACE_ID, generateTraceId(storeUserClaim.getUserId()));
        request.setAttribute(AuthenticateConstant.HEADER_USER, userInfo);
        return true;
    }

    /**
     * 生成traceId
     *
     * @param userId
     * @return
     */
    private String generateTraceId(String userId) {
        return String.join("-", userId, IdsUtil.getNextStrId());
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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable
            Exception ex) throws Exception {
        // 移除用户信息
        UserContextUtil.removeUserClaim();
        UserContextUtil.removeRequestHeader();
        MDC.remove(AuthenticateConstant.MDC_USER_ID);
        MDC.remove(AuthenticateConstant.MDC_TRACE_ID);
    }

    /**
     * 校验并转换用户信息
     *
     * @param request
     * @param tokenHeader
     * @return
     */
    private AuthInfo checkAndParseAuthInfo(HttpServletRequest request,  String tokenHeader) {
        UserClaim tokenUserClaim;
        String tokenPrefix = authenticateProperties.getTokenPrefix();
        if (tokenHeader == null || !tokenHeader.startsWith(tokenPrefix)) {
            throw new UnauthorizedException("token格式不合法！");
        }
        String token = tokenHeader.split(tokenPrefix)[1];

        UserClaim storeUser;
        if (authenticateProperties.isMock() && !environmentHelper.isProdProfile() &&
                CollUtil.isNotEmpty(authenticateProperties.getMockUserIdList()) && authenticateProperties.getMockUserIdList().contains(token)) {
            tokenUserClaim = UserClaim.mockUser(token);
            storeUser = userClaimService.getStoreUserInfo(tokenUserClaim);
        } else {
            AuthenticateCheckResult authenticateCheckResult = TokenUtil.checkToken(token);
            tokenUserClaim = authenticateCheckResult.getUserClaim();
            // 额外业务token校验规则
            storeUser = tokenCustomizeCheckService.customizeCheck(request, authenticateCheckResult);
        }
        return new AuthInfo().setRealToken(token)
                .setUserClaim(tokenUserClaim)
                .setStoreUserClaim(storeUser);
    }

    /**
     * 解析请求头
     *
     * @param request
     */
    public void resolveHeader(HttpServletRequest request) {
        UserContextUtil.setRequestHeaderContext(RequestHeader.builder()
                .sign(request.getHeader(RequestHeaderEnum.SIGN.getName()))
                .os(OsEnum.valueOf(request.getHeader(RequestHeaderEnum.OS.getName())))
                .imei(request.getHeader(RequestHeaderEnum.IMEI.getName()))
                .nonce(Long.parseLong(ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.NONCE.getName()), "0")))
                .version(Integer.parseInt(ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.VERSION.getName()), "0")))
                .longitude(new BigDecimal(ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.LONGITUDE.getName()), "0")))
                .latitude(new BigDecimal(ObjectUtils.defaultIfNull(request.getHeader(RequestHeaderEnum.LATITUDE.getName()), "0")))
                .build());
    }


    @Data
    @Accessors(chain = true)
    public static class AuthInfo {

        /**
         * 真实token内容
         */
        private String realToken;

        /**
         * 解析后自定义对象
         */
        private UserClaim userClaim;

        /**
         * 根据解析对象接口实现返回最新的UserClaim对象信息
         */
        private UserClaim storeUserClaim;
    }
}
