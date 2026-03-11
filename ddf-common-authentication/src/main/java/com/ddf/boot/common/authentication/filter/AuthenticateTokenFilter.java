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
import com.ddf.boot.common.core.authentication.TokenUtil;
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
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
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

        final HashMap<String, String> allHeaderMap = new HashMap<>(clientHeaderMap);
        allHeaderMap.putAll(customizeHeaderMap);
        // 预留认证通过后置接口
        userClaimService.afterTokenVerifySuccess(request, userClaim, allHeaderMap, customizeHeaderMap);

        // 重放简单校验
        final long nonce = Long.parseLong(Objects.requireNonNull(request.getHeader(RequestHeaderEnum.NONCE.getName())));
        final long currentTimeMillis = System.currentTimeMillis();
        final Integer timeForceCheckDiffMinute = authenticateProperties.getTimeForceCheckDiffMinute();
        if (nonce < currentTimeMillis - TimeUnit.MINUTES.toMillis(timeForceCheckDiffMinute)
                || nonce > currentTimeMillis + TimeUnit.MINUTES.toMillis(timeForceCheckDiffMinute)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
        }
        // 校验签名
        checkSign(request);
        // 分发服务前
        final ResponseData<Object> responseData = userClaimService.beforeDispatch(
                request, response, userClaim, allHeaderMap, customizeHeaderMap);
        if (!responseData.isSuccess()) {
            WebUtil.writerJson(response, JsonUtil.toJson(responseData));
            return false;
        }
        // 构建解析后的上下文
        buildContext(request, userClaim, clientIp, token);
        return true;
    }
    /**
     * @param request 参数
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
     * @param request 参数
     * @param userClaim 参数
     * @param clientIp 参数
     * @param token 参数
     */
    public void buildContext(HttpServletRequest request, UserClaim userClaim, String clientIp, String token) {
        // 解析请求头
        resolveRequestContext(request, userClaim, clientIp, token);
        MDC.put(AuthenticateConstant.MDC_USER_ID, UserContextUtil.getUserId());
        MDC.put(
                AuthenticateConstant.MDC_TRACE_ID,
                request.getHeader(RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName())
        );
        MDC.put(AuthenticateConstant.MDC_CLIENT_IP, UserContextUtil.getClientIpFromGateway());
        MDC.put(AuthenticateConstant.MDC_IMEI, UserContextUtil.getImei());
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
     * @param request 参数
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
                && !SignatureUtil.verifySelfSignature(data, sign, authenticateProperties.getSignSecret(), true)) {
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
        removeContext();
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
            clientHeaderMap.put(
                    name, Optional
                            .ofNullable(request.getHeader(name))
                            .orElse(obj.getDefaultValue())
            );
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
        serverHeaderMap.put(
                RequestHeaderEnum.TRACE_ID_FROM_GATEWAY.getName(), generateTraceId(
                        Objects.nonNull(userClaim) ? userClaim.getUserId() :
                                request.getHeader(RequestHeaderEnum.IMEI.getName()))
        );
        return serverHeaderMap;
    }
    /**
     * @param request 参数
     * @param userClaim 参数
     * @param clientIp 参数
     * @param token 参数
     */
    public void resolveRequestContext(HttpServletRequest request, UserClaim userClaim, String clientIp, String token) {
        // TODO 可以预留一个集合属性，允许外部配置自定义的请求头，这里去解析自定义的请求头，才能保证这个模块作为基础模块被引用
        UserContextUtil.setRequestContext(RequestContext
                .builder()
						.token(token)
                .userClaim(userClaim)
                .sign(request.getHeader(RequestHeaderEnum.SIGN.getName()))
                .os(OsEnum.resolve(request.getHeader(RequestHeaderEnum.OS.getName())))
                .imei(request.getHeader(RequestHeaderEnum.IMEI.getName()))
                .nonce(Long.parseLong(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.NONCE.getName()), "0")))
                .versionCode(Integer.parseInt(
                        StringUtils.defaultIfBlank(request.getHeader(RequestHeaderEnum.VERSION_CODE.getName()), "0")))
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


    public void removeContext() {
        // 移除用户信息
        UserContextUtil.removeRequestContext();
        UserContextUtil.removeUserClaim();
        MDC.remove(AuthenticateConstant.MDC_USER_ID);
        MDC.remove(AuthenticateConstant.MDC_TRACE_ID);
        MDC.remove(AuthenticateConstant.MDC_CLIENT_IP);
        MDC.remove(AuthenticateConstant.MDC_IMEI);
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
