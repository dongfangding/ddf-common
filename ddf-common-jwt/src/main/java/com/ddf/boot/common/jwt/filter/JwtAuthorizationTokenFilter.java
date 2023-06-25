package com.ddf.boot.common.jwt.filter;

import cn.hutool.core.collection.CollUtil;
import com.ddf.boot.common.api.exception.AccessDeniedException;
import com.ddf.boot.common.api.util.JsonUtil;
import com.ddf.boot.common.core.helper.EnvironmentHelper;
import com.ddf.boot.common.core.util.IdsUtil;
import com.ddf.boot.common.jwt.config.JwtProperties;
import com.ddf.boot.common.jwt.consts.JwtConstant;
import com.ddf.boot.common.jwt.interfaces.UserClaimService;
import com.ddf.boot.common.jwt.model.UserClaim;
import com.ddf.boot.common.jwt.util.JwtUtil;
import com.ddf.boot.common.jwt.util.UserContextUtil;
import com.ddf.boot.common.mvc.util.WebUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.security.KeyException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

;

/**
 * 拦截请求处理用户信息
 * <p>
 * _ooOoo_
 * o8888888o
 * 88" . "88
 * (| -_- |)
 * O\ = /O
 * ___/`---'\____
 * .   ' \\| |// `.
 * / \\||| : |||// \
 * / _||||| -:- |||||- \
 * | | \\\ - /// | |
 * | \_| ''\---/'' | |
 * \ .-\__ `-` ___/-. /
 * ___`. .' /--.--\ `. . __
 * ."" '< `.___\_<|>_/___.' >'"".
 * | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * \ \ `-. \_ __\ /__ _/ .-` / /
 * ======`-.____`-.___\_____/___.-`____.-'======
 * `=---='
 * .............................................
 * 佛曰：bug泛滥，我已瘫痪！
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 */
@Slf4j
public class JwtAuthorizationTokenFilter extends HandlerInterceptorAdapter {

    public static final String BEAN_NAME = "jwtAuthorizationTokenFilter";

    /**
     * 系统级别忽略的路径
     */
    private static final List<String> SYSTEM_IGNORE_PATH = Collections.unmodifiableList(Lists.newArrayList("/error"));

    /**
     * 用户uid
     */
    private static final String USER_ID = "userId";

    /**
     * 日志跟踪上下文id
     */
    private static final String TRACE_ID = "traceId";

    @Autowired(required = false)
    private UserClaimService userClaimService;

    @Autowired
    private JwtProperties jwtProperties;
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
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath();
        if (SYSTEM_IGNORE_PATH.contains(path)) {
            return true;
        }
        String host = WebUtil.getHost();
        final String tokenHeader = request.getHeader(jwtProperties.getTokenHeaderName());
        request.setAttribute(JwtConstant.CLIENT_IP, host);
        // 跳过忽略路径
        if (jwtProperties.isIgnore(path)) {
            return true;
        }

        if (userClaimService == null) {
            throw new NoSuchBeanDefinitionException(UserClaimService.class);
        }
        // 填充认证接口前置属性
        userClaimService.storeRequest(request, host);

        // 校验并转换jws
        AuthInfo authInfo = checkAndGetJws(request, tokenHeader);
        final UserClaim userClaim = authInfo.getUserClaim();
        final Jws<Claims> claimsJws = authInfo.getClaimsJws();
        String token = authInfo.getRealToken();
        UserClaim storeUserClaim = authInfo.getStoreUserClaim();

        // 预留认证通过后置接口
        userClaimService.afterVerifySuccess(userClaim);

