package com.ddf.boot.common.authentication.interfaces.impl;


import com.ddf.boot.common.api.exception.BaseErrorCallbackCode;
import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.core.util.PreconditionUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/05/27 21:56
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTokenCheckServiceImpl implements TokenCustomizeCheckService {

    public static final String BEAN_NAME = "defaultTokenCheckServiceImpl";

    private final AuthenticationProperties authenticationProperties;
    private final UserClaimService userClaimService;

    /**
     * 业务校验规则
     *
     * @param request 请求对象
     * @param authenticateCheckResult 认证校验结果
     * @return
     */
    @Override
    public UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult authenticateCheckResult) {
        final UserClaim tokenUserClaim = authenticateCheckResult.getUserClaim();
        PreconditionUtil.checkArgument(Objects.nonNull(tokenUserClaim), BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        PreconditionUtil.checkArgument(!StringUtils.isAnyBlank(tokenUserClaim.getUsername(), tokenUserClaim.getCredit()),
                BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        // credit校验
        final String credit = StringUtils.defaultIfBlank(request.getHeader(authenticationProperties.getCreditHeaderName()),
                request.getHeader("User-Agent"));
        if (Objects.nonNull(tokenUserClaim.getCredit()) && !Objects.equals(tokenUserClaim.getCredit(), credit)) {
            log.error("当前请求credit和token不匹配， 当前: {}, token: {}", credit, tokenUserClaim.getCredit());
            throw new UnauthorizedException(BaseErrorCallbackCode.USER_INFO_EXPIRED_OR_NOT_EXIST);
        }
        // 获取最新用户信息
        UserClaim storeUser = userClaimService.getStoreUserInfo(request, tokenUserClaim);
        return storeUser;
    }
}
