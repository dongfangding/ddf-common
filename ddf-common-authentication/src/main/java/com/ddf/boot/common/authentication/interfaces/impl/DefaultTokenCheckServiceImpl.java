package com.ddf.boot.common.authentication.interfaces.impl;

import com.ddf.boot.common.api.exception.UnauthorizedException;
import com.ddf.boot.common.authentication.config.AuthenticationProperties;
import com.ddf.boot.common.authentication.interfaces.TokenCustomizeCheckService;
import com.ddf.boot.common.authentication.interfaces.UserClaimService;
import com.ddf.boot.common.authentication.model.AuthenticateCheckResult;
import com.ddf.boot.common.authentication.model.UserClaim;
import com.ddf.boot.common.core.util.PreconditionUtil;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>description</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/27 21:56
 */
@Service
@Slf4j
public class DefaultTokenCheckServiceImpl implements TokenCustomizeCheckService {

    @Autowired
    private AuthenticationProperties authenticationProperties;
    @Autowired(required = false)
    private UserClaimService userClaimService;

    /**
     * 业务校验规则
     *
     * @param request
     * @param authenticateCheckResult
     * @return
     */
    @Override
    public UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult authenticateCheckResult) {
        final UserClaim tokenUserClaim = authenticateCheckResult.getUserClaim();
        PreconditionUtil.checkArgument(Objects.nonNull(tokenUserClaim), "解析用户为空!");
        PreconditionUtil.checkArgument(!StringUtils.isAnyBlank(tokenUserClaim.getUsername(), tokenUserClaim.getCredit()),
                "用户关键信息缺失！");

        final String credit = request.getHeader(authenticationProperties.getCreditHeaderName());
        // credit校验
        if (Objects.nonNull(tokenUserClaim.getCredit()) && !Objects.equals(tokenUserClaim.getCredit(), credit) && !tokenUserClaim.ignoreCredit()) {
            log.error("当前请求credit和token不匹配， 当前: {}, token: {}", credit, tokenUserClaim.getCredit());
            throw new UnauthorizedException("登录环境变更，需要重新登录！");
        }
        // 获取最新用户信息
        UserClaim storeUser = userClaimService.getStoreUserInfo(tokenUserClaim);
        PreconditionUtil.checkArgument(!storeUser.isDisabled(), new UnauthorizedException("用户已被关进小黑屋了~"));
        // 如果token中设置了相关值，则进行判断，否则视为不开启对应校验。
        if (Objects.nonNull(tokenUserClaim.getLastModifyPasswordTime()) && !Objects.equals(tokenUserClaim.getLastModifyPasswordTime(), storeUser.getLastModifyPasswordTime())) {
            log.error("密码已经修改，不允许通过！当前修改密码时间: {}, token: {}", storeUser.getLastModifyPasswordTime(), tokenUserClaim.getLastModifyPasswordTime());
            throw new UnauthorizedException("密码已经修改，请重新登录！");
        }
        return storeUser;
    }
}
