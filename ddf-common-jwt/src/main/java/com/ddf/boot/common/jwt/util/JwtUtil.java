package com.ddf.boot.common.jwt.util;

import com.ddf.boot.common.core.helper.SpringContextHolder;
import com.ddf.boot.common.core.model.UserClaim;
import com.ddf.boot.common.core.util.JsonUtil;
import com.ddf.boot.common.jwt.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.KeyException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;
import org.springframework.util.AntPathMatcher;

/**
 * Jwt的工具类
 *
 * @author dongfang.ding
 * @date 2019-12-07 16:45
 * @see UserClaim
 */
public class JwtUtil {

    private static final JwtProperties JWT_PROPERTIES = SpringContextHolder.getBean(JwtProperties.class);

    private static volatile AntPathMatcher antPathMatcher;

    /**
     * 生成与解析jws如果不是同一台机器可能会存在时钟差的问题
     * 而导致jws失效，这里提供一个忽略值
     */
    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 120;


    /**
     * 创建默认的Jws payload
     * 会将传入的UserClaim里的有get方法的所有属性附加到payload中;
     *
     * @param userClaim
     * @return
     */
    public static String defaultJws(UserClaim userClaim) {
        return defaultJws(userClaim, Collections.emptyMap());
    }

    /**
     * 创建默认的Jws payload
     * 会将传入的UserClaim里的有get方法的所有属性附加到payload中;
     *
     * @param userClaim
     * @return
     */
    public static String defaultJws(UserClaim userClaim, Map<String, Object> claims) {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, JWT_PROPERTIES.getExpiredMinute());
        final Map<String, Object> finalClaimMap = userClaim.toMap();
        if (!io.jsonwebtoken.lang.Collections.isEmpty(claims)) {
            finalClaimMap.putAll(claims);
        }
        return Jwts.builder()
                .addClaims(finalClaimMap)
                .setId(UUID.randomUUID().toString())
                .setIssuer(userClaim.getUsername())
                .setSubject(JsonUtil.asString(userClaim))
                .setExpiration(calendar.getTime())
                .setIssuedAt(now)
                .signWith(
                        Keys.hmacShaKeyFor(JWT_PROPERTIES.getSecret().getBytes(StandardCharsets.UTF_8)),
                        SignatureAlgorithm.HS512
                )
                .compact();
    }

    /**
     * 创建jws
     *
     * @param claims
     * @return
     */
    public static String createJws(Map<String, Object> claims) {

        return Jwts.builder().addClaims(claims).signWith(
                Keys.hmacShaKeyFor(JWT_PROPERTIES.getSecret().getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS512
        ).compact();
    }

    /**
     * 解析jws，异常放在调用方去捕获，方便根据异常类型做不同的事情
     *
     * @param jws
     * @return
     */
    public static Jws<Claims> parseJws(String jws) throws KeyException, ExpiredJwtException {
        return parseJws(jws, ALLOWED_CLOCK_SKEW_SECONDS);
    }


    /**
     * 解析jws，异常放在调用方去捕获，方便根据异常类型做不同的事情
     *
     * @param jws
     * @param allowedClockSkewSeconds 解析时为了可以忽略的一个时间差，单位为秒；在这个时间差时间，jws依然有效
     * @return
     */
    public static Jws<Claims> parseJws(String jws, int allowedClockSkewSeconds)
            throws KeyException, ExpiredJwtException {
        return Jwts.parser().setAllowedClockSkewSeconds(allowedClockSkewSeconds).setSigningKey(
                Keys.hmacShaKeyFor(JWT_PROPERTIES.getSecret().getBytes(StandardCharsets.UTF_8))).parseClaimsJws(jws);
    }


    /**
     * 从payload中解析出UserClaim对象
     * <p>
     * 注意payload存进去的数据格式再取出来并不能保证类型正确，所有会给反射造成一定的困扰；
     * 目前仅支持Integer Long String
     *
     * @param claimsJws
     * @return
     */
    public static UserClaim getUserClaim(Jws<Claims> claimsJws) {
        if (claimsJws.getBody() == null) {
            return new UserClaim();
        }
        Claims body = claimsJws.getBody();
        return JsonUtil.toBean(body.getSubject(), UserClaim.class);
    }

//    /**
//     * 获取用户uid
//     *
//     * @return
//     */
//    public static String getUserId() {
//        return getByContext().getUserId();
//    }
//
//    /**
//     * 尝试获取用户uid
//     *
//     * @return
//     */
//    public static String tryGetUserId() {
//        try {
//            return getByContext().getUserId();
//        } catch (UserClaimMissionException e) {
//            return UserClaim.defaultUser().getUserId();
//        }
//    }

//    /**
//     * 获取当前用户信息，如果没有获取到用户信息会抛出异常
//     *
//     * @return
//     */
//    public static UserClaim getByContext() {
//        return getByContext(true);
//    }
//
//
//    /**
//     * 从上下文中获取用户信息， 这个上下文可能是多个上下文， 如http请求头， RpcContext等
//     *
//     * @param necessary
//     * @return
//     */
//    private static UserClaim getByContext(boolean necessary) {
//        Object headerUser;
//        try {
//            headerUser = WebUtil.getCurRequest().getAttribute(JwtConstant.HEADER_USER);
//            if (headerUser == null) {
//                headerUser = RpcContext.getContext().getAttachment(JwtConstant.HEADER_USER);
//            }
//        } catch (Exception e) {
//            headerUser = RpcContext.getContext().getAttachment(JwtConstant.HEADER_USER);
//        }
//        if (headerUser == null) {
//            if (necessary) {
//                throw new UserClaimMissionException("无法获取当前用户信息！");
//            }
//            return UserClaim.defaultUser();
//        }
//        return JsonUtil.toBean(Convert.toStr(headerUser), UserClaim.class);
//    }
//
//
//    /**
//     * 获取当前用户信息，如果没有获取到用户信息，会返回默认用户信息
//     *
//     * @return com.ddf.boot.common.jwt.model.UserClaim
//     * @author dongfang.ding
//     * @date 2019/12/9 0009 16:23
//     **/
//    public static UserClaim getByContextNotNecessary() {
//        try {
//            return getByContext(false);
//        } catch (Exception e) {
//            return UserClaim.defaultUser();
//        }
//    }
//
//    /**
//     * 获取客户端ip
//     *
//     * @return
//     */
//    public static String getHost() {
//        try {
//            return WebUtil.getHost();
//        } catch (Exception e) {
//            String clientIp = RpcContext.getContext().getAttachment(JwtConstant.CLIENT_IP);
//            if (clientIp == null) {
//                return DEFAULT_CLIENT_IP.get(0);
//            }
//            return clientIp;
//        }
//    }


    /**
     * 返回一个单例的ant风格匹配器
     *
     * @return org.springframework.util.AntPathMatcher
     * @author dongfang.ding
     * @date 2019/12/30 0030 17:43
     **/
    public static AntPathMatcher getAntPathMatcher() {
        if (antPathMatcher == null) {
            synchronized (JwtUtil.class) {
                if (antPathMatcher == null) {
                    antPathMatcher = new AntPathMatcher();
                }
            }
        }
        return antPathMatcher;
    }
}
