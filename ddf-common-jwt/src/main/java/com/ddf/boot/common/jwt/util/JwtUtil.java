package com.ddf.boot.common.jwt.util;

import cn.hutool.core.convert.Convert;
import com.ddf.boot.common.jwt.config.JwtProperties;
import com.ddf.boot.common.jwt.consts.JwtConstant;
import com.ddf.boot.common.jwt.exception.UserClaimMissionException;
import com.ddf.boot.common.jwt.model.UserClaim;
import com.ddf.boot.common.util.JsonUtil;
import com.ddf.boot.common.util.SpringContextHolder;
import com.ddf.boot.common.util.WebUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.KeyException;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.util.AntPathMatcher;

import java.util.*;

/**
 * Jwt的工具类
 *
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
 * @see UserClaim
 */
public class JwtUtil {

    private static JwtProperties jwtProperties = SpringContextHolder.getBean(JwtProperties.class);

    private static volatile AntPathMatcher antPathMatcher;

    /**
     * 生成与解析jws如果不是同一台机器可能会存在时钟差的问题
     * 而导致jws失效，这里提供一个忽略值
     */
    private static final int ALLOWED_CLOCK_SKEW_SECONDS = 120;


    /**
     * 由于Jwt要判断客户端的IP，但是在RpcContext中有可能会获取不到（不知道为啥，就是有一次没有获取到），现在为了
     * 保险，如果没有获取到给个默认值；如果是默认值也算IP 匹配
     */
    public static final String DEFAULT_CLIENT_IP = "127.0.0.1";


    /**
     *
     * 创建默认的Jws payload
     * 会将传入的UserClaim里的有get方法的所有属性附加到payload中;
     *
     * @param userClaim
     * @return

     */
    public static String defaultJws(UserClaim userClaim) {
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, jwtProperties.getExpiredMinute());
        calendar.getTime();


        return Jwts.builder()
                .addClaims(userClaim.toMap())
                .setId(UUID.randomUUID().toString())
                .setIssuer(userClaim.getUsername())
                .setSubject(JsonUtil.asString(userClaim))
                .setExpiration(calendar.getTime())
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret()).compact();
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
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret()).compact();
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
    public static Jws<Claims> parseJws(String jws, int allowedClockSkewSeconds) throws KeyException, ExpiredJwtException {
        return Jwts.parser()
                .setAllowedClockSkewSeconds(allowedClockSkewSeconds)
                .setSigningKey(jwtProperties.getSecret())
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


    /**
     * 获取当前用户信息，如果没有获取到用户信息会抛出异常
     *
     * @return
     * @throws UserClaimMissionException
     */
    public static UserClaim getByContext() throws UserClaimMissionException {
        return getByContext(true);
    }


    private static UserClaim getByContext(boolean necessary) throws UserClaimMissionException {
        Object headerUser;
        try {
            headerUser = WebUtil.getCurRequest().getAttribute(JwtConstant.HEADER_USER);
            if (headerUser == null) {
                headerUser = RpcContext.getContext().getAttachment(JwtConstant.HEADER_USER);
            }
        } catch (Exception e) {
            headerUser = RpcContext.getContext().getAttachment(JwtConstant.HEADER_USER);
        }
        if (headerUser == null) {
            throw new UserClaimMissionException("无法获取当前用户信息！");
        }
        return JsonUtil.toBean(Convert.toStr(headerUser), UserClaim.class);
    }
    
    
    /**
     * 获取当前用户信息，如果没有获取到用户信息，会返回默认用户信息
     
     * @return com.ddf.boot.common.jwt.model.UserClaim
     * @author dongfang.ding
     * @date 2019/12/9 0009 16:23
     **/
    public static UserClaim getByContextNotNecessary() {
        try {
           return getByContext(false); 
        } catch (Exception e) {
            return UserClaim.defaultUser();
        }
    }

    /**
     * 获取客户端ip
     * @return
     */
    public static String getHost() {
        try {
            return WebUtil.getHost();
        } catch (Exception e) {
            String clientIp = RpcContext.getContext().getAttachment(JwtConstant.CLIENT_IP);
            if (clientIp == null) {
                return DEFAULT_CLIENT_IP;
            }
            return clientIp;
        }
    }


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