        // 如果token即将失效，服务端主动用原来token中的信息重新生成token返回给客户端
        if (Objects.nonNull(claimsJws)) {
            long oldExpiredMinute = claimsJws.getBody().getExpiration().getTime();
            long now = System.currentTimeMillis();
            // 如果在未将新token返回给客户端或客户端未替换掉旧的token之前有多个请求过来，会生成多个有效token，由于token有信任
            // 设备的管理，因此这里不再做分布式锁的处理，只随缘用本地锁稍微意思一下
            if (oldExpiredMinute - now <= (long) jwtProperties.getRefreshTokenMinute() * 60 * 1000) {
                synchronized (token.intern()) {
                    token = JwtUtil.defaultJws(userClaim);
                    response.setHeader(jwtProperties.getTokenHeaderName(), token);
                }
            }
        }
        String userInfo = JsonUtil.asString(storeUserClaim);
        // 塞入最新用户数据
        UserContextUtil.setUserClaim(storeUserClaim);
        MDC.put(USER_ID, storeUserClaim.getUserId());
        MDC.put(TRACE_ID, storeUserClaim.getUserId() + "-" + IdsUtil.getNextStrId());
        request.setAttribute(JwtConstant.HEADER_USER, userInfo);
        return true;
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
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable
            Exception ex) throws Exception {
        // 移除用户信息
        UserContextUtil.removeUserClaim();
        MDC.remove(USER_ID);
        MDC.remove(TRACE_ID);
    }

    /**
     * 校验并转换jws
     *
     * @param request
     * @param tokenHeader
     * @return
     */
    private AuthInfo checkAndGetJws(HttpServletRequest request, String tokenHeader) {
        UserClaim tokenUserClaim;
        if (tokenHeader == null || !tokenHeader.startsWith(jwtProperties.getTokenPrefix())) {
            throw new AccessDeniedException("token格式不合法！");
        }
        String token = tokenHeader.split(jwtProperties.getTokenPrefix())[1];

        Jws<Claims> claimsJws = null;
        if (jwtProperties.isMock() && !environmentHelper.isProdProfile() &&
                CollUtil.isNotEmpty(jwtProperties.getMockUserIdList()) && jwtProperties.getMockUserIdList().contains(token)) {
            tokenUserClaim = UserClaim.mockUser(token);
        } else {
            try {
                claimsJws = JwtUtil.parseJws(token, 0);
            } catch (KeyException e) {
                throw new AccessDeniedException("token无效！");
            } catch (ExpiredJwtException e) {
                throw new AccessDeniedException("token已过期！");
            } catch (Exception e) {
                throw new AccessDeniedException("token解析失败！");
            }
            tokenUserClaim = JwtUtil.getUserClaim(claimsJws);
        }

        Preconditions.checkNotNull(tokenUserClaim, "解析用户为空!");
        Preconditions.checkArgument(!StringUtils.isAnyBlank(tokenUserClaim.getUsername(), tokenUserClaim.getCredit()),
                "用户关键信息缺失！"
        );

        final String userAgent = WebUtil.getUserAgent(request);
        if (!Objects.equals(tokenUserClaim.getCredit(), userAgent)) {
            log.error("请求环境已变更， 当前: {}, user-agent: {}", userAgent, tokenUserClaim);
            throw new AccessDeniedException("请求环境已变更， 请重新登录！");
        }

        UserClaim storeUser = userClaimService.getStoreUserInfo(tokenUserClaim);

        if (Objects.nonNull(storeUser.getLastModifyPasswordTime()) && !Objects.equals(tokenUserClaim.getLastModifyPasswordTime(), storeUser.getLastModifyPasswordTime())) {
            log.error("密码已经修改，不允许通过！当前修改密码时间: {}, token: {}", storeUser.getLastLoginTime(), tokenUserClaim);
            throw new AccessDeniedException("密码已经修改，请重新登录！");
        }
        if (Objects.nonNull(storeUser.getLastLoginTime()) && !Objects.equals(tokenUserClaim.getLastLoginTime(), storeUser.getLastLoginTime())) {
            log.error("token已刷新！当前最后一次登录时间: {}, token: {}", storeUser.getLastLoginTime(), tokenUserClaim);
            throw new AccessDeniedException("token已刷新，请重新登录！");
        }
        return new AuthInfo().setRealToken(token)
                .setClaimsJws(claimsJws)
                .setUserClaim(tokenUserClaim)
                .setStoreUserClaim(storeUser);
    }


    @Data
    @Accessors(chain = true)
    public static class AuthInfo {

        /**
         * 真实jwt token内容
         */
        private String realToken;

        /**
         * token解析后的Jws对象
         */
        private Jws<Claims> claimsJws;

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
