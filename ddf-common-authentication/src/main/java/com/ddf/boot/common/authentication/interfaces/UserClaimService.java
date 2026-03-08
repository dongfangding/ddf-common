package com.ddf.boot.common.authentication.interfaces;

import com.ddf.boot.common.api.model.authentication.UserClaim;
import com.ddf.boot.common.api.model.common.response.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 提供一个接口让调用方实现，来将用户的最新数据信息加载进来，这样模块才能验证信息
 *
 * @author dongfang.ding
 * @since 2019-12-07 16:45
 */
public interface UserClaimService {

    /**
     * 校验/解析token之前
     *
     * @param request
     * @param response
     * @param clientHeaderMap    客户端传递的请求头
     * @param customizeHeaderMap 自定义的请求头
     * @return
     */
    default ResponseData<Object> beforeTokenVerify(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> clientHeaderMap, Map<String, String> customizeHeaderMap) {
        return ResponseData.success(null);
    }


    /**
     * 验证通过后预留一个接口允许客户端对用户做一些事情；
     * 如可以将用户放在自行选择的安全框架上下文中
     *
     * @param request
     * @param userClaim
     * @param headerMap
     * @param customizeHeaderMap
     */
    default void afterTokenVerifySuccess(HttpServletRequest request, UserClaim userClaim, Map<String, String> headerMap,
            Map<String, String> customizeHeaderMap) {

    }

    /**
     * Jwt将token中的用户信息，传递给调用方，需要调用方实现这个接口来将数据库中的最新用户数据返回过来
     *
     * @param request
     * @param userClaim
     * @return
     */
    UserClaim getStoreUserInfo(HttpServletRequest request, UserClaim userClaim);


    /**
     * 请求分发前，所有自己服务的网关流程已经走完，开始将请求分发到下游服务
     *
     * @param request
     * @param response
     * @param userClaim
     * @param headerMap
     * @param customizeHeaderMap
     * @return
     */
    default ResponseData<Object> beforeDispatch(HttpServletRequest request, HttpServletResponse response,
            UserClaim userClaim, Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
        return ResponseData.success(null);
    }



}
