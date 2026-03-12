package com.ddf.boot.common.authentication.interfaces;

import com.ddf.boot.common.api.model.authentication.AuthenticateCheckResult;
import com.ddf.boot.common.api.model.authentication.UserClaim;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>在通用的校验规则上可以实现该接口实现自己的校验规则</p >
 *
 * @author Snowball
 * @version 1.0
 * @since 2022/05/25 09:57
 */
public interface TokenCustomizeCheckService {


    /**
     * 自定义校验规则
     *
     * @param request 请求对象
     * @param authenticateCheckResult 认证校验结果
     * @return 返回最新的用户信息
     */
    UserClaim customizeCheck(HttpServletRequest request, AuthenticateCheckResult authenticateCheckResult);
}
