package com.ddf.boot.common.jwt.config;

import com.ddf.boot.common.jwt.util.JwtUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Jwt相关配置类
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
@ConfigurationProperties(prefix = "customs.jwt")
@Data
@NoArgsConstructor
@Component
public class JwtProperties {

    public static final String BEAN_NAME = "jwtProperties";


    /**
     * 适用于permitAll的放行路径配置
     * httpSecurity.authorizeRequests().antMatchers(httpMethod, path).permitAll()
     * 如果配置了httpMethod，则针对该方法配置路径；
     * 如果没有配置httpMethod或为*,则直接配置路径
     *
     * @see PathMatch
     */
    private List<PathMatch> ignores = new ArrayList<>();

    /**
     * 当token的过期时间小于等于这个时间的时候，服务端重新生成token,
     * 单位 分钟
     */
    private int refreshTokenMinute;

    /**
     * 过期时间，第一次生成token时登录时别的服务生成的，这个过期时间是网关判断token即将失效，重新签发的
     * 单位  分钟
     */
    private int expiredMinute;

    /**
     * 生成的秘钥
     */
    private String secret;


    /**
     * 判断路径是否跳过
     *
     * @param path
     * @return
     */
    public boolean isIgnore(String path) {
        if (ignores == null || ignores.isEmpty()) {
            return false;
        }
        List<String> pathList = ignores.stream().filter(s -> StringUtils.isNotBlank(s.getPath())).map(
                PathMatch::getPath).collect(Collectors.toList());
        // 当前请求地址是否需要跳过认证
        if (!pathList.isEmpty()) {
            for (String ignore : pathList) {
                if (JwtUtil.getAntPathMatcher().match(ignore, path)) {
                    return true;
                }
            }
        }
        return false;
    }
}
