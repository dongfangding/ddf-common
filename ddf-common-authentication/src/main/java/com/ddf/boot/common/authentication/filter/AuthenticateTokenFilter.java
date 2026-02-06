package com.ddf.boot.common.authentication.filter;

import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.consts.AuthenticateConstant;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.authentication.util.UserContextUtil;
import com.ddf.boot.common.core.authentication.TokenUtil;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.util.GlobalAntMatcher;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.core.util.SignatureUtil;
import com.ddf.boot.common.mvc.util.WebUtil;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截请求处理用户认证信息
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Slf4j
public class AuthenticateTokenFilter implements HandlerInterceptor {

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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String url = request.getServletPath();
        // 通用请求头解析
        final Map<String, String> clientHeaderMap = resolveClientHeaders(request, null);
        // 自定义请求头解析， 处理过程中可以额外添加请求头，最终会被统一添加到请求头中
        final Map<String, String> customizeHeaderMap = new HashMap<>();
        if (SYSTEM_IGNORE_PATH.contains(url)) {
            return true;
        }
        // 开放接口校验，比如提供给外部的回调接口
        final RequestContext requestContext = UserContextUtil.getRequestContext();
        final List<String> openIgnores = authenticateProperties.getOpenIgnores();
        if (GlobalAntMatcher.match(openIgnores, url)) {
            return true;
        }
        // 必传请求头校验， 放在开放接口校验之后
        final Map<String, RequestHeaderEnum> requiredClientHeaders = RequestHeaderEnum.getRequiredClientHeaders();
        for (String name : requiredClientHeaders.keySet()) {
            if (StringUtils.isBlank(request.getHeader(name))) {
                throw new BusinessException(BaseErrorCallbackCode.ILLEGAL_REQUEST);
            }
        }
        if (userClaimService == null) {
            throw new NoSuchBeanDefinitionException(UserClaimService.class);
        }

        // token校验前置校验
        userClaimService.beforeTokenVerify(request, response, clientHeaderMap, customizeHeaderMap);

        String clientIp = requestContext.getClientIp();
        final String token = request.getHeader(authenticateProperties.getTokenHeaderName());
        request.setAttribute(AuthenticateConstant.CLIENT_IP, clientIp);
        final List<String> ignores = authenticateProperties.getIgnores();

