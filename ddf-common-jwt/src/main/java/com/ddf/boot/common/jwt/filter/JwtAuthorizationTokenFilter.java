package com.ddf.boot.common.jwt.filter;

import com.ddf.boot.common.jwt.config.JwtProperties;
import com.ddf.boot.common.jwt.model.UserClaim;
import com.ddf.boot.common.jwt.consts.JwtConstant;
import com.ddf.boot.common.jwt.interfaces.UserClaimService;
import com.ddf.boot.common.jwt.util.JwtUtil;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.util.WebUtil;
import com.google.common.base.Preconditions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.KeyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 拦截请求处理用户信息
 *
 */
@Slf4j
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    public static final String BEAN_NAME = "jwtAuthorizationTokenFilter";

    /**
     * 认证请求头字段
     */
    private static final String AUTH_HEADER = "Authorization";

    /**
     * 认证接口类型
     * 与{@link JwtAuthorizationTokenFilter#AUTH_HEADER} 组合表现形式为:
     * Authorization: Bearer <token>
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    @Autowired(required = false)
    private UserClaimService userClaimService;

    @Autowired
    private JwtProperties jwtProperties;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getServletPath();
        String host = WebUtil.getHost();
        request.setAttribute(JwtConstant.CLIENT_IP, host);
        userClaimService.storeRequest(request, host);
        RpcContext.getContext().setAttachment(JwtConstant.CLIENT_IP, WebUtil.getHost());
        // 跳过忽略路径
        if (jwtProperties.isIgnore(path)) {
            chain.doFilter(request, response);
            return;
        }
        final String tokenHeader = request.getHeader(AUTH_HEADER);
        if (tokenHeader == null || !tokenHeader.startsWith(TOKEN_PREFIX)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), String.format("token必须以【%s】开头！", TOKEN_PREFIX));
            return;
        }

        String token = tokenHeader.split(TOKEN_PREFIX)[1];
        Jws<Claims> claimsJws;
        try {
            claimsJws = JwtUtil.parseJws(token, 0);
        } catch (KeyException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "token无效！");
            return;
        } catch (ExpiredJwtException e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "token已过期！");
            return;
        } catch (Exception e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "token解析失败！");
            return;
        }

        UserClaim userClaim = JwtUtil.getUserClaim(claimsJws);
        Preconditions.checkNotNull(userClaim, "解析用户为空!");
        Preconditions.checkArgument(!StringUtils.isAnyBlank(userClaim.getUsername(), userClaim.getCredit()),
                "用户关键信息缺失！");

        if (userClaimService == null) {
            throw new NoSuchBeanDefinitionException(UserClaimService.class);
        }

        // 也可以维护一个列表， defaultClientIp其实只是一个保险，当获取不到的时候做一个妥协
        if (!Objects.equals(userClaim.getCredit(), WebUtil.getHost()) && !JwtUtil.DEFAULT_CLIENT_IP.equals(WebUtil.getHost())) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "更换登录地址，需要重新登录！");
            return;
        }

        UserClaim storeUser = userClaimService.getStoreUserInfo(userClaim);

        if (!Objects.equals(userClaim.getLastModifyPasswordTime(), storeUser.getLastModifyPasswordTime())) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "密码已经修改，请重新登录！");
            return;
        }
        if (!Objects.equals(userClaim.getLastLoginTime(), storeUser.getLastLoginTime())) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "token已刷新，请重新登录！");
            return;
        }

        userClaimService.afterVerifySuccess(userClaim);

        // 如果token即将失效，服务端主动用原来token中的信息重新生成token返回给客户端
        long oldExpiredMinute = claimsJws.getBody().getExpiration().getTime();
        long now = System.currentTimeMillis();
        // 如果在未将新token返回给客户端或客户端未替换掉旧的token之前有多个请求过来，会生成多个有效token，由于token有信任
        // 设备的管理，因此这里不再做分布式锁的处理，只随缘用本地锁稍微意思一下
        synchronized (token.intern()) {
            if (oldExpiredMinute - now <= jwtProperties.getRefreshTokenMinute() * 60 * 1000) {
                token = JwtUtil.defaultJws(userClaim);
                response.setHeader(AUTH_HEADER, token);
            }
        }
        String userInfo = JsonUtil.asString(storeUser);
        request.setAttribute(JwtConstant.HEADER_USER, userInfo);
        RpcContext.getContext().setAttachment(JwtConstant.HEADER_USER, userInfo);

        chain.doFilter(request, response);
    }
}
