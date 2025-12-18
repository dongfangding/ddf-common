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
 * @date 2019-12-07 16:45
 */
public interface UserClaimService {


    /**
     * 认证校验前的操作, 比如黑名单拉黑操作等
     *
     * @param request
     * @param response
     */
    default void beforeTokenVerify(HttpServletRequest request, HttpServletResponse response,
            Map<String, String> headerMap, Map<String, String> customizeHeaderMap) {
    }


    /**
     * 正常环境下能够获取到HttpServletRequest，但如果有些项目使用了RPC框架，请求被转发到另一个服务后，HttpServletRequest
     * 则无法正常获取，这里提供一个接口，使用者可以自行将对象放在一些上下文中；如RpcContext，则自己在对应的服务中按照自己
     * 存入的方式再获取到
     *
     * @param request
     * @param host    客户端请求ip
     * @return void
     * @author dongfang.ding
     * @date 2019/12/7 0007 16:04
     **/
    void storeRequest(HttpServletRequest request, String host);


    /**
     * 验证通过后预留一个接口允许客户端对用户做一些事情；
     * 如可以将用户放在自行选择的安全框架上下文中
     *
     * @param request
     * @param userClaim
     * @param headerMap
     * @param customizeHeaderMap
     */
    default void afterVerifySuccess(HttpServletRequest request, UserClaim userClaim, Map<String, String> headerMap,
            Map<String, String> customizeHeaderMap) {

    }

    /**
     * Jwt将token中的用户信息，传递给调用方，需要调用方实现这个接口来将数据库中的最新用户数据返回过来
     *
     * @param userClaim
     * @return
     */
    UserClaim getStoreUserInfo(HttpServletRequest request, UserClaim userClaim);


    /**
     * 请求分发前，所有自己服务的流程已经走完，开始将请求分发到下游服务
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
