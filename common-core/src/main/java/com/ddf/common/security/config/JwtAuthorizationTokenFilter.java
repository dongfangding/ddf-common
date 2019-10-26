package com.ddf.common.security.config;

import com.ddf.common.util.WebUtil;
import com.google.common.base.Preconditions;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.KeyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author xujinquan
 */
@Slf4j
@Component
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

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


    @Autowired
    @Qualifier("userDetailsServiceImpl")
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtProperties jwtProperties;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String path = request.getServletPath();
        // 跳过忽略路径
        if (jwtProperties.isIgnore(path)) {
            chain.doFilter(request, response);
            return;
        }
        final String tokenHeader = request.getHeader(AUTH_HEADER);
        if (tokenHeader == null || !tokenHeader.startsWith(TOKEN_PREFIX)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "token格式错误！");
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
        Preconditions.checkArgument(!StringUtils.isAnyBlank(userClaim.getUsername(), userClaim.getCredit(),
                userClaim.getLastModifyPasswordTime().toString()), "用户关键信息缺失！");

        // FIXME 是否需要判断
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserClaim bootUser = (UserClaim) userDetailsService.loadUserByUsername(userClaim.getUsername());
            // 也可以维护一个列表
            if (!Objects.equals(userClaim.getCredit(), WebUtil.getHost())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "token无效！");
                return;
            }
            if (!Objects.equals(userClaim.getLastModifyPasswordTime(), bootUser.getLastModifyPasswordTime())) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "密码已经修改，请重新登录！");
                return;
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(bootUser,
                    null, bootUser.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }


        // 如果token即将失效，服务端主动用原来token中的信息重新生成token返回给客户端
        long oldExpiredMinute = claimsJws.getBody().getExpiration().getTime();
        long now = System.currentTimeMillis();
        // 如果在未将新token返回给客户端或客户端未替换掉旧的token之前有多个请求过来，会生成多个有效token，由于token有信任
        // 设备的管理，因此这里不再做分布式锁的处理，只随缘用本地锁稍微意思一下
        synchronized (token.intern()) {
            if (oldExpiredMinute - now <= jwtProperties.getRefreshTokenMinute() * 60 * 1000) {
                token = JwtUtil.defaultJws(userClaim, jwtProperties.getExpiredMinute());
                response.setHeader(AUTH_HEADER, token);
            }
        }
        chain.doFilter(request, response);
    }
}
