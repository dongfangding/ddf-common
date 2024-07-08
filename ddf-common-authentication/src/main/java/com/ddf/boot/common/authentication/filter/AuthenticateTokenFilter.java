package com.ddf.boot.common.authentication.filter;

import com.ddf.boot.common.api.enums.OsEnum;
import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.BaseException;
import com.ddf.boot.common.api.exception.BusinessException;
import com.ddf.boot.common.api.exception.ServerErrorException;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.RequestContext;
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
import com.ddf.boot.common.core.util.IdsUtils;
import com.ddf.boot.common.core.util.SignatureUtils;
import com.ddf.boot.common.mvc.util.WebUtil;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
        // 解析请求头
        resolveRequestContext(request);
        if (SYSTEM_IGNORE_PATH.contains(url)) {
            return true;
        }
        final RequestContext requestContext = UserContextUtil.getRequestContext();
        final List<String> openIgnores = authenticateProperties.getOpenIgnores();
        if (GlobalAntMatcher.match(openIgnores, url)) {
            return true;
        }
        if (StringUtils.isAnyBlank(request.getHeader(RequestHeaderEnum.OS.getName()),
                request.getHeader(RequestHeaderEnum.IMEI.getName()),
                request.getHeader(RequestHeaderEnum.NONCE.getName()),
                request.getHeader(RequestHeaderEnum.VERSION.getName()),
                request.getHeader(RequestHeaderEnum.VERSION_CODE.getName())
        )) {
            throw new BusinessException(BaseErrorCallbackCode.ILLEGAL_REQUEST);
        }
        userClaimService.before(request, response);
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
        }
        if (userClaimService == null) {
            throw new NoSuchBeanDefinitionException(UserClaimService.class);
        }
        // 填充认证接口前置属性
        userClaimService.storeRequest(request, clientIp);

        // 预留认证通过后置接口
        userClaimService.afterVerifySuccess(userClaim);

        // 重放简单校验
        final long nonce = Long.parseLong(Objects.requireNonNull(request.getHeader(RequestHeaderEnum.NONCE.getName())));
        if (nonce < System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)) {
            throw new BusinessException(BaseErrorCallbackCode.SIGN_TIMESTAMP_ERROR);
        }

        // 签名校验
        final String contentType = request.getContentType();
        final MediaType mediaType = MediaType.parseMediaType(contentType);
        if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)
                || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) { // 适合 JSON 和 Form 提交的请求
            resolveBodySignData(request);
        } else {
            resolveQueryParamsSignData(request);
        }

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
        final String body = WebUtil.readBodyRepeat(request);
        Map dataMap = StringUtils.isNotBlank(body) ? JsonUtil.toBean(body, Map.class) : new HashMap();
        validSign(request, dataMap);
    }


    /**
     * QueryString传参签名校验
     *
     * @param request
     */
    private void resolveQueryParamsSignData(HttpServletRequest request) {
        final Map<String, Object> data = request.getParameterMap().entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, val -> val.getValue()[0]));
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
                && !SignatureUtils.verifySelfSignature(data, sign, authenticateProperties.getSignSecret())) {
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
        return String.join("-", userId, IdsUtils.getNextStrId());
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
