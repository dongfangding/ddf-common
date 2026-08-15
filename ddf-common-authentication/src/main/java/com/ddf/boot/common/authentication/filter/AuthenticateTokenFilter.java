package com.ddf.boot.common.authentication.filter;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.dto.RequestContext;
import com.ddf.boot.common.api.model.common.request.RequestHeaderEnum;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.api.util.UserContextUtil;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.consts.AuthenticateConstant;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.core.authentication.TokenGenerator;
import com.ddf.boot.common.core.event.LoginFailureEvent;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 拦截请求处理用户认证信息
 *
 * @author dongfang.ding
 * @since 2019-12-07 16:45
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticateTokenFilter implements HandlerInterceptor {

    public static final String BEAN_NAME = "authenticateTokenFilter";

    /**
     * 系统级别忽略的路径
     */
    private static final List<String> SYSTEM_IGNORE_PATH = Collections.unmodifiableList(Lists.newArrayList("/error"));

    private final UserClaimService userClaimService;
    private final TokenCustomizeCheckService tokenCustomizeCheckService;
    private final AuthenticationProperties authenticateProperties;
    private final TokenGenerator tokenGenerator;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 前置校验
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 当前处理器对象
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String url = request.getServletPath();
        // 通用请求头解析
        final Map<String, String> clientHeaderMap = resolveClientHeaders(request);
        // 自定义请求头解析， 处理过程中可以额外添加请求头，最终会被统一添加到请求头中
        final Map<String, String> customizeHeaderMap = new HashMap<>();
        if (SYSTEM_IGNORE_PATH.contains(url)) {
            return true;
        }
        // 开放接口校验，比如提供给外部的回调接口
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

        String clientIp = WebUtil.getHost();
        final String token = request.getHeader(authenticateProperties.getTokenHeaderName());
        request.setAttribute(AuthenticateConstant.CLIENT_IP, clientIp);
        final List<String> ignores = authenticateProperties.getIgnores();

        UserClaim userClaim = null;
        // 内部接口白名单
        if (!GlobalAntMatcher.match(ignores, url)) {
            userClaim = checkAndParseAuthInfo(request, token);
            if (Objects.isNull(userClaim)) {
                throw new BusinessException(BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
            }
        } else {
            userClaim = UserClaim.getDefaultUser();
        }
        // 添加服务端请求头
        clientHeaderMap.putAll(resolveServerHeaders(request, userClaim, clientIp));

        final HashMap<String, String> allHeaderMap = new HashMap<>(clientHeaderMap);
        allHeaderMap.putAll(customizeHeaderMap);
        // 预留认证通过后置接口
        userClaimService.afterTokenVerifySuccess(request, userClaim, allHeaderMap, customizeHeaderMap);

        // 重放校验：仅保留客户端时间戳 ± 允许误差范围（下界/上界）的校验。
        // 服务端 nonce 去重（记录已消费的 nonce）需要引入 Redis 等共享存储，为避免模块对存储的强依赖，暂未实现；
        // 时间戳上界+签名已能阻止大部分重放，若需更强防护可在此接入服务端 nonce 缓存。
        final long nonce = parseNonce(request.getHeader(RequestHeaderEnum.NONCE.getName()));
        final long currentTimeMillis = System.currentTimeMillis();
        final Integer timeForceCheckDiffMinute = authenticateProperties.getTimeForceCheckDiffMinute();
        if (nonce < currentTimeMillis - TimeUnit.MINUTES.toMillis(timeForceCheckDiffMinute)
                || nonce > currentTimeMillis + TimeUnit.MINUTES.toMillis(timeForceCheckDiffMinute)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
        }
        // 校验签名
        checkSign(request);
        // 分发服务前
        final ResponseData<Object> responseData = userClaimService.beforeDispatch(request, response, userClaim,
                allHeaderMap, customizeHeaderMap);
        if (!responseData.isSuccess()) {
            WebUtil.writerJson(response, JsonUtil.toJson(responseData));
            return false;
        }
        // 构建解析后的上下文
        buildContext(request, userClaim, clientIp, token);
        return true;
    }

    /**
     * @param request 请求对象
     */
    private void checkSign(HttpServletRequest request) {
        // 标准情况下，get方法应该是没有content-type的，但是有些不规范的写法会将这个传过来，导致走body签名，那就不管了。
        final String contentType = request.getContentType();
        boolean isBodyContentType = false;
        // 只有当 contentType 非空，且解析后的类型匹配时，才视为 Body 类型
        if (StringUtils.isNotBlank(contentType)) {
            final MediaType mediaType = MediaType.parseMediaType(contentType);
            isBodyContentType = MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)
                    || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
        }
        if (isBodyContentType) {
            resolveBodySignData(request);
        } else {
            resolveQueryParamsSignData(request);
        }
    }

    /**
     * @param request 请求对象
     * @param userClaim 用户声明信息
     * @param clientIp 客户端 IP
     * @param token token 字符串
     */
    private void buildContext(HttpServletRequest request, UserClaim userClaim, String clientIp, String token) {
        // 解析请求头
        resolveRequestContext(request, userClaim, clientIp, token);
        MDC.put(AuthenticateConstant.MDC_USER_ID, UserContextUtil.getUserId());
        MDC.put(AuthenticateConstant.MDC_TRACE_ID,
                request.getHeader(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName()));
        MDC.put(AuthenticateConstant.MDC_CLIENT_IP, UserContextUtil.getClientIpFromGateway());
        MDC.put(AuthenticateConstant.MDC_IMEI, UserContextUtil.getImei());
    }


    /**
     * body传参参数校验
     *
     * @param request 请求对象
     */
    private void resolveBodySignData(HttpServletRequest request) {
        final String body = WebUtil.readBody(request);
        Map dataMap = StringUtils.isNotBlank(body) ? JsonUtil.toBean(body, Map.class) : new HashMap();
        validSign(request, dataMap);
    }


    /**
     * QueryString传参签名校验
     *
     * @param request 请求对象
     */
    private void resolveQueryParamsSignData(HttpServletRequest request) {
        final Map<String, Object> data = request.getParameterMap().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, val -> val.getValue()[0]));
        validSign(request, data);
    }

    /**
     * 验签
     *
     * @param data 待处理数据
     * @param request 请求对象
     */
    private void validSign(HttpServletRequest request, Map<String, Object> data) {
        // 把请求头中的nonce也加入到加签规则字段中
        data.put(RequestHeaderEnum.NONCE.getName(), request.getHeader(RequestHeaderEnum.NONCE.getName()));
        final String sign = request.getHeader(RequestHeaderEnum.SIGN.getName());
        if (!authenticateProperties.isSignEnabled()) {
            return;
        }
        if (!SignatureUtil.verifySelfSignature(data, sign, authenticateProperties.getSignSecret(), true)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_ERROR);
        }
    }

    /**
     * 生成traceId
     *
     * @param userId 用户 ID
     */
    private String generateTraceId(String userId) {
        return String.join("-", userId, IdsUtil.getNextStrId());
    }

    /**
     * 执行器结束
     *
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 当前处理器对象
     * @param ex 异常对象
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
        removeContext();
    }

    /**
     * 校验并转换用户信息
     *
     * @param request 请求对象
     * @param tokenHeader 请求头中的 token 值
     */
    private UserClaim checkAndParseAuthInfo(HttpServletRequest request, String tokenHeader) {
        String tokenPrefix = authenticateProperties.getTokenPrefix();
        String token = tokenHeader;
        if (StringUtils.isNotBlank(tokenPrefix) && StringUtils.isNotBlank(tokenHeader) && tokenHeader.startsWith(tokenPrefix)) {
            token = tokenHeader.substring(tokenPrefix.length());
        }

        try {
            if (StringUtils.isBlank(tokenHeader)) {
                throw new UnauthorizedException(BaseErrorCallbackCode.ILLEGAL_TOKEN);
            }
            AuthenticateCheckResult authenticateCheckResult = tokenGenerator.checkToken(token);
            return tokenCustomizeCheckService.customizeCheck(request, authenticateCheckResult);
        } catch (BaseException e) {
            applicationEventPublisher.publishEvent(
                    new LoginFailureEvent(this, token, e.getBaseCallbackCode().getCode()));
            throw new BusinessException(e.getBaseCallbackCode());
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(new LoginFailureEvent(this, token, "SERVER_ERROR"));
            throw new ServerErrorException(BaseErrorCallbackCode.SERVER_ERROR);
        }
    }

    /**
     * 解析客户端请求头
     *
     * @param request 请求对象
     * @return 请求头映射
     */
    private Map<String, String> resolveClientHeaders(HttpServletRequest request) {
        return RequestHeaderEnum.resolveClientHeaders(request);
    }

    /**
     * 解析服务端内部请求头
     *
     * @param request 请求对象
     * @param userClaim 用户声明信息
     * @param clientIp 客户端 IP
     */
    private Map<String, String> resolveServerHeaders(HttpServletRequest request, UserClaim userClaim, String clientIp) {
        Map<String, String> serverHeaderMap = new HashMap<>();
        // 处理服务端内部的请求头
        serverHeaderMap.put(RequestHeaderEnum.CLIENT_IP_FROM_GATEWAY.getName(), clientIp);
        serverHeaderMap.put(RequestHeaderEnum.USER_ID_FROM_GATEWAY.getName(),
                Objects.nonNull(userClaim) ? userClaim.getUserId() :
                        request.getHeader(RequestHeaderEnum.IMEI.getName()));
        serverHeaderMap.put(RequestHeaderEnum.IS_GATEWAY_DISPATCH.getName(),
                RequestHeaderEnum.IS_GATEWAY_DISPATCH.getDefaultValue());
        serverHeaderMap.put(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName(), generateTraceId(
                Objects.nonNull(userClaim) ? userClaim.getUserId() :
                        request.getHeader(RequestHeaderEnum.IMEI.getName())));
        return serverHeaderMap;
    }

    /**
     * @param request 请求对象
     * @param userClaim 用户声明信息
     * @param clientIp 客户端 IP
     * @param token token 字符串
     */
    private void resolveRequestContext(HttpServletRequest request, UserClaim userClaim, String clientIp, String token) {
        // TODO 可以预留一个集合属性，允许外部配置自定义的请求头，这里去解析自定义的请求头，才能保证这个模块作为基础模块被引用
        UserContextUtil.setRequestContext(RequestContext.builder()
                .token(token)
                .userClaim(userClaim)
                .sign(request.getHeader(RequestHeaderEnum.SIGN.getName()))
                .os(OsEnum.resolve(request.getHeader(RequestHeaderEnum.OS.getName())))
                .imei(request.getHeader(RequestHeaderEnum.IMEI.getName()))
                .nonce(parseNonce(request.getHeader(RequestHeaderEnum.NONCE.getName())))
                .versionCode(parseVersionCode(request.getHeader(RequestHeaderEnum.VERSION_CODE.getName())))
                .version(request.getHeader(RequestHeaderEnum.VERSION.getName()))
                .language(request.getHeader(RequestHeaderEnum.LANGUAGE.getName()))
                .timeZone(request.getHeader(RequestHeaderEnum.TIME_ZONE.getName()))
                .osVersion(request.getHeader(RequestHeaderEnum.OS_VERSION.getName()))
                .deviceMode(request.getHeader(RequestHeaderEnum.DEVICE_MODE.getName()))
                .requestUri(request.getRequestURI())
                .clientIp(clientIp)
                .clientIpFromGateway(clientIp)
                .userIdFromGateway(userClaim.getUserId())
                .build());
    }


    private void removeContext() {
        // 移除用户信息
        UserContextUtil.removeRequestContext();
        UserContextUtil.removeUserClaim();
        MDC.remove(AuthenticateConstant.MDC_USER_ID);
        MDC.remove(AuthenticateConstant.MDC_TRACE_ID);
        MDC.remove(AuthenticateConstant.MDC_CLIENT_IP);
        MDC.remove(AuthenticateConstant.MDC_IMEI);
    }

    /**
     * 安全解析 nonce 时间戳，缺失时按 0 处理，格式非法按非法请求处理。
     */
    private static long parseNonce(String nonceValue) {
        try {
            return Long.parseLong(StringUtils.defaultIfBlank(nonceValue, "0"));
        } catch (NumberFormatException e) {
            throw new BusinessException(BaseErrorCallbackCode.ILLEGAL_REQUEST);
        }
    }

    /**
     * 安全解析 versionCode，缺失时按 0 处理，格式非法按非法请求处理。
     */
    private static int parseVersionCode(String versionCode) {
        try {
            return Integer.parseInt(StringUtils.defaultIfBlank(versionCode, "0"));
        } catch (NumberFormatException e) {
            throw new BusinessException(BaseErrorCallbackCode.ILLEGAL_REQUEST);
        }
    }

}