        UserClaim userClaim = null;
        // 内部接口白名单
        if (!GlobalAntMatcher.match(ignores, url)) {
            try {
                userClaim = checkAndParseAuthInfo(request, token);
            } catch (BaseException e) {
                throw new BusinessException(e.getBaseCallbackCode());
            } catch (Exception e) {
                throw new ServerErrorException(BaseErrorCallbackCode.SERVER_ERROR);
            }
            if (Objects.isNull(userClaim)) {
                throw new BusinessException(BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
            }
        } else {
            userClaim = UserClaim.getDefaultUser();
        }
        // 添加服务端请求头
        clientHeaderMap.putAll(resolveServerHeaders(request, userClaim));
        // 填充认证接口前置属性
        userClaimService.storeRequest(request, clientIp);

        // 预留认证通过后置接口
        userClaimService.afterVerifySuccess(request, userClaim, clientHeaderMap, customizeHeaderMap);

        // 重放简单校验
        final long nonce = Long.parseLong(Objects.requireNonNull(request.getHeader(RequestHeaderEnum.NONCE.getName())));
        final long currentTimeMillis = System.currentTimeMillis();
        if (nonce < currentTimeMillis - TimeUnit.MINUTES.toMillis(5)
                || nonce > currentTimeMillis + TimeUnit.MINUTES.toMillis(5)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
        }
        // 签名校验
        final String contentType = request.getContentType();
        final MediaType mediaType = MediaType.parseMediaType(contentType);
        // 适合 JSON 和 Form 提交的请求
        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)
                || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            resolveBodySignData(request);
        } else {
            resolveQueryParamsSignData(request);
        }

        final HashMap<String, String> allHeaderMap = new HashMap<>(clientHeaderMap);
        allHeaderMap.putAll(customizeHeaderMap);
        // 自定义token校验

        // 塞入最新用户数据
        UserContextUtil.setUserClaim(userClaim);
        if (Objects.nonNull(userClaim)) {
            MDC.put(AuthenticateConstant.MDC_USER_ID, userClaim.getUserId());
            String userInfo = JsonUtil.asString(userClaim);
            request.setAttribute(AuthenticateConstant.HEADER_USER, userInfo);
        }
        MDC.put(AuthenticateConstant.MDC_TRACE_ID, generateTraceId(userClaim.getUserId()));
        return true;
    }


    /**
     * body传参参数校验
     *
     * @param request
     */
    private void resolveBodySignData(HttpServletRequest request) {
        final String body = WebUtil.readBody(request);
        Map dataMap = StringUtils.isNotBlank(body) ? JsonUtil.toBean(body, Map.class) : new HashMap();
        validSign(request, dataMap);
    }


    /**
     * QueryString传参签名校验
     *
     * @param request
     */
    private void resolveQueryParamsSignData(HttpServletRequest request) {
        final Map<String, Object> data = request
                .getParameterMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, val -> val.getValue()[0]));
        validSign(request, data);
    }

    /**
     * 验签
     *
     * @param data
     * @return
     */
    private void validSign(HttpServletRequest request, Map<String, Object> data) {
        // 把请求头中的nonce也加入到加签规则字段中
        data.put(RequestHeaderEnum.NONCE.getName(), request.getHeader(RequestHeaderEnum.NONCE.getName()));
        final String sign = request.getHeader(RequestHeaderEnum.SIGN.getName());
        if (!authenticateProperties.isSignEnabled()) {
            return;
        }
        if (!(authenticateProperties.isMockSignEnabled() && Objects.equals(authenticateProperties.getMockSign(), sign))
                && !SignatureUtil.verifySelfSignature(data, sign, authenticateProperties.getSignSecret())) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_ERROR);
        }
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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
        // 移除用户信息
        UserContextUtil.removeUserClaim();
        UserContextUtil.removeRequestContext();
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
    private UserClaim checkAndParseAuthInfo(HttpServletRequest request, String tokenHeader) {
        String tokenPrefix = authenticateProperties.getTokenPrefix();
        if (StringUtils.isBlank(tokenHeader)) {
            throw new UnauthorizedException(BaseErrorCallbackCode.ILLEGAL_TOKEN);
        }
        String token = tokenHeader;
        if (StringUtils.isNotBlank(authenticateProperties.getTokenPrefix())) {
            token = tokenHeader.split(tokenPrefix)[1];
        }

        AuthenticateCheckResult authenticateCheckResult = TokenUtil.checkToken(token);
        UserClaim tokenUserClaim = authenticateCheckResult.getUserClaim();
        // 额外业务token校验规则
        return tokenCustomizeCheckService.customizeCheck(request, authenticateCheckResult);
    }

    /**
     * 解析客户端请求头
     *
     * @param request
     * @param userClaim
     * @return
     */
    private Map<String, String> resolveClientHeaders(HttpServletRequest request, UserClaim userClaim) {
        Map<String, String> clientHeaderMap = new HashMap<>();
        // 处理客户端传递的约定好的请求头
        final Map<String, RequestHeaderEnum> clientHeaders = RequestHeaderEnum.getAllClientHeaders();
        clientHeaders.forEach((name, obj) -> {
            clientHeaderMap.put(name, Optional
                    .ofNullable(request.getHeader(name))
                    .orElse(obj.getDefaultValue()));
        });
        return clientHeaderMap;
    }

    /**
     * 解析服务端内部请求头
     *
     * @param request
     * @param userClaim
     * @return
     */
    private Map<String, String> resolveServerHeaders(HttpServletRequest request, UserClaim userClaim) {
        Map<String, String> serverHeaderMap = new HashMap<>();
        // 处理服务端内部的请求头
        serverHeaderMap.put(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName(), WebUtil.getHost());
        serverHeaderMap.put(
                RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName(),
                Objects.nonNull(userClaim) ? userClaim.getUserId() : request.getHeader(RequestHeaderEnum.IMEI.getName())
        );
        serverHeaderMap.put(
                RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName(),
                RequestHeaderEnum.IS_GATEWAY_DISPATCH.getDefaultValue()
        );
        serverHeaderMap.put(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName(), generateTraceId(
                Objects.nonNull(userClaim) ? userClaim.getUserId() :
                        request.getHeader(RequestHeaderEnum.IMEI.getName())));
        return serverHeaderMap;
    }

    public void resolveRequestContext() {

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
