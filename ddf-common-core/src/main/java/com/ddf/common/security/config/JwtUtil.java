package com.ddf.common.security.config;

import com.ddf.common.util.JsonUtil;
import com.ddf.common.util.SecureUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.util.*;

/**
 * Jwt的工具类
 * <p>
 * <p>
 * 问题列表
 * 1. 是否需要解决同一个token同一时刻只能维持一个人在线？
 * 解决方案： 生成token的时候将请求的ip也写入到payload中，同时维护该用户的信任列表，每次签发token都会生成一个授信ip列表，
 * 如果当前token里的ip和当前请求不一致，则认证不通过，要求客户端重新认证；重新生成token后，再增加当前ip为可信；
 * 同一时间内可以存在多个有效的token,只要token里的ip是该用户的授信ip即可
 * <p>
 * 2. token是否需要自动刷新功能？采用什么方式刷新？
 * 2.1 由服务端提供刷新方法，客户端每次请求前先判断是否将要过期，如果将要过期，则发送一个请求由服务端返回最新的token，客户端使用最新的token；
 * 带来的问题是，同一个用户会有两个有效的token，一个即将失效，但如果没失效发送请求怎么办？所以是不是服务端重新生成时需要将旧的token加入黑名单
 * 2.2 客户端只发送token即可，服务端验证通过后，判断是否即将失效，如果即将失效，主动重新生成，放入到Response Header中；客户端判断如果
 * Response Header中存在token，则替换掉旧的token;与2.1存在同样的问题，旧的token是否加入到黑名单？
 * 2.3 生成token的时候同时生成一个refresh expired 代表最终过期时间，服务端验证的时候，如果已经过期，但是这个refresh expired没有过期，就重新生成；
 * 这样就不会存在同一个用户出现两个token的问题，但是可能会存在并发问题，短时间内发送两个请求，每个请求中都感知到了原token过期并都准备重新生成；
 * 最终会返回两个有效的token; 2.1 和 2.2 其实也会存在并发生成的问题，如果满足有多个请求过来，都会去生成；
 * 这个只能加锁了，但是加锁需要考虑分布式锁；
 * <p>
 * 在该项目中：
 * 生成多个先不管；因为有了第一步的ip限制
 * <p>
 * <p>
 * 3. 修改密码，原token是否需要失效？？？？？？
 * 解决方案： 每次校验的时候，需要查出来用户信息，用户信息存入最后修改密码时间，如果这个时间晚于token签发时间，就提示token过期
 * <p>
 * 4. 如何避免服务端签发的token被盗用？
 * 同1，如果被盗走，发送请求的ip和用户可授信ip列表的ip不一致，照样不允许登录
 * <p>
 * 总结：
 * 关于授信设备的key选择，使用ip并不是一个很好的方案，但是这个值又必须是由服务端无论什么设备都能获取的一个值，比如mac地址自然是最好的；
 * 但是没有找到获取mac地址的方法；后面再看有没有更好的方法。
 *
 * @author dongfang.ding
 * @date 2019/7/17 16:00
 * @see UserClaim
 */
public class JwtUtil {

    /**
     * 生成与解析jws如果不是同一台机器可能会存在时钟差的问题
     * 而导致jws失效，这里提供一个忽略值
     */
    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 120;

    static {

    }

    /**
     * 创建默认的Jws payload
     * 会将传入的UserClaim里的有get方法的所有属性附加到payload中
     *
     * @param userClaim
     * @return
     */
    public static String defaultJws(UserClaim userClaim, int expiredMinute) {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, expiredMinute);
        calendar.getTime();


        return Jwts.builder()
                .addClaims(userClaim.toMap())
                .setId(UUID.randomUUID().toString())
                .setIssuer(userClaim.getUsername())
                .setSubject(JsonUtil.asString(userClaim))
                .setExpiration(calendar.getTime())
                .setIssuedAt(now)
                .signWith(SecureUtil.getPrivateRsa().getPrivateKey()).compact();
    }

    /**
     * 创建jws
     *
     * @param claims
     * @return
     */
    public static String createJws(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SecureUtil.getPrivateRsa().getPrivateKey()).compact();
    }

    /**
     * 解析jws，异常放在调用方去捕获，方便根据异常类型做不同的事情
     *
     * @param jws
     * @return
     */
    public static Jws<Claims> parseJws(String jws) {
        return parseJws(jws, ALLOWED_CLOCK_SKEW_SECONDS);
    }


    /**
     * 解析jws，异常放在调用方去捕获，方便根据异常类型做不同的事情
     *
     * @param jws
     * @param allowedClockSkewSeconds 解析时为了可以忽略的一个时间差，单位为秒；在这个时间差时间，jws依然有效
     * @return
     */
    public static Jws<Claims> parseJws(String jws, int allowedClockSkewSeconds) {
        return Jwts.parser()
                .setAllowedClockSkewSeconds(allowedClockSkewSeconds)
                .setSigningKey(SecureUtil.getPrivateRsa().getPublicKey())
                .parseClaimsJws(jws);
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
}
