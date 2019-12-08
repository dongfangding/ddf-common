package com.ddf.common.security.service.impl;

import com.ddf.common.jwt.consts.JwtConstant;
import com.ddf.common.jwt.interfaces.UserClaimService;
import com.ddf.common.jwt.model.UserClaim;
import com.ddf.common.util.WebUtil;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取数据库最新用户信息$
 *
 * @author dongfang.ding
 * @date 2019/12/7 0007 15:57
 */
@Service
public class UserClaimServiceImpl implements UserClaimService {


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
    @Override
    public void storeRequest(HttpServletRequest request, String host) {
        RpcContext.getContext().setRequest(request);
        RpcContext.getContext().setAttachment(JwtConstant.CLIENT_IP, WebUtil.getHost());
    }

    /**
     * Jwt将token中的用户信息，传递给调用方，需要调用方实现这个接口来将数据库中的最新用户数据返回过来
     *
     * @param userClaim
     * @return
     */
    @Override
    public UserClaim getStoreUserInfo(UserClaim userClaim) {
        return userClaim;
    }


    /**
     * 验证通过后蒋用户信息放到spring-security上下文中
     * @param userClaim
     * @return void
     * @author dongfang.ding
     * @date 2019/12/7 0007 15:58
     **/
    public void afterVerifySuccess(UserClaim userClaim) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userClaim,
                    null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(WebUtil.getCurRequest()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
